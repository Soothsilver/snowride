package cz.hudecekpetr.snowride.filesystem;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.generalpurpose.Holder;
import cz.hudecekpetr.snowride.tree.highelements.FileSuite;
import cz.hudecekpetr.snowride.tree.highelements.FolderSuite;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

class ReloadChangesWindow extends Stage {
    private static ReloadChangesWindow activeWindow = null;
    private final Label lblInfo;
    private LinkedHashSet<Path> changedPaths = new LinkedHashSet<>();


    private ReloadChangesWindow() {
        lblInfo = new Label(getInfo());
        lblInfo.setWrapText(true);
        Button bReload = new Button("Reload from disk", new ImageView(Images.refresh));
        bReload.setOnAction(event1 -> reloadAll());
        Button bKeep = new Button("Keep what's in Snowride", new ImageView(Images.no));
        bKeep.setOnAction(actionEvent -> closeSelf());
        VBox vbAll = new VBox(15, lblInfo, bReload, bKeep);
        vbAll.setPadding(new Insets(5));
        vbAll.setAlignment(Pos.CENTER);
        HBox outer2 = new HBox(vbAll);
        outer2.setAlignment(Pos.CENTER);
        VBox outer1 = new VBox(outer2);
        outer1.setAlignment(Pos.CENTER);
        this.setScene(new Scene(outer1, 400, 300));
        this.setTitle("Changes from outside Snowride...");
        this.setAlwaysOnTop(true);
        this.getIcons().add(Images.snowflake);
        this.setOnHidden(event -> activeWindow = null);
    }

    /**
     * Creates this window if it doesn't exist or returns the existing window if it's already shown.
     */
    public static ReloadChangesWindow activateWindowIfNotActive() {
        if (activeWindow == null) {
            activeWindow = new ReloadChangesWindow();
            activeWindow.show();
        }
        return activeWindow;
    }

    /**
     * On the JavaFX thread, reloads changes from disk.
     */
    private void reloadAll() {
        MainForm mainForm = MainForm.INSTANCE;
        closeSelf();
        Set<Suite> reloadRequired = new HashSet<>();

        // Convert absolute paths to high elements:
        discoverElementsToBeReloadedRecursive(mainForm.getRootElement(), changedPaths, reloadRequired);
        changedPaths.clear();

        mainForm.projectLoad.progress.set(0);
        Holder<HighElement> deadButFocusedElement = new Holder<>(null);
        double progressPerFile = 1.0 / reloadRequired.size();

        for (Suite rl : reloadRequired) {
            // Remove the old instance from memory:
            rl.selfAndDescendantHighElements().forEachOrdered(he -> {
                mainForm.navigationStack.remove(he);
                if (mainForm.getFocusedElement() == he) {
                    deadButFocusedElement.setValue(he);
                }
            });

            // Reload the suite
            Suite remainingParent = rl.parent;
            Suite newElement;
            if (rl instanceof FolderSuite) {
                newElement = mainForm.gateParser.loadDirectory(((FolderSuite) rl).directoryPath, mainForm.projectLoad, progressPerFile);
            } else if (rl instanceof FileSuite) {
                try {
                    newElement = mainForm.gateParser.loadFile(((FileSuite) rl).file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                mainForm.projectLoad.success(progressPerFile);
            } else {
                throw new RuntimeException("The file '" + rl.getShortName() + "' is not a suite. This cannot happen.");
            }

            // Reanalyze.
            newElement.selfAndDescendantHighElements().forEachOrdered(he -> {
                if (he instanceof Suite) {
                    ((Suite) he).analyzeSemantics();
                }
            });
            if (remainingParent == null) {
                throw new RuntimeException("The suite '" + rl.getQualifiedName() + "' appears to have no parent.");
            }

            // Update the tree. Potentially CPU-expensive but hopefully not too many changes were made.
            remainingParent.replaceChildWithAnotherChild(rl, newElement);
        }

        // Remove the ghost high element from being loaded in the text edit and grid tab by reloading it.
        HighElement dead = deadButFocusedElement.getValue();
        if (dead != null) {
            Optional<HighElement> newVersionOfThisElement = mainForm.findTestByFullyQualifiedName(dead.getQualifiedName());
            if (newVersionOfThisElement.isPresent()) {
                mainForm.selectProgrammatically(newVersionOfThisElement.get());
            } else {
                mainForm.selectProgrammatically(mainForm.getRootDirectoryElement());
            }
        }
        mainForm.runTab.maybeRunNumberChanged();
        mainForm.projectLoad.progress.set(1);
    }

    private void discoverElementsToBeReloadedRecursive(HighElement currentElement, LinkedHashSet<Path> changedFiles, Set<Suite> outElementsToBeReloaded) {
        if (currentElement instanceof FileSuite) {
            Path path = ((FileSuite) currentElement).file.toPath();
            if (changedFiles.contains(path)) {
                outElementsToBeReloaded.add((Suite) currentElement);
            }
            // Never recurse into scenarios, it would be pointless.
            return;
        } else if (currentElement instanceof FolderSuite) {
            Path path = ((FolderSuite) currentElement).directoryPath.toPath();
            if (changedFiles.contains(path)) {
                outElementsToBeReloaded.add((Suite) currentElement);
                return; // Don't recurse if we're already reloading a folder
            }
        }
        currentElement.children.forEach(he -> discoverElementsToBeReloadedRecursive(he, changedFiles, outElementsToBeReloaded));
    }

    private void closeSelf() {
        this.close();
    }

    private String getInfo() {
        return Extensions.englishCount(changedPaths.size(), "file or folder was changed", "files or folders were changed") + " from outside Snowride (" +
                StringUtils.join(changedPaths.stream().limit(5).map(Path::getFileName).iterator(), ", ") + "). Reload them from disk?";
    }

    /**
     * Adds a path to the set of changes file and directories. Call from JavaFX thread only. Updates the text in the window as well.
     */
    public void addPath(Path absolutePathToChangedFile) {
        changedPaths.add(absolutePathToChangedFile);
        lblInfo.setText(getInfo());
    }
}

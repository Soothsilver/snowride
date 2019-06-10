package cz.hudecekpetr.snowride.filesystem;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ReloadChangesWindow extends Stage {
    private static ReloadChangesWindow activeWindow = null;
    private final Label lblInfo;
    private LinkedHashSet<Path> changedPaths = new LinkedHashSet<>();


    private ReloadChangesWindow() {
        lblInfo = new Label(getInfo());
        lblInfo.setWrapText(true);
        Button bReload = new Button("Reload from disk", new ImageView(Images.refresh));
        bReload.setOnAction(this::reloadAll);
        Button bKeep = new Button("Keep what's in Snowride", new ImageView(Images.no));
        bKeep.setOnAction(this::closeSelf);
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
        this.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                activeWindow = null;
            }
        });
    }

    public static ReloadChangesWindow activateWindowIfNotActive() {
        if (activeWindow == null) {
            activeWindow = new ReloadChangesWindow();
            activeWindow.show();
        }
        return activeWindow;
    }

    private void reloadAll(ActionEvent actionEvent) {
        closeSelf(actionEvent);
        Set<HighElement> reloadRequired = new HashSet<>();
        processChildrenRecursively(MainForm.INSTANCE.getProjectTree().getRoot().getValue(), changedPaths, reloadRequired);
        MainForm mainForm = MainForm.INSTANCE;
        changedPaths.clear();
        mainForm.projectLoad.progress.set(0);
        double progressPerFile = 1.0 / reloadRequired.size();
        for (HighElement rl : reloadRequired) {
            rl.selfAndDescendantHighElements().forEachOrdered(he -> {
                mainForm.navigationStack.remove(he);
                if (mainForm.getFocusedElement() == he) {
                    mainForm.selectProgrammatically(mainForm.getProjectTree().getRoot().getValue());
                }
            });
            FolderSuite remainingParent = (FolderSuite) rl.parent;
            Suite newElement = null;
            if (rl instanceof FolderSuite) {
                newElement = mainForm.gateParser.loadDirectory(((FolderSuite) rl).directoryPath, mainForm.projectLoad, progressPerFile);
            } else if (rl instanceof FileSuite) {
                try {
                    newElement = mainForm.gateParser.loadFile(((FileSuite) rl).file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                mainForm.projectLoad.success(progressPerFile);
            }
            newElement.selfAndDescendantHighElements().forEachOrdered(he -> {
                if (he instanceof Suite) {
                    ((Suite) he).analyzeSemantics();
                }
            });
            remainingParent.replaceChildWithAnotherChild(rl, newElement);
        }
        mainForm.runTab.maybeRunNumberChanged();
        mainForm.projectLoad.progress.set(1);
    }

    private void processChildrenRecursively(HighElement currentElement, LinkedHashSet<Path> changedFiles, Set<HighElement> outElementsToBeReloaded) {
        if (currentElement instanceof FileSuite) {
            Path path = ((FileSuite) currentElement).file.toPath();
            if (changedFiles.contains(path)) {
                outElementsToBeReloaded.add(currentElement);
            }
            // Never recurse into scenarios, it would be pointless.
            return;
        } else if (currentElement instanceof FolderSuite) {
            Path path = ((FolderSuite) currentElement).directoryPath.toPath();
            if (changedFiles.contains(path)) {
                outElementsToBeReloaded.add(currentElement);
                return; // Don't recurse if we're already reloading a folder
            }
        }
        currentElement.children.forEach(he -> {
            processChildrenRecursively(he, changedFiles, outElementsToBeReloaded);
        });
    }

    private void closeSelf(ActionEvent actionEvent) {
        this.close();
    }

    private String getInfo() {
        return Extensions.englishCount(changedPaths.size(), "file or folder", "files or folders") + " were changed from outside Snowride (" +
                StringUtils.join(changedPaths.stream().limit(5).map(Path::getFileName).iterator(), ", ") + "). Reload them from disk?";
    }

    public void addPath(Path absolutePathToChangedFile) {
        changedPaths.add(absolutePathToChangedFile);
        lblInfo.setText(getInfo());
    }
}

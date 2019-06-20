package cz.hudecekpetr.snowride.tree.highelements;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.filesystem.FilesystemWatcher;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.RobotFile;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FolderSuite extends Suite implements ISuite {
    public File directoryPath;
    private File initFile;

    public FolderSuite(File directoryPath, File initFile, String name, String contents, List<HighElement> children) {
        super(name, contents, children);
        this.imageView.setImage(getAutocompleteIcon());
        this.directoryPath = directoryPath;
        this.initFile = initFile;
        if (this.initFile != null) {
            reparse();
        }
    }

    @Override
    public void saveAll() throws IOException {
        if (unsavedChanges != LastChangeKind.PRISTINE) {
            ensureAnInitFileExists();
        }
        if (unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            this.applyText();
        } else if (unsavedChanges == LastChangeKind.STRUCTURE_CHANGED) {
            optimizeStructure();
            this.contents = serialize();
        }
        // Save folders and suites below
        for (HighElement child : children) {
            child.saveAll();
        }
        if (this.unsavedChanges != LastChangeKind.PRISTINE) {
            System.out.println("SaveAll: [initfile] " + this.getShortName());
            FilesystemWatcher.getInstance().ignoreNextChangeOf(directoryPath.toPath());
            FilesystemWatcher.getInstance().ignoreNextChangeOf(initFile.toPath());
            FileUtils.write(initFile, contents, "utf-8");
            this.unsavedChanges = LastChangeKind.PRISTINE;
            for (HighElement child : children) {
                if (child instanceof Scenario) {
                    child.unsavedChanges = LastChangeKind.PRISTINE;
                    child.refreshToString();
                }
            }
            refreshToString();
        }
    }

    private void ensureAnInitFileExists() {
        try {
            if (initFile == null) {
                initFile = directoryPath.toPath().resolve("__init__.robot").toAbsolutePath().toFile();
                FilesystemWatcher.getInstance().ignoreNextChangeOf(initFile.toPath());
                if (!initFile.createNewFile()) {
                    throw new RuntimeException("Could not create __init__.robot");
                }
            }
            if (fileParsed == null) {
                fileParsed = new RobotFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSelf(MainForm mainForm) {
        FilesystemWatcher.getInstance().ignoreNextChangeOf(this.directoryPath.toPath());
        if (this.directoryPath.delete()) {
            this.dead = true;
            this.parent.dissociateSelfFromChild(this);
        } else {
            ButtonType deleteType = new ButtonType("Delete folder and all files inside");
            if (new Alert(Alert.AlertType.CONFIRMATION, "Could not delete folder '" + this.directoryPath.getName() + "' probably because it's not empty." +
                    " Delete it recursively?", deleteType, new ButtonType("No")).showAndWait().orElse(ButtonType.NO)
                    == deleteType) {
                try {
                    FilesystemWatcher.getInstance().ignoreNextChangeOf(this.directoryPath.toPath());
                    FileUtils.deleteDirectory(this.directoryPath);
                    this.selfAndDescendantHighElements().forEach(he -> he.dead = true);
                    this.children.clear();
                    this.treeNode.getChildren().clear();
                    this.parent.dissociateSelfFromChild(this);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void renameSelfTo(String newName, MainForm mainForm) {
        File oldFile = this.directoryPath;
        File newFile = this.directoryPath.getParentFile().toPath().resolve(newName).toFile();
        FilesystemWatcher.getInstance().ignoreNextChangeOf(oldFile.toPath());
        FilesystemWatcher.getInstance().ignoreNextChangeOf(newFile.toPath());
        if (this.directoryPath.renameTo(newFile)) {
            this.shortNameProperty.set(Extensions.toPrettyName(newName));
            this.directoryPath = newFile;
            if (this.initFile != null) {
                this.initFile = Extensions.changeAncestorTo(initFile, oldFile, newFile);
            }
            for (HighElement child : this.children) {
                child.ancestorRenamed(oldFile, newFile);
            }
        } else {
            throw new RuntimeException("Renaming failed.");
        }
        refreshToString();
    }


    @Override
    public void markAsStructurallyChanged(MainForm mainForm) {
        this.analyzeSemantics();
        mainForm.changeOccurredTo(this, LastChangeKind.STRUCTURE_CHANGED);
    }

    @Override
    protected void ancestorRenamed(File oldFile, File newFile) {
        this.directoryPath = Extensions.changeAncestorTo(this.directoryPath, oldFile, newFile);
        if (this.initFile != null) {
            this.initFile = Extensions.changeAncestorTo(this.initFile, oldFile, newFile);
        }
    }


    public RobotFile getInitFileParsed() {
        return fileParsed;
    }

    @Override
    public Image getAutocompleteIcon() {
        return isResourceOnly() ? Images.cogfolderIcon : Images.folderIcon;
    }

    @Override
    public String getAutocompleteText() {
        return "[folder] " + toString();
    }

    @Override
    public String getQuickDocumentationCaption() {
        return toString();
    }


    @Override
    public String getItalicsSubheading() {
        return "Folder";
    }

    @Override
    public Scenario createNewChild(String name, boolean asTestCase, MainForm mainForm) {
        this.applyText();
        if (asTestCase) {
            throw new RuntimeException("Folders can't contain test cases.");
        }
        ensureAnInitFileExists();
        if (this.fileParsed.errors.size() > 0) {
            throw new RuntimeException("You can't create a child suite, test or keyword because there are parse errors in the file.");
        }
        Scenario newKeyword = new Scenario(new Cell(name, "", null), false, new ArrayList<>());
        newKeyword.parent = this;
        this.fileParsed.findOrCreateKeywordsSection().addScenario(newKeyword);
        this.children.add(newKeyword);
        this.treeNode.getChildren().add(newKeyword.treeNode);
        this.unsavedChanges = LastChangeKind.STRUCTURE_CHANGED;
        mainForm.selectProgrammaticallyAndRememberInHistory(newKeyword);
        return newKeyword;
    }
}

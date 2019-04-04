package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FolderSuite extends Suite implements ISuite {
    public File directoryPath;
    private File initFile;
    private RobotFile initFileParsed;

    public FolderSuite(File directoryPath, File initFile, RobotFile initFileParsed, String name, String contents, List<HighElement> children) {
        super(name, contents, children);
        this.imageView.setImage(Images.folderIcon);
        this.directoryPath = directoryPath;
        this.initFile = initFile;
        this.initFileParsed = initFileParsed;
    }

    @Override
    public void saveAll() throws IOException {
        if (unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            if (initFile == null) {
                initFile = directoryPath.toPath().resolve("__init__.robot").toAbsolutePath().toFile();
                initFile.createNewFile();
                initFileParsed = new RobotFile();
            }
            this.applyAndValidateText();
        }
        // Save folders and suites below
        for (HighElement child : children) {
            child.saveAll();
        }
        if (this.unsavedChanges != LastChangeKind.PRISTINE) {
            System.out.println("SaveAll: [initfile] " + this.shortName);
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

    @Override
    public void deleteSelf(MainForm mainForm) {
        if (this.directoryPath.delete()) {
            this.dead = true;
            this.parent.dissociateSelfFromChild(this);
        } else {
            ButtonType deleteType = new ButtonType("Delete folder and all files inside");
            if (new Alert(Alert.AlertType.CONFIRMATION, "Could not delete folder '" + this.directoryPath.getName() + "' probably because it's not empty." +
                    " Delete it recursively?", deleteType, new ButtonType("No")).showAndWait().orElse(ButtonType.NO)
                    == deleteType) {
                try {
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
        if (this.directoryPath.renameTo(newFile)) {
            this.shortName = newName;
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
    public void applyAndValidateText() {
        // Apply
        if (this.areTextChangesUnapplied) {
            reparse();
            this.areTextChangesUnapplied = false;
        }

        // Validate
        if (initFileParsed != null && initFileParsed.errors.size() > 0) {
            throw new RuntimeException("There are parse errors.");
        }
    }

    @Override
    public void markAsStructurallyChanged(MainForm mainForm) {
        throw new NotImplementedException("not yet");
    }

    @Override
    protected void optimizeStructure() {
        // not yet
    }

    @Override
    protected void ancestorRenamed(File oldFile, File newFile) {
        this.directoryPath = Extensions.changeAncestorTo(this.directoryPath, oldFile, newFile);
        if (this.initFile != null) {
            this.initFile = Extensions.changeAncestorTo(this.initFile, oldFile, newFile);
        }
    }

    public void reparse() {
        if (contents != null) {
            this.children.removeIf(he -> he instanceof Scenario);
            this.treeNode.getChildren().removeIf(ti -> ti.getValue() instanceof Scenario);
            RobotFile parsed = GateParser.parse(contents);
            this.initFileParsed = parsed;
            this.reparseResources(parsed);
            this.addChildren(parsed.getHighElements());
            this.analyzeSemantics();
        }
    }


    public RobotFile getInitFileParsed() {
        return initFileParsed;
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.folderIcon;
    }

    @Override
    public String getAutocompleteText() {
        return "[folder] " + toString();
    }

    @Override
    public String getFullDocumentation() {
        return this.directoryPath.getAbsolutePath();
    }

    @Override
    public String getItalicsSubheading() {
        return "Folder";
    }

    @Override
    public void createNewChild(String name, boolean asTestCase) {
        this.applyAndValidateText();
        if (asTestCase) {
            throw new RuntimeException("Folders can't contain test cases.");
        }
        if (this.initFile == null) {
            throw new NotImplementedException("This folder doesn't have an initfile yet and creating one isn't implemented yet.");
        }
        Scenario newKeyword = new Scenario(new Cell(name, "", null), false, new ArrayList<>());
        this.initFileParsed.findOrCreateKeywordsSection().addScenario(newKeyword);
        this.children.add(newKeyword);
        this.treeNode.getChildren().add(newKeyword.treeNode);
        this.unsavedChanges = LastChangeKind.STRUCTURE_CHANGED;
    }

    public void analyzeSemantics() {
        if (this.initFileParsed != null) {
            this.initFileParsed.analyzeSemantics(this);
        }
    }
}

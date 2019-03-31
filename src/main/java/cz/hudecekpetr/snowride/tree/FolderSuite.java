package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.lexer.Cell;
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

public class FolderSuite extends HighElement implements ISuite {
    public final File directoryPath;
    private File initFile;
    private final RobotFile initFileParsed;

    public FolderSuite(File directoryPath, File initFile, RobotFile initFileParsed, String name, String contents, List<HighElement> children) {
        super(name, contents, children);
        this.imageView.setImage(Images.folderIcon);
        this.directoryPath = directoryPath;
        this.initFile = initFile;
        this.initFileParsed = initFileParsed;
    }

    @Override
    public void saveAll() throws IOException {
        if (initFile != null && unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            unsavedChanges = LastChangeKind.PRISTINE;
            System.out.println("SaveAll: [initfile] " + this.shortName);
            FileUtils.write(initFile, contents, "utf-8");
            refreshToString();
        }
        for (HighElement child : children) {
            child.saveAll();
        }
    }

    @Override
    public void deleteSelf() {
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
    public void renameSelfTo(String newName) {
        refreshToString();
        throw new RuntimeException("Renaming folders not yet implemented.");
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

    public void reparse() {
        // TODO reparse not yet implemented
        throw new RuntimeException("Not yet done.");
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
}

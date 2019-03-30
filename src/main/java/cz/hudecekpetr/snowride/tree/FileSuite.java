package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileSuite extends HighElement implements ISuite {
    public File file;
    private final RobotFile fileParsed;

    public FileSuite(File file, String name, String contents,  RobotFile fileParsed) {
        super(name, contents, fileParsed.getHighElements());
        this.file = file;
        this.fileParsed = fileParsed;
        for (HighElement scenario : fileParsed.getHighElements()) {
            if (scenario instanceof Scenario) {
                ((Scenario)scenario).parent = this;
            }
        }
    }

    @Override
    public String toString() {
        return "[file] " + super.toString();
    }

    @Override
    public void saveAll() throws IOException {
        if (this.unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            this.unsavedChanges = LastChangeKind.PRISTINE;
            System.out.println("SaveAll: " + this.shortName);
            FileUtils.write(file, contents, "utf-8");
            refreshToString();
        }
    }

    @Override
    public void deleteSelf() {
        if (this.file.delete()) {
            this.dead = true;
            this.parent.dissociateSelfFromChild(this);
        } else {
            throw new RuntimeException("Could not delete file '" + this.file.getName() + "'.");
        }
    }

    public FolderSuite getParentAsFolder() {
        return (FolderSuite) parent;
    }

    @Override
    public void renameSelfTo(String newName) {
        File selfsParent = getParentAsFolder().directoryPath;
        File currentFile = this.file;
        File newFile = selfsParent.toPath().resolve(newName + ".robot").toFile();
        if (currentFile.renameTo(newFile)) {
            this.shortName = newName;
            this.file = newFile;
        } else {
            throw new RuntimeException("Could not rename the file suite '" + this.shortName + "'.");
        }
    }

    @Override
    public void applyAndValidateText() {
        // Apply
        if (this.areTextChangesUnapplied) {
            reparse();
            this.areTextChangesUnapplied = false;
        }

        // Validate
        if (fileParsed != null && fileParsed.errors.size() > 0) {
            throw new RuntimeException("There are parse errors.");
        }
    }

    public void reparse() {
        // TODO
        throw new NotImplementedException("Not yet donee");
    }

    public RobotFile getFileParsed() {
        return fileParsed;
    }

    public String serialize() {
        return fileParsed.serialize();
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.fileIcon;
    }

    @Override
    public void createNewChild(String name, boolean asTestCase) {
        this.applyAndValidateText();
        Scenario scenario = new Scenario(new Cell(name, ""), asTestCase, new ArrayList<>());
        scenario.parent = this;
        if (asTestCase) {
            this.fileParsed.findOrCreateTestCasesSection().addScenario(scenario);
        } else {
            this.fileParsed.findOrCreateKeywordsSection().addScenario(scenario);
        }
        this.children.add(scenario);
        this.treeNode.getChildren().add(scenario.treeNode);
    }
}

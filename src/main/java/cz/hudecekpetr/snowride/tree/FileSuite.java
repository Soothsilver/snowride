package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.UserKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileSuite extends HighElement implements ISuite {
    public File file;
    public RobotFile fileParsed;

    public FileSuite(File file, String name, String contents) {
        super(name, contents, new ArrayList<>());
        this.file = file;
        this.fileParsed = GateParser.parse(contents);
        this.addChildren(fileParsed.getHighElements());
    }

    @Override
    public String toString() {
        return "[file] " + super.toString();
    }

    @Override
    public void saveAll() throws IOException {
        if (this.unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            this.applyAndValidateText();
            System.out.println("SaveAll: " + this.shortName);
        } else if (this.unsavedChanges == LastChangeKind.STRUCTURE_CHANGED) {
            this.contents = serialize();
            this.optimizeStructure();
            System.out.println("SaveAll structurally: " + this.shortName);
        }
        if (this.unsavedChanges != LastChangeKind.PRISTINE) {
            FileUtils.write(file, contents, "utf-8");
            this.unsavedChanges = LastChangeKind.PRISTINE;
            for (HighElement child : children) {
                child.unsavedChanges = LastChangeKind.PRISTINE;
                child.refreshToString();
            }
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
        refreshToString();
    }

    public Stream<UserKeyword> getSelfKeywords() {
        return this.children.stream().filter(he -> (he instanceof Scenario) && !((Scenario)he).isTestCase())
                .map(he -> {
                   Scenario s = (Scenario)he;
                   return UserKeyword.fromScenario(s);
                });
    }
    public Stream<IKnownKeyword> getKeywordsPermissibleInSuite() {
        return Stream.concat(getSelfKeywords(), ExternalLibrary.builtIn.keywords.stream());
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

    @Override
    public void markAsStructurallyChanged(MainForm mainForm) {
        mainForm.changeOccurredTo(this, LastChangeKind.STRUCTURE_CHANGED);
    }

    @Override
    protected void optimizeStructure() {
        // not yet
        for (HighElement child : children) {
            child.optimizeStructure();
        }
    }

    public void reparse() {
        this.children.clear();
        this.treeNode.getChildren().clear();
        RobotFile parsed = GateParser.parse(contents);
        this.fileParsed = parsed;
        this.addChildren(parsed.getHighElements());
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
        Scenario scenario = new Scenario(new Cell(name, "", null), asTestCase, new ArrayList<>());
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

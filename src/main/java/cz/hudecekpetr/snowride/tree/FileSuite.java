package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileSuite extends Suite implements ISuite {
    public File file;

    public FileSuite(File file, String name, String contents) {
        super(name, contents, new ArrayList<>());
        this.file = file;
        this.reparse();
    }

    @Override
    public void saveAll() throws IOException {
        if (this.unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            this.applyText();
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
    public void deleteSelf(MainForm mainForm) {
        if (this.file.delete()) {
            this.dead = true;
            this.parent.dissociateSelfFromChild(this);
        } else {
            throw new RuntimeException("Could not delete file '" + this.file.getName() + "'.");
        }
    }

    private FolderSuite getParentAsFolder() {
        return (FolderSuite) parent;
    }

    @Override
    public void renameSelfTo(String newName, MainForm mainForm) {
        File selfsParent = getParentAsFolder().directoryPath;
        File currentFile = this.file;
        File newFile = selfsParent.toPath().resolve(newName + ".robot").toFile();
        if (currentFile.renameTo(newFile)) {
            this.shortName = Extensions.toPrettyName(newName);
            this.file = newFile;
        } else {
            throw new RuntimeException("Could not rename the file suite '" + this.shortName + "'.");
        }
        refreshToString();
    }



    @Override
    public void markAsStructurallyChanged(MainForm mainForm) {
        this.analyzeSemantics();
        mainForm.changeOccurredTo(this, LastChangeKind.STRUCTURE_CHANGED);
    }

    @Override
    protected void optimizeStructure() {
        // not yet
        for (HighElement child : children) {
            child.optimizeStructure();
        }
    }

    @Override
    protected void ancestorRenamed(File oldFile, File newFile) {
        this.file = Extensions.changeAncestorTo(this.file, oldFile, newFile);
    }

    @Override
    public Suite asSuite() {
        return this;
    }

    @Override
    public String getQuickDocumentationCaption() {
        return toString();
    }

    public String serialize() {
        return fileParsed.serialize();
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.fileIcon;
    }

    @Override
    public String getAutocompleteText() {
        return "[file] " + toString();
    }



    @Override
    public String getItalicsSubheading() {
        return "File suite or resource file";
    }

    @Override
    public void createNewChild(String name, boolean asTestCase, MainForm mainForm) {
        this.applyText();
        if (this.fileParsed.errors.size() > 0) {
            throw new RuntimeException("You can't create a child suite, test or keyword because there are parse errors in the file.");
        }
        Scenario scenario = new Scenario(new Cell(name, "", null), asTestCase, new ArrayList<>());
        scenario.parent = this;
        if (asTestCase) {
            this.fileParsed.findOrCreateTestCasesSection().addScenario(scenario);
        } else {
            this.fileParsed.findOrCreateKeywordsSection().addScenario(scenario);
        }
        this.children.add(scenario);
        this.treeNode.getChildren().add(scenario.treeNode);
        mainForm.selectProgrammaticallyAndRememberInHistory(scenario);
    }
}

package cz.hudecekpetr.snowride.tree.highelements;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.filesystem.FilesystemWatcher;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.tree.Cell;
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
        this.imageView.setImage(getAutocompleteIcon());
        this.reparse();
    }

    @Override
    public void saveAll() throws IOException {
        if (this.unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            this.applyText();
            System.out.println("SaveAll: " + this.getShortName());
        } else if (this.unsavedChanges == LastChangeKind.STRUCTURE_CHANGED) {
            this.optimizeStructure();
            this.contents = serialize();
            System.out.println("SaveAll structurally: " + this.getShortName());
        }
        if (this.unsavedChanges != LastChangeKind.PRISTINE) {
            FileUtils.write(file, contents, "utf-8");
            pristineContents = contents;
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
            this.shortNameProperty.set(Extensions.toPrettyName(newName));
            this.shortNameAsOnDisk = newName;
            this.file = newFile;
        } else {
            throw new RuntimeException("Could not rename the file suite '" + this.getShortName() + "'.");
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
        this.file = Extensions.changeAncestorTo(this.file, oldFile, newFile);
    }

    @Override
    public String getQuickDocumentationCaption() {
        return toString();
    }


    @Override
    public Image getAutocompleteIcon() {
        return isResourceOnly() ? Images.cogfileIcon : Images.fileIcon;
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
    public Scenario createNewChild(String name, boolean asTestCase, MainForm mainForm, HighElement justAfter) {
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
        int indexOfOld = this.children.indexOf(justAfter) + 1;
        if (indexOfOld == 0) {
            indexOfOld = this.children.size();
        }
        this.children.add(indexOfOld, scenario);
        this.treeNode.getChildren().add(indexOfOld, scenario.treeNode);
        mainForm.selectProgrammaticallyAndRememberInHistory(scenario);
        return scenario;
    }
}

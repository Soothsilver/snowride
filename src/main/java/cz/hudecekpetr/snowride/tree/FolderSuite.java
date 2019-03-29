package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FolderSuite extends HighElement {
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
    public String toString() {
        return "[folder] " + super.toString();
    }

    @Override
    public void saveAll() throws IOException {
        if (initFile != null && changedByUser) {
            changedByUser = false;
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
                    "Delete it recursively?", deleteType, new ButtonType("No")).showAndWait().orElse(ButtonType.NO)
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


    public RobotFile getInitFileParsed() {
        return initFileParsed;
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.folderIcon;
    }
}

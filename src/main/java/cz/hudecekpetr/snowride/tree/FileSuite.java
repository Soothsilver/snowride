package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileSuite extends HighElement {
    public final File file;
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
        if (this.changedByUser) {
            this.changedByUser = false;
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
}

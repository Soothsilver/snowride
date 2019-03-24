package cz.hudecekpetr.snowride.tree;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileSuite extends HighElement {
    private final File file;
    private final RobotFile fileParsed;

    public FileSuite(File file,String name, String contents,  RobotFile fileParsed) {
        super(name, contents, fileParsed.getHighElements());
        this.file = file;
        this.fileParsed = fileParsed;
    }

    @Override
    public String toString() {
        return "[file] " + super.toString();
    }

    @Override
    public void saveAll() throws IOException {
        if (this.changedByUser) {
            this.changedByUser = false;
            System.out.println("SaveAll: " + this.name);
            FileUtils.write(file, contents, "utf-8");
            refreshToString();
        }
    }

    public RobotFile getFileParsed() {
        return fileParsed;
    }

    public String serialize() {
        return fileParsed.serialize();
    }
}

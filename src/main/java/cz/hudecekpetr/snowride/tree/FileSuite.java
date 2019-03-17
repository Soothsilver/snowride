package cz.hudecekpetr.snowride.tree;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileSuite extends HighElement {
    private final File file;

    public FileSuite(File file, String name, String contents, ArrayList<HighElement> children) {
        super(name, contents, children);
        this.file = file;
    }

    @Override
    public String toString() {
        return "[file] " + name;
    }

    @Override
    public void saveAll() throws IOException {
        if (this.changedByUser) {
            this.changedByUser = false;
            FileUtils.write(file, contents, "utf-8");
        }
    }
}

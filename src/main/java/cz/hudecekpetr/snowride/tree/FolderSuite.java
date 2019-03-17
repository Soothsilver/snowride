package cz.hudecekpetr.snowride.tree;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FolderSuite extends HighElement {
    private File initFile;

    public FolderSuite(File initFile, String name, String contents, List<HighElement> children) {
        super(name, contents, children);
        this.initFile = initFile;
    }

    @Override
    public String toString() {
        return "[folder] " + name;
    }

    @Override
    public void saveAll() throws IOException {
        if (initFile != null && changedByUser) {
            changedByUser = false;
            FileUtils.write(initFile, contents, "utf-8");
        }
        for (HighElement child : children) {
            child.saveAll();
        }
    }
}

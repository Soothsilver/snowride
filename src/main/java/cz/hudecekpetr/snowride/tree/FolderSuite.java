package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.ui.Images;
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
            System.out.println("SaveAll: [initfile] " + this.name);
            FileUtils.write(initFile, contents, "utf-8");
            refreshToString();
        }
        for (HighElement child : children) {
            child.saveAll();
        }
    }


    public RobotFile getInitFileParsed() {
        return initFileParsed;
    }
}

package cz.hudecekpetr.snowride.tree;

import com.google.common.collect.Lists;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UltimateRoot extends Suite {

    public UltimateRoot(FolderSuite rootDirectory, ExternalResourcesElement externalResourcesElement) {
        super("Ultimate root element", null, Lists.newArrayList(rootDirectory, externalResourcesElement));
        this.imageView.setImage(Images.internet);
        rootDirectory.parent = this;
        externalResourcesElement.parent = this;
    }

    @Override
    public void saveAll() throws IOException {
        for (HighElement child : children) {
            child.saveAll();
        }
    }

    @Override
    public void deleteSelf(MainForm mainForm) {
        // impossible
    }

    @Override
    public void renameSelfTo(String newName, MainForm mainForm) {
        // impossible
    }

    @Override
    public void markAsStructurallyChanged(MainForm mainForm) {
        // impossible
    }

    @Override
    protected void ancestorRenamed(File oldFile, File newFile) {
        // impossible
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.no;
    }

    @Override
    public String getItalicsSubheading() {
        return "Hidden root node";
    }

    @Override
    public String getAutocompleteText() {
        return "Ultimate root element";
    }

    @Override
    public void applyText() {
        // Cannot happen
    }

    @Override
    public void updateTagsForSelfAndChildren() {
        this.forceTagsCumulative.clear();
        this.defaultTags.clear();

        for (HighElement child : this.children) {
            child.updateTagsForSelfAndChildren();
        }
    }

    @Override
    public String getInvariantName() {
        return "";
    }

    public FolderSuite getRootDirectory() {
        return ((FolderSuite) children.get(0));
    }

    public ExternalResourcesElement getExternalResourcesElement() {
        return ((ExternalResourcesElement) children.get(1));
    }
}

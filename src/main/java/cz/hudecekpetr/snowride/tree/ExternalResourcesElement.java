package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ExternalResourcesElement extends Suite {

    public ExternalResourcesElement(List<HighElement> children) {
        super("External resources", null, children);
        for (HighElement child : children) {
            child.parent = this;
        }
        this.imageView.setImage(Images.internet);
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

    @Override
    public Scenario createNewChild(String name, boolean asTestCase, MainForm mainForm) {
        throw new RuntimeException("You can't add children to the external resources node.");
    }
}

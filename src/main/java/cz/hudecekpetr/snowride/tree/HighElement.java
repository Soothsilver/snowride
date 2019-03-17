package cz.hudecekpetr.snowride.tree;

import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class HighElement {
    public final String name;
    public String contents;
    public final List<HighElement> children;
    public TreeItem<HighElement> treeNode;
    public boolean changedByUser;

    public HighElement(String name, String contents, List<HighElement> children) {
        treeNode = new TreeItem<>(this);
        this.name = name;
        this.contents = contents;
        this.children = children;
        for (HighElement child : children) {
            treeNode.getChildren().add(child.treeNode);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract void saveAll() throws IOException;
}

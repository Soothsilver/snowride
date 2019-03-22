package cz.hudecekpetr.snowride.tree;

import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        if (changedByUser) {
            return "[changed] " + name;
        }
        return name;
    }

    public abstract void saveAll() throws IOException;

    protected void refreshToString() {
        this.treeNode.setValue(null);
        this.treeNode.setValue(this);
    }
    public Stream<HighElement> selfAndDescendantHighElements() {
        return Stream.concat(
                Stream.of(this),
                children.stream().flatMap(HighElement::selfAndDescendantHighElements)
        );
    }
}

package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class HighElement implements IAutocompleteOption {
    public String shortName;
    public final ImageView imageView;
    public final CheckBox checkbox;
    private HBox graphic;
    public String contents;
    public final List<HighElement> children;
    public TreeItem<HighElement> treeNode;
    public LastChangeKind unsavedChanges = LastChangeKind.PRISTINE;
    public boolean areTextChangesUnapplied = false;
    public HighElement parent;
    public boolean dead;

    public HighElement(String shortName, String contents, List<HighElement> children) {
        graphic = new HBox();
        imageView = new ImageView(Images.fileIcon);
        checkbox = new CheckBox();
        checkbox.setVisible(false);
        checkbox.managedProperty().bind(checkbox.visibleProperty());
        graphic.getChildren().add(imageView);
        graphic.getChildren().add(checkbox);
        treeNode = new TreeItem<>(this, this.graphic);
        this.shortName = shortName;
        this.contents = contents;
        this.children = new ArrayList<>();
        addChildren(children);
    }

    public void addChildren(List<HighElement> children) {
        this.children.addAll(children);
        for (HighElement child : children) {
            treeNode.getChildren().add(child.treeNode);
            if (child instanceof Scenario) {
                child.parent = this;
            }
        }
    }

    @Override
    public String toString() {
        if (unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            return "[text changed] " + shortName;
        }
        if (unsavedChanges == LastChangeKind.STRUCTURE_CHANGED) {
            return "[structure changed] " + shortName;
        }
        return shortName;
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

    public String getQualifiedName() {
        if (parent == null) {
            return shortName;
        } else {
            return parent.getQualifiedName() + "." + shortName;
        }
    }

    public abstract void deleteSelf();

    protected void dissociateSelfFromChild(HighElement child) {
        this.children.remove(child);
        this.treeNode.getChildren().remove(child.treeNode);
    }

    public abstract void renameSelfTo(String newName);

    public abstract void applyAndValidateText();
}

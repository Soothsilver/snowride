package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.SnowrideError;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.fx.AggregatedObservableArrayList;
import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.ObservableMultiset;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class HighElement implements IAutocompleteOption {
    public String shortName;
    public final ImageView imageView;
    public final CheckBox checkbox;
    public ObservableList<SnowrideError> selfErrors = FXCollections.observableArrayList();
    public ObservableList<SnowrideError> allErrorsRecursive;
    private ObservableMultiset<SnowrideError> allErrorsRecursiveSource = new ObservableMultiset<>();
    private HBox graphic;
    public String contents;
    public final ObservableList<HighElement> children;
    public TreeItem<HighElement> treeNode;
    public LastChangeKind unsavedChanges = LastChangeKind.PRISTINE;
    public boolean areTextChangesUnapplied = false;
    public HighElement parent;
    public boolean dead;
    protected String semanticsDocumentation;

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
        this.children = FXCollections.observableArrayList();
        this.children.addListener(new ListChangeListener<HighElement>() {
            @Override
            public void onChanged(Change<? extends HighElement> c) {
                while (c.next()) {
                    for (HighElement added : c.getAddedSubList()) {
                        System.out.println("Adding [" + added + "] to [" + HighElement.this + "].");
                        allErrorsRecursiveSource.appendList(added.allErrorsRecursive);
                    }
                    for (HighElement removed : c.getRemoved()) {
                        allErrorsRecursiveSource.removeList(removed.allErrorsRecursive);
                    }
                }
            }
        });
        System.out.println("Adding [" + this + ".self] to [" + this + "].");
        allErrorsRecursiveSource.appendList(this.selfErrors);
        allErrorsRecursive = allErrorsRecursiveSource;//.getAggregatedList();
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

    public abstract void deleteSelf(MainForm mainForm);

    protected void dissociateSelfFromChild(HighElement child) {
        this.children.remove(child);
        this.treeNode.getChildren().remove(child.treeNode);
    }

    public abstract void renameSelfTo(String newName, MainForm mainForm);

    public abstract void applyText();

    public abstract void markAsStructurallyChanged(MainForm mainForm);

    protected abstract void optimizeStructure();


    public String getDocumentation() {
        return semanticsDocumentation;
    }

    protected abstract void ancestorRenamed(File oldFile, File newFile);

    public abstract Suite asSuite();



    @Override
    public String getFullDocumentation() {
        return "*Qualified name:* " + this.getQualifiedName() +
                (!StringUtils.isBlank(this.semanticsDocumentation) ? ("\n" + "*Documentation:* " + this.semanticsDocumentation) : "");
    }
}

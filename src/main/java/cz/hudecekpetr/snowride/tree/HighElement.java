package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.SnowrideError;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.ObservableMultiset;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.List;
import java.util.stream.Stream;

public abstract class HighElement implements IAutocompleteOption {
    public final ImageView imageView;
    public final CheckBox checkbox;
    public final ObservableList<HighElement> children;
    public final ObservableMultiset<HighElement> childrenRecursively = new ObservableMultiset<>();
    public SimpleStringProperty shortNameProperty = new SimpleStringProperty();
    public ObservableList<SnowrideError> selfErrors = FXCollections.observableArrayList();
    public ObservableList<SnowrideError> allErrorsRecursive;
    public String contents;
    public TreeItem<HighElement> treeNode;
    public LastChangeKind unsavedChanges = LastChangeKind.PRISTINE;
    public boolean areTextChangesUnapplied = false;
    public Suite parent;
    public boolean dead;
    protected String semanticsDocumentation;
    private ObservableMultiset<SnowrideError> allErrorsRecursiveSource = new ObservableMultiset<>();
    private String invariantName;

    public HighElement(String shortName, String contents, List<HighElement> children) {
        HBox graphic = new HBox();
        imageView = new ImageView(Images.fileIcon);
        checkbox = new CheckBox();
        checkbox.setVisible(false);
        checkbox.managedProperty().bind(checkbox.visibleProperty());
        checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                MainForm.INSTANCE.runTab.maybeRunNumberChanged();
            }
        });
        graphic.getChildren().add(imageView);
        graphic.getChildren().add(checkbox);
        treeNode = new TreeItem<>(this, graphic);
        this.shortNameProperty.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                invariantName = Extensions.toInvariant(newValue);
            }
        });
        this.shortNameProperty.set(shortName);
        this.contents = contents;
        this.children = FXCollections.observableArrayList();
        childrenRecursively.appendList(this.children);
        this.children.addListener(new ListChangeListener<HighElement>() {
            @Override
            public void onChanged(Change<? extends HighElement> c) {
                while (c.next()) {
                    for (HighElement added : c.getAddedSubList()) {
                        allErrorsRecursiveSource.appendList(added.allErrorsRecursive);
                        childrenRecursively.appendList(added.childrenRecursively);
                    }
                    for (HighElement removed : c.getRemoved()) {
                        allErrorsRecursiveSource.removeList(removed.allErrorsRecursive);
                        childrenRecursively.removeList(removed.childrenRecursively);
                    }
                }
            }
        });
        allErrorsRecursiveSource.appendList(this.selfErrors);
        allErrorsRecursive = allErrorsRecursiveSource;//.getAggregatedList();
        addChildren(children);
    }

    public void addChildren(List<HighElement> children) {
        this.children.addAll(children);
        for (HighElement child : children) {
            treeNode.getChildren().add(child.treeNode);
            if (child instanceof Scenario) {
                child.parent = (Suite) this;
            }
        }
    }

    @Override
    public String toString() {
        if (unsavedChanges == LastChangeKind.TEXT_CHANGED) {
            return "[text changed] " + getShortName();
        }
        if (unsavedChanges == LastChangeKind.STRUCTURE_CHANGED) {
            return "[structure changed] " + getShortName();
        }
        return getShortName();
    }

    public String getShortName() {
        return shortNameProperty.get();
    }

    public abstract void saveAll() throws IOException;

    public abstract void updateTagsForSelfAndChildren();

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
            return getShortName();
        } else {
            return parent.getQualifiedName() + "." + getShortName();
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

    public String getInvariantName() {
        return invariantName;
    }
}

package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TextEditTab {

    private final TextArea tbTextEdit;
    private VBox editorPane;
    private final Node warningPane;
    private MainForm mainForm;
    private Tab tabTextEdit;
    private HighElement lastLoaded;

    public TextEditTab(MainForm mainForm) {
        this.mainForm = mainForm;
        tbTextEdit = new TextArea();
        Label noEditForTestCases = new Label("You cannot edit test cases this way. Use the grid editor instead.");
        Button bOrSwitch = new Button("...or edit the entire suite as text");
        bOrSwitch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mainForm.selectProgrammatically(mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue().parent);
            }
        });
        VBox vboxWarning = new VBox(noEditForTestCases, bOrSwitch);
        HBox outer2 = new HBox(vboxWarning);
        outer2.setAlignment(Pos.CENTER);
        VBox outer1 = new VBox(outer2);
        outer1.setAlignment(Pos.CENTER);
        warningPane = outer1;
    }

    public Tab createTab() {
        tbTextEdit.setPromptText("This will display text...");
        tbTextEdit.setFont(MainForm.TEXT_EDIT_FONT);
        tbTextEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!mainForm.switchingTextEditContents) {
                    HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
                    whatChanged.areTextChangesUnapplied = true;
                    whatChanged.contents = newValue;
                    mainForm.changeOccurredTo(whatChanged, LastChangeKind.TEXT_CHANGED);
                }
            }
        });
        Button bApply = new Button("Apply changes");
        Label lblInfo = new Label("Changes are applied automatically if you switch to another tab, test case, or suite; or if you save.");
        bApply.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
                whatChanged.applyText();
                if (whatChanged.asSuite().fileParsed != null && whatChanged.asSuite().fileParsed.errors.size() > 0) {
                    throw new RuntimeException("There are parse errors. See the other tabs for details.");
                }
            }
        });
        HBox hBox = new HBox(2, bApply, lblInfo);
        hBox.setPadding(new Insets(2));
        hBox.setAlignment(Pos.CENTER_LEFT);
        VBox textVBox = new VBox(hBox, tbTextEdit);
        VBox.setVgrow(tbTextEdit, Priority.ALWAYS);
        editorPane = textVBox;
        tabTextEdit = new Tab("Edit entire file as text", textVBox);
        tabTextEdit.setClosable(false);
        return tabTextEdit;
    }

    public void loadElement(HighElement value) {
        if (value != null && value.unsavedChanges == LastChangeKind.STRUCTURE_CHANGED && value instanceof Suite) {
            Suite asSuite = (Suite) value;
            asSuite.optimizeStructure();
            asSuite.contents = asSuite.serialize();
        }
        this.lastLoaded = value;
        if (value instanceof Scenario) {
            tabTextEdit.setContent(warningPane);
        } else if (value != null) {
            tbTextEdit.setText(value.contents);
            tabTextEdit.setContent(editorPane);
        } else {
            tbTextEdit.setText("");
            tabTextEdit.setContent(editorPane);
        }
    }

    public void selTabChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        if (newValue == this.tabTextEdit && lastLoaded != null && lastLoaded instanceof Scenario) {
            mainForm.selectProgrammatically(lastLoaded.parent);
            Platform.runLater(() -> {
                if (mainForm.getTabs().getSelectionModel().getSelectedItem() != this.tabTextEdit) {
                    mainForm.getTabs().getSelectionModel().select(this.tabTextEdit);
                }
            });
        }
        if (newValue == this.tabTextEdit) {
            TreeItem<HighElement> focusedItem = mainForm.getProjectTree().getFocusModel().getFocusedItem();
            if (focusedItem != null) {
                loadElement(focusedItem.getValue());
            }
        }
    }
}

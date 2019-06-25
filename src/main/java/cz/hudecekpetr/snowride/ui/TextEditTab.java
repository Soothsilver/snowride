package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        bOrSwitch.setOnAction(event -> mainForm.selectProgrammatically(mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue().parent));
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
        tbTextEdit.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!mainForm.switchingTextEditContents) {
                HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
                whatChanged.areTextChangesUnapplied = true;
                whatChanged.contents = newValue;
                mainForm.changeOccurredTo(whatChanged, LastChangeKind.TEXT_CHANGED);
            }
        });
        Button bApply = new Button("Apply changes");
        Label lblInfo = new Label("Changes are applied automatically if you switch to another tab, test case, or suite; or if you save.");
        bApply.setOnAction(event -> {
            HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
            whatChanged.applyText();
            if (whatChanged.asSuite().fileParsed != null && whatChanged.asSuite().fileParsed.errors.size() > 0) {
                throw new RuntimeException("There are parse errors. See the other tabs for details.");
            }
        });
        Button bReformat = new Button("Reformat (Ctrl+L)");
        bReformat.setOnAction(event -> reformat());
        Tooltip.install(bReformat, new Tooltip("Reformats the file so that it looks as close as RIDE would reformat it. Does nothing if the file cannot be parsed."));
        mainForm.getStage().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.L && event.isControlDown()) {
               reformat();
            }
        });
        HBox hBox = new HBox(2, bReformat, bApply, lblInfo);
        hBox.setPadding(new Insets(2));
        hBox.setAlignment(Pos.CENTER_LEFT);
        VBox textVBox = new VBox(hBox, tbTextEdit);
        VBox.setVgrow(tbTextEdit, Priority.ALWAYS);
        editorPane = textVBox;
        tabTextEdit = new Tab("Edit entire file as text", textVBox);
        tabTextEdit.setClosable(false);
        return tabTextEdit;
    }

    private void reformat() {
        Suite reformattingWhat = lastLoaded.asSuite();
        reformattingWhat.applyText();
        if (reformattingWhat.fileParsed != null) {
            if (reformattingWhat.fileParsed.errors.size() > 0) {
                throw new RuntimeException("There are parse errors. Reformatting cannot happen.");
            }
            reformattingWhat.fileParsed.reformat();
            reformattingWhat.contents = reformattingWhat.serialize();
            mainForm.changeOccurredTo(reformattingWhat, LastChangeKind.TEXT_CHANGED);

        }
        loadElement(reformattingWhat);
    }

    public void loadElement(HighElement value) {
        if (value instanceof Suite && value.unsavedChanges == LastChangeKind.STRUCTURE_CHANGED) {
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

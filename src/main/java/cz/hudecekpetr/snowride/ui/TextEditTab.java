package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TextEditTab {

    private final TextArea tbTextEdit;
    private VBox editorPane;
    private final Node warningPane;
    private MainForm mainForm;
    private Tab tabTextEdit;

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
                    System.out.println(whatChanged.shortName + " selectedTabChanged.");
                    whatChanged.areTextChangesUnapplied = true;
                    whatChanged.contents = newValue;
                    mainForm.changeOccurredTo(whatChanged, LastChangeKind.TEXT_CHANGED);
                }
            }
        });
        Button bApply = new Button("Apply changes");
        Label lblInfo = new Label("Changes are applied automatically if you switch to another tab, test case, or suite.");
        bApply.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
                whatChanged.applyAndValidateText();
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
        if (value instanceof Scenario) {
            tabTextEdit.setContent(warningPane);
        } else {
            tbTextEdit.setText(value.contents);
            tabTextEdit.setContent(editorPane);
        }
    }
}
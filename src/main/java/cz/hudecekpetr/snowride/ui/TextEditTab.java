package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;

public class TextEditTab {

    private final TextArea tbTextEdit;
    private final Node warningPane;
    private VBox editorPane;
    private MainForm mainForm;
    private Tab tabTextEdit;
    private HighElement lastLoaded;
    private TextField tbSearchBox;
    private Scenario lastLoadedScenario;
    private boolean cleanLastLoadedScenario = true;

    public TextEditTab(MainForm mainForm) {
        this.mainForm = mainForm;
        tbTextEdit = new TextArea();

        // The warning pane is a remnant of an old solution where, if you tried to edit the text of a scenario,
        // it showed this warning pane. Now, it will programmatically select the suite that contains that scenario
        // instead. But in some edge cases, the warning pane can still be displayed.
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

    /**
     * Gets the Ctrl+F text field where you type what you want to search for.
     */
    public TextField getTbSearchBox() {
        return tbSearchBox;
    }

    public Tab createTab() {
        // Main editor
        tbTextEdit.setPromptText("This will display text...");
        tbTextEdit.setFont(MainForm.TEXT_EDIT_FONT);
        tbTextEdit.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!mainForm.switchingTextEditContents) {
                HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
                whatChanged.areTextChangesUnapplied = true;
                whatChanged.contents = ((Suite) whatChanged).newlineStyle.convertToStyle(newValue);
                mainForm.changeOccurredTo(whatChanged, LastChangeKind.TEXT_CHANGED);
            }
        });

        // Apply changes
        Button bApply = new Button("Apply changes");
        Label lblInfo = new Label("Changes are applied automatically if you switch to another tab, test case, or suite; or if you save.");
        bApply.setOnAction(event -> {
            HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
            whatChanged.applyText();
            if (whatChanged.asSuite().fileParsed != null && whatChanged.asSuite().fileParsed.errors.size() > 0) {
                throw new RuntimeException("There are parse errors. See the other tabs for details.");
            }
        });

        // Reformat
        Button bReformat = new Button("Reformat (Ctrl+L)");
        bReformat.setOnAction(event -> reformat());
        Tooltip.install(bReformat, new Tooltip("Reformats the file so that it looks as close as RIDE would reformat it. Does nothing if the file cannot be parsed."));
        mainForm.getStage().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.L && event.isShortcutDown()) {
                reformat();
            }
        });

        // Search field
        tbSearchBox = new TextField();
        tbSearchBox.setPromptText("Ctrl+F to search...");
        tbSearchBox.textProperty().addListener((observableValue, oldValue, newValue) -> tbSearchBoxChanged(newValue));
        tbSearchBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchNext();
                event.consume();
            }
        });

        HBox hBox = new HBox(2, tbSearchBox, bReformat, bApply, lblInfo);
        hBox.setPadding(new Insets(2));
        hBox.setAlignment(Pos.CENTER_LEFT);

        VBox textVBox = new VBox(hBox, tbTextEdit);
        VBox.setVgrow(tbTextEdit, Priority.ALWAYS);

        editorPane = textVBox;
        tabTextEdit = new Tab("Text edit", textVBox);
        tabTextEdit.setClosable(false);
        return tabTextEdit;
    }

    private void tbSearchBoxChanged(String newValue) {
        if (StringUtils.isEmpty(newValue)) {
            tbSearchBox.setStyle(null);
            return;
        }
        if (tbTextEdit.getText() == null) {
            // Sometime this can happen, apparently...
            return;
        }
        int firstIndex = StringUtils.indexOfIgnoreCase(tbTextEdit.getText(), newValue);
        if (firstIndex == -1) {
            // I have no idea how this works.
            // But see https://stackoverflow.com/a/27708846/1580088
            Paint paint = Paint.valueOf("#ffa0b9");
            tbSearchBox.setStyle("-fx-control-inner-background: #" + paint.toString().substring(2));
        } else {
            Paint paint = Paint.valueOf("#bff2ff");
            tbSearchBox.setStyle("-fx-control-inner-background: #" + paint.toString().substring(2));
            tbTextEdit.selectRange(firstIndex, firstIndex + newValue.length());
        }
    }

    /**
     * Selects the next instance of the searched text in the main editor.
     */
    private void searchNext() {
        if (tbTextEdit.getText() == null) {
            return;
        }
        String searchFor = tbSearchBox.getText();
        int firstIndex = StringUtils.indexOfIgnoreCase(tbTextEdit.getText(), searchFor, tbTextEdit.getAnchor() + 1);
        if (firstIndex == -1) {
            int fromStartIndex = StringUtils.indexOfIgnoreCase(tbTextEdit.getText(), searchFor);
            if (fromStartIndex != -1) {
                tbTextEdit.selectRange(fromStartIndex, fromStartIndex + searchFor.length());
            }
        } else {
            tbTextEdit.selectRange(firstIndex, firstIndex + searchFor.length());
        }

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

        if (cleanLastLoadedScenario) {
            lastLoadedScenario = null;
        }
        cleanLastLoadedScenario = true;

        if (value instanceof Scenario) {
            lastLoadedScenario = (Scenario) value;
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
            cleanLastLoadedScenario = false;
            mainForm.keepTabSelection = true;
            mainForm.selectProgrammatically(lastLoaded.parent);
        }
        if (oldValue == this.tabTextEdit) {
            HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
            whatChanged.applyText();
            if (lastLoadedScenario != null) {
                mainForm.selectChildOfFocusedElementIfAvailable(lastLoadedScenario);
            }
        }
    }
}

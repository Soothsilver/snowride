package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
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
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TextEditTab {

    private Map<HighElement, VirtualizedScrollPane<CodeArea>> codeAreaMap = new ConcurrentHashMap<>();
    private VirtualizedScrollPane<CodeArea> nonEditableCodeAreaPane;
    private SnowCodeArea codeArea;
    private final Node warningPane;
    private VBox editorPane;
    private final MainForm mainForm;
    private Tab tabTextEdit;
    private HighElement lastLoaded;
    private final TextField searchBox = new TextField();
    private Scenario lastLoadedScenario;
    private boolean cleanLastLoadedScenario = true;

    public TextEditTab(MainForm mainForm) {
        this.mainForm = mainForm;

        SnowCodeArea nonEditableCodeArea = new SnowCodeArea(searchBox);
        nonEditableCodeAreaPane = new VirtualizedScrollPane<>(nonEditableCodeArea);
        nonEditableCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(nonEditableCodeArea));
        VBox.setVgrow(nonEditableCodeAreaPane, Priority.ALWAYS);
        nonEditableCodeArea.setEditable(false);

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
    public TextField getSearchBox() {
        return searchBox;
    }

    public Tab createTab() {
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
        searchBox.setPromptText("Ctrl+F to search...");
        searchBox.textProperty().addListener((observableValue, oldValue, newValue) -> codeArea.searchBoxChanged(newValue));
        searchBox.focusedProperty().addListener((observable, oldValue, isFocused) -> {
            if (!oldValue && isFocused && StringUtils.isNotBlank(codeArea.getSelectedText())) {
                searchBox.setText(codeArea.getSelectedText());
            }
            if (isFocused) {
                String searchText = searchBox.getText();
                if (!StringUtils.isEmpty(searchText) && codeArea.getText() != null) {
                    codeArea.highlightAllOccurrences(searchText);
                }
            }
        });
        searchBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                codeArea.searchNext();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                String text = codeArea.getText();
                if (text != null) {
                    codeArea.clearStyle(0, text.length());
                }
                codeArea.requestFocus();
            }
        });

        HBox hBox = new HBox(2, searchBox, bReformat, bApply, lblInfo);
        hBox.setPadding(new Insets(2));
        hBox.setAlignment(Pos.CENTER_LEFT);
        VBox textVBox = new VBox(hBox);

        editorPane = textVBox;
        tabTextEdit = new Tab("Text edit", textVBox);
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
        lastLoaded = value;

        if (cleanLastLoadedScenario) {
            lastLoadedScenario = null;
        }
        cleanLastLoadedScenario = true;

        if (value instanceof Scenario) {
            lastLoadedScenario = (Scenario) value;
            tabTextEdit.setContent(warningPane);
            return;
        } else if (value instanceof Suite && value.contents != null) {
            if (!codeAreaMap.containsKey(value)) {
                switchCodeArea((Suite) value);
            }
            switchCodeArea(codeAreaMap.get(value));
            codeArea.loadElement(value, lastLoadedScenario);
        } else {
            switchCodeArea(nonEditableCodeAreaPane);
        }
        tabTextEdit.setContent(editorPane);
    }

    public void selTabChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        if (newValue == tabTextEdit && lastLoaded != null && lastLoaded instanceof Scenario) {
            cleanLastLoadedScenario = false;
            mainForm.keepTabSelection = true;
            mainForm.selectProgrammatically(lastLoaded.parent);
        }
        if (oldValue == tabTextEdit) {
            HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
            whatChanged.applyText();
            if (lastLoadedScenario != null) {
                mainForm.selectChildOfFocusedElementIfAvailable(lastLoadedScenario);
            }
        }
    }

    public void clear() {
        codeAreaMap.clear();
    }

    private void switchCodeArea(Suite value) {
        SnowCodeArea codeArea = new SnowCodeArea(searchBox);
        codeArea.overrideDefaultKeybindings(value.newlineStyle);
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!mainForm.switchingTextEditContents) {
                HighElement whatChanged = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
                whatChanged.areTextChangesUnapplied = true;
                whatChanged.contents = ((Suite) whatChanged).newlineStyle.convertToStyle(newValue);
                mainForm.changeOccurredTo(whatChanged, LastChangeKind.TEXT_CHANGED);
            }
        });
        VirtualizedScrollPane<CodeArea> pane = new VirtualizedScrollPane<>(codeArea);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        VBox.setVgrow(pane, Priority.ALWAYS);

        codeAreaMap.put(value, pane);
    }

    private void switchCodeArea(VirtualizedScrollPane<CodeArea> scrollPane) {
        codeArea = (SnowCodeArea) scrollPane.getContent();
        if (editorPane.getChildren().size() > 1) {
            editorPane.getChildren().remove(1);
        }
        editorPane.getChildren().add(scrollPane);
    }
}

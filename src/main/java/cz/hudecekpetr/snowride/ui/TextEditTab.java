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
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import static cz.hudecekpetr.snowride.filesystem.LastChangeKind.STRUCTURE_CHANGED;

public class TextEditTab {

    private final Node warningPane;
    private VBox editorPane;
    private final MainForm mainForm;
    private Tab tabTextEdit;
    private HighElement lastLoaded;
    private Scenario lastLoadedScenario;
    private final SnowCodeAreaProvider codeAreaProvider = SnowCodeAreaProvider.INSTANCE;
    private boolean disableMovingCaret;
    public boolean manuallySelected;

    public TextEditTab(MainForm mainForm) {
        this.mainForm = mainForm;
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

        HBox hBox = new HBox(2, SnowCodeAreaSearchBox.INSTANCE, bReformat, bApply, lblInfo);
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
        if (value instanceof Suite && value.unsavedChanges == STRUCTURE_CHANGED) {
            ((Suite) value).updateContents();
        }

        lastLoaded = value;

        if (value instanceof Scenario) {
            lastLoadedScenario = (Scenario) value;
            tabTextEdit.setContent(warningPane);
            // Eagerly initialize codeArea for Suite! to ensure user can 'undo' ANY changes in the Text edit
            codeAreaProvider.getTextEditCodeArea(lastLoadedScenario.parent);
            return;
        } else {
            if (value instanceof Suite && value.contents != null) {
                switchCodeArea((Suite) value);
            } else {
                switchCodeArea(null);
            }
        }
        tabTextEdit.setContent(editorPane);
    }

    public void selTabChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        if (oldValue == tabTextEdit) {
            lastLoaded.applyText();
            for (HighElement child : lastLoaded.children) {
                if (child.contents != null && !child.contents.equals(child.pristineContents)) {
                    child.unsavedChanges = STRUCTURE_CHANGED;
                }
            }
            if (!manuallySelected) {
                HighElement currentlyEditedScenario = codeAreaProvider.getCodeArea().getCurrentlyEditedScenario();
                if (currentlyEditedScenario == lastLoaded) {
                    mainForm.gridTab.loadElement(lastLoaded);
                } else {
                    mainForm.keepTabSelection = true;
                    mainForm.selectProgrammatically(currentlyEditedScenario);
                }
            }
        }
        if (newValue == tabTextEdit && lastLoaded != null) {
            if (lastLoaded instanceof Scenario) {
                mainForm.keepTabSelection = true;
                mainForm.selectProgrammatically(lastLoaded.parent);
            } else {
                // switching to "Text edit" for Suite - ensure caret is not moved to 'lastLoadedScenario'
                disableMovingCaret = true;
                loadElement(lastLoaded);
                disableMovingCaret = false;
            }
        }

        manuallySelected = false;
    }

    private void switchCodeArea(Suite suite) {
        VirtualizedScrollPane<SnowCodeArea> scrollPane;
        if (suite != null) {
            scrollPane = codeAreaProvider.getTextEditCodeArea(suite);
            if (!disableMovingCaret && suite.children.contains(lastLoadedScenario)) {
                scrollPane.getContent().moveCaretToCurrentlyEditedScenario(lastLoadedScenario);
            } else {
                scrollPane.getContent().moveCaretToCurrentlyEditedScenario(null);
            }
        } else {
            scrollPane = codeAreaProvider.getNonEditableCodeAreaPane();
        }

        if (editorPane.getChildren().size() > 1) {
            editorPane.getChildren().remove(1);
        }
        editorPane.getChildren().add(scrollPane);
    }
}

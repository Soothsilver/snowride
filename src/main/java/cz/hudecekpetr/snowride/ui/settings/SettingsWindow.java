package cz.hudecekpetr.snowride.ui.settings;

import cz.hudecekpetr.snowride.fx.CenterToParentUtility;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

public class SettingsWindow extends Stage {

    private TitledTextArea additionalXmlFilesBox;
    private MainForm mainForm;
    private CheckBox cbAlsoImportTxtFiles;
    private CheckBox cbDeselectAll;
    private CheckBox cbReloadAll;
    private CheckBox cbFirstCompletionOption;
    private CheckBox cbAutoExpandSelectedTests;
    private CheckBox cbUseStructureChanged;
    private TextField tbNumber;
    private CheckBox cbGarbageCollect;
    private CheckBox cbHighlightSameCells;
    private CheckBox cbUseSystemColorWindow;
    private CheckBox cbAutocompleteVariables;
    private TextField tbNumber2;

    public SettingsWindow(MainForm mainForm) {
        this.mainForm = mainForm;
        Tab tabImporting = createTabImporting();
        TabPane tabs = new TabPane(tabImporting);
        Button buttonSuperOK = new Button("Apply, close, and reload project", new ImageView(Images.refresh));
        buttonSuperOK.setOnAction(this::applyCloseAndRefresh);
        Button buttonOK = new Button("Apply and close");
        buttonOK.setOnAction(this::applyAndClose);
        Button buttonCancel = new Button("Cancel");
        buttonCancel.setOnAction(event -> SettingsWindow.this.close());
        HBox buttonRow = new HBox(5, buttonSuperOK, buttonOK, buttonCancel);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        VBox all = new VBox(5, tabs, buttonRow);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        all.setPadding(new Insets(8));
        this.setScene(new Scene(all, 700, 600));
        this.getIcons().add(Images.keywordIcon);
        this.setTitle("Settings");
        CenterToParentUtility.prepareToShowAtCenterOfMainForm(this);
    }

    private void applyCloseAndRefresh(ActionEvent actionEvent) {
        applyAndClose(actionEvent);
        mainForm.reloadAll();
    }

    private Tab createTabImporting() {
        additionalXmlFilesBox = new TitledTextArea("Folders for XML, Python and Java files", Settings.getInstance().additionalFolders);
        Label folderDescription = new Label("Each line is an absolute path to a folder. Snowride will add these folders to the runner script's pythonpath, and it will browse these folders for XML files, Python files, and Robot Framework files in order to get documentation.");
        folderDescription.setWrapText(true);
        cbAlsoImportTxtFiles = new CheckBox("Parse .txt files in addition to .robot files.");
        cbAlsoImportTxtFiles.setSelected(Settings.getInstance().cbAlsoImportTxtFiles);
        cbReloadAll = new CheckBox("Show 'Reload all' button in the toolbar.");
        cbReloadAll.setSelected(Settings.getInstance().toolbarReloadAll);
        cbDeselectAll = new CheckBox("Show 'Deselect all' button in the toolbar.");
        cbDeselectAll.setSelected(Settings.getInstance().toolbarDeselectEverything);
        cbFirstCompletionOption = new CheckBox("If you type a nonexistent keyword, confirm it with Enter instead of choosing the first completion option.");
        cbFirstCompletionOption.setWrapText(true);
        cbFirstCompletionOption.setSelected(Settings.getInstance().cbShowNonexistentOptionFirst);
        cbAutoExpandSelectedTests = new CheckBox("When you 'select all tests' or 'select failed tests', also expand them all.");
        cbAutoExpandSelectedTests.setWrapText(true);
        cbAutoExpandSelectedTests.setSelected(Settings.getInstance().cbAutoExpandSelectedTests);
        cbUseStructureChanged = new CheckBox("Show [structure changed] or [text changed] instead of an asterisk (*) for changed files.");
        cbUseStructureChanged.setWrapText(true);
        cbUseStructureChanged.setSelected(Settings.getInstance().cbUseStructureChanged);
        Label lblNumber = new Label("In 'until failure' mode, how many successes should end testing even if there's no failure: ");
        tbNumber = new TextField(Integer.toString(Settings.getInstance().numberOfSuccessesBeforeEnd));
        //https://stackoverflow.com/a/36436243/1580088
        tbNumber.setTextFormatter(new TextFormatter<String>(change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        }));
        HBox num = new HBox(5, lblNumber, tbNumber);
        cbGarbageCollect = new CheckBox("Automatically garbage collect every 5 minutes. Changes to this will take effect when you next start Snowride. Additional action from your side required!: Add the following VM options to your launcher to force JVM to return freed memory back to the operating system.");
        cbGarbageCollect.setSelected(Settings.getInstance().cbRunGarbageCollection);
        cbGarbageCollect.setWrapText(true);
        TextField tbXXargs = new TextField("-XX:+UseG1GC -XX:MaxHeapFreeRatio=30 -XX:MinHeapFreeRatio=10");
        tbXXargs.setEditable(false);
        VBox borderBox = new VBox(5, cbGarbageCollect, tbXXargs);
        borderBox.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 3px; ");

        cbHighlightSameCells = new CheckBox("Highlight cells with the same content as the selected cell in yellow.");
        cbHighlightSameCells.setWrapText(true);
        cbHighlightSameCells.setSelected(Settings.getInstance().cbHighlightSameCells);

        cbUseSystemColorWindow = new CheckBox("Use system color 'window' for background of text boxes instead of the default off-white color (requires a Snowride restart).");
        cbUseSystemColorWindow.setWrapText(true);
        cbUseSystemColorWindow.setSelected(Settings.getInstance().cbUseSystemColorWindow);

        Label lblNumber2 = new Label("Size of tree view font (in points): ");
        tbNumber2 = new TextField(Integer.toString(Settings.getInstance().treeSizeItemHeight));
        tbNumber2.setTextFormatter(new TextFormatter<String>(change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        }));
        tbNumber2.textProperty().addListener((ChangeListener<String>)this::treeSizeChanged);
        HBox num2 = new HBox(5, lblNumber2, tbNumber2);

        cbAutocompleteVariables = new CheckBox("Offer autocompletion for variables");
        cbAutocompleteVariables.setWrapText(true);
        cbAutocompleteVariables.setSelected(Settings.getInstance().cbAutocompleteVariables);

        VBox vboxImportingOptions = new VBox(5, additionalXmlFilesBox, folderDescription, cbAlsoImportTxtFiles, cbReloadAll, cbDeselectAll, cbFirstCompletionOption, cbAutoExpandSelectedTests, cbUseStructureChanged, num, borderBox, cbHighlightSameCells, cbUseSystemColorWindow, num2, cbAutocompleteVariables);
        vboxImportingOptions.setPadding(new Insets(5, 0, 0, 0));


        Tab tabImporting = new Tab("Settings", vboxImportingOptions);
        tabImporting.setClosable(false);
        return tabImporting;
    }

    private void treeSizeChanged(ObservableValue<? extends String> observableValue, String old, String newValue) {
        if (StringUtils.isBlank(newValue)) {
            return;
        }
        try {
            int asInt = Integer.parseInt(newValue);
            mainForm.getProjectTree().setStyle("-fx-font-size: " + asInt + "pt;");
            Settings.getInstance().treeSizeItemHeight = asInt;
        } catch (Exception e) {
            throw new RuntimeException(newValue + " is not a number.");
        }
    }

    private void applyAndClose(ActionEvent actionEvent) {
        Settings.getInstance().additionalFolders = additionalXmlFilesBox.getText();
        Settings.getInstance().cbAlsoImportTxtFiles = cbAlsoImportTxtFiles.isSelected();
        Settings.getInstance().toolbarDeselectEverything = cbDeselectAll.isSelected();
        Settings.getInstance().cbShowNonexistentOptionFirst = cbFirstCompletionOption.isSelected();
        Settings.getInstance().toolbarReloadAll = cbReloadAll.isSelected();
        Settings.getInstance().cbAutoExpandSelectedTests = cbAutoExpandSelectedTests.isSelected();
        Settings.getInstance().cbRunGarbageCollection = cbGarbageCollect.isSelected();
        Settings.getInstance().cbHighlightSameCells = cbHighlightSameCells.isSelected();
        Settings.getInstance().cbUseStructureChanged = cbUseStructureChanged.isSelected();
        Settings.getInstance().cbUseSystemColorWindow = cbUseSystemColorWindow.isSelected();
        Settings.getInstance().cbAutocompleteVariables = cbAutocompleteVariables.isSelected();
        try {
            Settings.getInstance().numberOfSuccessesBeforeEnd = Integer.parseInt(tbNumber.getText());
            mainForm.runTab.numberOfSuccessesToStop.setValue(Settings.getInstance().numberOfSuccessesBeforeEnd);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Settings.getInstance().saveAllSettings();
        mainForm.updateAdditionalToolbarButtonsVisibility();
        mainForm.reloadExternalLibraries();
        this.close();
    }
}

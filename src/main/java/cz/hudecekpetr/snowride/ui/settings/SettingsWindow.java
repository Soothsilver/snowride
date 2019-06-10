package cz.hudecekpetr.snowride.ui.settings;

import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Set;

public class SettingsWindow extends Stage {

    private TitledTextArea additionalXmlFilesBox;
    private MainForm mainForm;
    private CheckBox cbAlsoImportTxtFiles;

    public SettingsWindow(MainForm mainForm) {
        this.mainForm = mainForm;
        Tab tabImporting = createTabImporting();
        TabPane tabs = new TabPane(tabImporting);
        Button buttonSuperOK = new Button("Apply, close, and reload project", new ImageView(Images.refresh));
        buttonSuperOK.setOnAction(this::applyCloseAndRefresh);
        Button buttonOK = new Button("Apply and close");
        buttonOK.setOnAction(this::applyAndClose);
        Button buttonCancel = new Button("Cancel");
        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                 SettingsWindow.this.close();
            }
        });
        HBox buttonRow = new HBox(5, buttonSuperOK, buttonOK, buttonCancel);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        VBox all = new VBox(5, tabs, buttonRow);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        all.setPadding(new Insets(8));
        this.setScene(new Scene(all, 500, 600));
        this.getIcons().add(Images.keywordIcon);
        this.setTitle("Settings");
    }

    private void applyCloseAndRefresh(ActionEvent actionEvent) {
        Settings.getInstance().additionalFolders = additionalXmlFilesBox.getText();
        Settings.getInstance().cbAlsoImportTxtFiles = cbAlsoImportTxtFiles.isSelected();
        Settings.getInstance().save();
        this.close();
        mainForm.reloadAll(actionEvent);
    }

    private Tab createTabImporting() {
        additionalXmlFilesBox = new TitledTextArea("Folders for XML, Python and Java files", Settings.getInstance().additionalFolders);
        Label folderDescription = new Label("Each line is an absolute path to a folder. Snowride will add these folders to the runner script's pythonpath, and it will browse these folders for XML files, Python files, and Robot Framework files in order to get documentation.");
        folderDescription.setWrapText(true);
        cbAlsoImportTxtFiles = new CheckBox("Parse .txt files in addition to .robot files.");
        cbAlsoImportTxtFiles.setSelected(Settings.getInstance().cbAlsoImportTxtFiles);
        VBox vboxImportingOptions = new VBox(5, additionalXmlFilesBox, folderDescription, cbAlsoImportTxtFiles);
        vboxImportingOptions.setPadding(new Insets(5,0,0,0));
        Tab tabImporting = new Tab("Settings", vboxImportingOptions);
        tabImporting.setClosable(false);
        return tabImporting;
    }

    private void applyAndClose(ActionEvent actionEvent) {
        Settings.getInstance().additionalFolders = additionalXmlFilesBox.getText();
        Settings.getInstance().cbAlsoImportTxtFiles = cbAlsoImportTxtFiles.isSelected();
        Settings.getInstance().save();
        mainForm.reloadExternalLibraries();
        this.close();
    }
}

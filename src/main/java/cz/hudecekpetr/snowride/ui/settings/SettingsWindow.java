package cz.hudecekpetr.snowride.ui.settings;

import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsWindow extends Stage {

    private TitledTextArea additionalXmlFilesBox;
    private MainForm mainForm;

    public SettingsWindow(MainForm mainForm) {
        this.mainForm = mainForm;
        Tab tabImporting = createTabImporting();
        TabPane tabs = new TabPane(tabImporting);
        Button buttonOK = new Button("OK");
        buttonOK.setOnAction(this::applyAndClose);
        Button buttonCancel = new Button("Cancel");
        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                 SettingsWindow.this.close();
            }
        });
        HBox buttonRow = new HBox(5, buttonOK, buttonCancel);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        VBox all = new VBox(5, tabs, buttonRow);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        all.setPadding(new Insets(8));
        this.setScene(new Scene(all, 500, 600));
    }

    private Tab createTabImporting() {
        additionalXmlFilesBox = new TitledTextArea("Additional XML files", Settings.getInstance().additionalXmlFiles);
        VBox vboxImportingOptions = new VBox(5, additionalXmlFilesBox);
        Tab tabImporting = new Tab("Importing", vboxImportingOptions);
        tabImporting.setClosable(false);
        return tabImporting;
    }

    private void applyAndClose(ActionEvent actionEvent) {
        Settings.getInstance().additionalXmlFiles = additionalXmlFilesBox.getText();
        Settings.getInstance().save();
        mainForm.reloadExternalLibraries();
        this.close();
    }
}

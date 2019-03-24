package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class RunTab {
    private MainForm mainForm;
    private FileChooser openScriptFileDialog;
    private TextField tbScript;

    public RunTab(MainForm mainForm) {
        this.mainForm = mainForm;
        openScriptFileDialog = new FileChooser();
        openScriptFileDialog.setTitle("Choose runner script");
        openScriptFileDialog.getExtensionFilters().addAll(
               new FileChooser.ExtensionFilter("Windows executable files", "*.bat", "*.exe"),
               new FileChooser.ExtensionFilter("All files", "*.*")
        );
    }

    public Tab createTab() {

        TextArea tbOutput = new TextArea("Standard output goes here.");
        tbOutput.setFont(MainForm.TEXT_EDIT_FONT);
        tbOutput.setEditable(false);
        TextArea tbLog = new TextArea("Log goes here.");
        tbLog.setEditable(false);
        tbLog.setFont(MainForm.TEXT_EDIT_FONT);
        SplitPane splitterOutput = new SplitPane(tbOutput, tbLog);
        splitterOutput.setOrientation(Orientation.VERTICAL);
        Label lblScript = new Label("Script:");
        tbScript = new TextField();
        Button bLoadScriptButton = new Button("...");
        bLoadScriptButton.setOnAction(this::loadScriptOnClick);
        HBox hboxScript = new HBox(5, lblScript, tbScript, bLoadScriptButton);
        hboxScript.setPadding(new Insets(2));
        hboxScript.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tbScript, Priority.ALWAYS);
        Button bRun = new Button("Run");
        bRun.disableProperty().bind(mainForm.canRun.not());
        Button bStop = new Button("Stop");
        bStop.disableProperty().bind(mainForm.canStop.not());
        Button bLog = new Button("Log");
        Button bReport = new Button("Report");
        HBox hboxButtons = new HBox(5,bRun, bStop, bLog, bReport);
        hboxButtons.setPadding(new Insets(2));
        Label labelArguments = new Label("Arguments:");
        TextField tbArguments = new TextField();
        tbArguments.setFont(MainForm.TEXT_EDIT_FONT);
        HBox hboxArguments = new HBox(5, labelArguments, tbArguments);
        hboxArguments.setPadding(new Insets(2));
        hboxArguments.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tbArguments, Priority.ALWAYS);
        Label lblTotalTime = new Label("0:00:00");
        Label lblFailed = new Label("Failed: 0");
        Label lblPassed = new Label("Passed: 0");
        Label lblKeyword = new Label("No keyword running.");
        lblTotalTime.setMinWidth(50);
        lblFailed.setMinWidth(90);
        lblPassed.setMinWidth(90);
        HBox hboxExecutionLine = new HBox(lblTotalTime, lblFailed, lblPassed, lblKeyword);
        hboxExecutionLine.setAlignment(Pos.CENTER_LEFT);
        VBox vboxTabRun = new VBox(0, hboxScript, hboxButtons, hboxArguments, hboxExecutionLine, splitterOutput);
        VBox.setVgrow(splitterOutput, Priority.ALWAYS);
        Tab tabRun = new Tab("Run", vboxTabRun);
        tabRun.setClosable(false);
        return tabRun;
    }

    private void loadScriptOnClick(ActionEvent actionEvent) {
        File file = openScriptFileDialog.showOpenDialog(mainForm.getStage());
        if (file != null) {
            tbScript.setText(file.getAbsoluteFile().toString());
        }
    }
}

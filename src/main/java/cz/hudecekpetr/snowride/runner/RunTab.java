package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RunTab {
    private MainForm mainForm;

    public RunTab(MainForm mainForm) {
        this.mainForm = mainForm;
    }

    public Tab createTab() {

        TextArea tbOutput = new TextArea("Standard output goes here.");
        TextArea tbLog = new TextArea("Log goes here.");
        SplitPane splitterOutput = new SplitPane(tbOutput, tbLog);
        splitterOutput.setOrientation(Orientation.VERTICAL);
        Label lblScript = new Label("Script:");
        TextField tbScript = new TextField();
        Button bLoadScriptButton = new Button("...");
        HBox hboxScript = new HBox(lblScript, tbScript, bLoadScriptButton);
        HBox.setHgrow(tbScript, Priority.ALWAYS);
        Button bRun = new Button("Run");
        bRun.disableProperty().bind(mainForm.canRun.not());
        Button bStop = new Button("Stop");
        bRun.disableProperty().bind(mainForm.canStop.not());
        Button bLog = new Button("Log");
        Button bReport = new Button("Report");
        HBox hboxButtons = new HBox(bRun, bStop, bLog, bReport);
        Label labelArguments = new Label("Arguments:");
        TextField tbArguments = new TextField();
        HBox hboxArguments = new HBox(labelArguments, tbArguments);
        HBox.setHgrow(tbArguments, Priority.ALWAYS);
        Label lblExecutionLine = new Label("Something will be displayed here.");
        HBox hboxExecutionLine = new HBox(lblExecutionLine);
        VBox vboxTabRun = new VBox(0, hboxScript, hboxButtons, hboxArguments, hboxExecutionLine, splitterOutput);
        VBox.setVgrow(splitterOutput, Priority.ALWAYS);
        Tab tabRun = new Tab("Run", vboxTabRun);
        tabRun.setClosable(false);
        return tabRun;
    }
}

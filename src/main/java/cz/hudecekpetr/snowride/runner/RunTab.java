package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RunTab {
    private MainForm mainForm;
    private FileChooser openScriptFileDialog;
    private TextField tbScript;
    private Run run = new Run();
    public BooleanProperty canRun = new SimpleBooleanProperty(true);
    public BooleanProperty canStop = new SimpleBooleanProperty(false);
    private TextArea tbOutput;
    private TextArea tbLog;
    private Tab tabRun;
    private Label lblKeyword;
    private Label lblPassed;
    private Label lblFailed;
    private Label lblTotalTime;
    private HBox hboxExecutionLine;
    private TcpHost tcpHost = new TcpHost();
    private TextField tbArguments;
    private Path temporaryDirectory;


    public RunTab(MainForm mainForm) {
        this.mainForm = mainForm;
        this.temporaryDirectory = createTemporaryDirectory();
        openScriptFileDialog = new FileChooser();
        openScriptFileDialog.setTitle("Choose runner script");
        openScriptFileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Windows executable files", "*.bat", "*.exe"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
    }

    private Path createTemporaryDirectory() {
        try {
            Path tempDirectory = Files.createTempDirectory("Snowride");
            return tempDirectory;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Tab createTab() {

        tbOutput = new TextArea("Standard output goes here.");
        tbOutput.setFont(MainForm.TEXT_EDIT_FONT);
        tbOutput.setEditable(false);
        tbLog = new TextArea("Log goes here.");
        tbLog.setEditable(false);
        tbLog.setFont(MainForm.TEXT_EDIT_FONT);
        SplitPane splitterOutput = new SplitPane(tbOutput, tbLog);
        splitterOutput.setOrientation(Orientation.VERTICAL);
        Label lblScript = new Label("Script:");
        tbScript = new TextField(Settings.getInstance().runScript);
        Button bLoadScriptButton = new Button("...");
        bLoadScriptButton.setOnAction(this::loadScriptOnClick);
        HBox hboxScript = new HBox(5, lblScript, tbScript, bLoadScriptButton);
        hboxScript.setPadding(new Insets(2));
        hboxScript.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tbScript, Priority.ALWAYS);
        Button bRun = new Button("Run");
        bRun.setOnAction(this::clickRun);
        bRun.disableProperty().bind(canRun.not());
        Button bStop = new Button("Stop");
        bStop.setOnAction(this::clickStop);
        bStop.disableProperty().bind(canStop.not());
        Button bLog = new Button("Log");
        bLog.disableProperty().bind(Bindings.isNull(run.logFile));
        bLog.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openFile(RunTab.this.run.logFile.getValue());
            }
        });
        Button bReport = new Button("Report");
        bReport.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openFile(RunTab.this.run.reportFile.getValue());
            }
        });
        bReport.disableProperty().bind(Bindings.isNull(run.reportFile));
        HBox hboxButtons = new HBox(5, bRun, bStop, bLog, bReport);
        hboxButtons.setPadding(new Insets(2));
        Label labelArguments = new Label("Arguments:");
        tbArguments = new TextField(Settings.getInstance().runArguments);
        tbArguments.setFont(MainForm.TEXT_EDIT_FONT);
        HBox hboxArguments = new HBox(5, labelArguments, tbArguments);
        hboxArguments.setPadding(new Insets(2));
        hboxArguments.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tbArguments, Priority.ALWAYS);
        lblTotalTime = new Label("0:00:00");
        lblFailed = new Label("Failed: 0");
        lblPassed = new Label("Passed: 0");
        lblKeyword = new Label("No keyword running.");
        lblTotalTime.setMinWidth(50);
        lblFailed.setMinWidth(90);
        lblPassed.setMinWidth(90);
        hboxExecutionLine = new HBox(lblTotalTime, lblFailed, lblPassed, lblKeyword);
        hboxExecutionLine.setAlignment(Pos.CENTER_LEFT);
        VBox vboxTabRun = new VBox(0, hboxScript, hboxButtons, hboxArguments, hboxExecutionLine, splitterOutput);
        VBox.setVgrow(splitterOutput, Priority.ALWAYS);
        tabRun = new Tab("Run", vboxTabRun);
        tabRun.setClosable(false);
        return tabRun;
    }

    public void clickStop(ActionEvent actionEvent) {
        if (run.stoppableProcessId.getValue() > 0) {
            run.forciblyKilled = true;
            try {
                ProcessUtil.destroyForcefullyAndWait(Processes.newPidProcess(run.stoppableProcessId.getValue()));
                appendGreenText("Robot process killed.");
                updateResultsPanel();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void appendGreenText(String text) {
        this.tbOutput.appendText(text + "\n");
    }

    public void clickRun(ActionEvent actionEvent) {
        try {
            tbOutput.clear();
            tbLog.clear();
            mainForm.getTabs().getSelectionModel().select(tabRun);
            canRun.set(false);
            canStop.set(false);
            run.clear();
            updateResultsPanel();
            lblKeyword.setText("");
            rememberRunPageSettings();
            planRobots();

            String runner = tbScript.getText();
            File runnerFile = new File(runner);
            File runnerDirectory = runnerFile.getParentFile();

            ProcessBuilder processBuilder = new ProcessBuilder();
            List<String> command = composeScriptAndArguments();
            tbOutput.setText("> " + String.join(" ", command));
            processBuilder.command(command);
            processBuilder.directory(runnerDirectory);
            processBuilder.redirectErrorStream(true);
            Process start = processBuilder.start();
            start.getInputStream(); // TODO do something about it
            // TODO notice the exit


        } catch (Exception ex) {
            canRun.set(true);
            tbOutput.setText(Extensions.toStringWithTrace(ex));
            new Alert(Alert.AlertType.WARNING, Extensions.toStringWithTrace(ex), ButtonType.CLOSE).showAndWait();
        }
    }

    private List<String> composeScriptAndArguments() {
        File argfile;
        File runnerAgent;
        try {
            runnerAgent = temporaryDirectory.resolve("TestRunnerAgent.py").toFile();
            argfile = File.createTempFile("argfile", ".txt", temporaryDirectory.toFile());
            createArgFile(argfile);
            File testRunnerAgentFile = new File(this.getClass().getResource("/TestRunnerAgent.py").getFile());
            byte[] testRunnerAgentData = Files.readAllBytes(testRunnerAgentFile.toPath());
            Files.write(runnerAgent.toPath(), testRunnerAgentData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> result = new ArrayList<>();
        result.add(this.tbScript.getText());
        result.add("--argumentfile");
        result.add(argfile.toString());
        result.add("--listener");
        result.add(runnerAgent.toString() + ":" + tcpHost.portNumber + ":False");
        String[] args = StringUtils.splitByWholeSeparator(this.tbArguments.getText(), " ");
        for (String arg : args) {
            result.add(arg);
        }
        result.add(((FolderSuite) mainForm.getProjectTree().getRoot().getValue()).directoryPath.toString());
        return result;
    }

    private void createArgFile(File argfile) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("--outputdir");
        lines.add(temporaryDirectory.toString());
        lines.add("-C");
        lines.add("ansi");
        FileUtils.writeLines(argfile, lines);
    }

    private void planRobots() {
        // Don't plant yet. We don't have checkboxes yet.
    }

    private void rememberRunPageSettings() {
        Settings.getInstance().runArguments = tbArguments.getText();
        Settings.getInstance().runScript = tbScript.getText();
        Settings.getInstance().save();
    }

    private void updateResultsPanel() {
        if (run.forciblyKilled) {
            setHboxBackgroundColor(Color.LIGHTGRAY);
        } else if (run.countFailedTests > 0) {
            setHboxBackgroundColor(Color.SANDYBROWN);
        } else if (run.countPassedTests > 0) {
            if (run.runInProgress) {
                setHboxBackgroundColor(Color.LIGHTGREEN);
            } else {
                setHboxBackgroundColor(Color.LIMEGREEN);
            }
        } else {
            setHboxBackgroundColor(Color.TRANSPARENT);
        }
        lblFailed.setText("Failed: " + run.countFailedTests);
        lblPassed.setText("Passed: " + run.countPassedTests);
    }

    private void setHboxBackgroundColor(Color color) {
        hboxExecutionLine.setBackground(new Background(new BackgroundFill(color, null, null)));
    }

    private void openFile(String filename) {
        try {
            Desktop.getDesktop().open(new File(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadScriptOnClick(ActionEvent actionEvent) {
        File file = openScriptFileDialog.showOpenDialog(mainForm.getStage());
        if (file != null) {
            tbScript.setText(file.getAbsoluteFile().toString());
        }
    }
}

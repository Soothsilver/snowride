package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.ui.DeferredActions;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RunTab {
    private MainForm mainForm;
    private FileChooser openScriptFileDialog;
    private TextField tbScript;
    public Run run = new Run();
    public BooleanProperty canRun = new SimpleBooleanProperty(true);
    public BooleanProperty canStop = new SimpleBooleanProperty(false);
    private TextArea tbOutput;
    public TextArea tbLog;
    private Tab tabRun;
    public Label lblKeyword;
    private Label lblPassed;
    private Label lblFailed;
    private Label lblTotalTime;
    private HBox hboxExecutionLine;
    private TcpHost tcpHost;
    private TextField tbArguments;
    private Path temporaryDirectory;
    private Executor executor = Executors.newFixedThreadPool(4);


    public RunTab(MainForm mainForm) {
        this.mainForm = mainForm;
        this.tcpHost = new TcpHost(this);
        this.tcpHost.start();
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
        canStop.bind(run.stoppableProcessId.greaterThan(-1));
        canRun.bind(run.running.not());
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> timer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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
        lblTotalTime.setPadding(new Insets(0,0,0,10));
        lblFailed = new Label("Failed: 0");
        lblPassed = new Label("Passed: 0");
        lblKeyword = new Label("No keyword running.");
        lblTotalTime.setMinWidth(90);
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

    private void timer() {
        if (run.isInProgress()) {
            this.lblTotalTime.setText(Extensions.millisecondsToHumanTime(System.currentTimeMillis() - run.lastRunBeganWhen));
            this.lblKeyword.setText(run.keywordStackAsString());
        }
        DeferredActions.timer(this);
    }

    public void clickStop(ActionEvent actionEvent) {
        if (run.stoppableProcessId.getValue() > 0) {
            run.forciblyKilled = true;
            try {
                ProcessUtil.destroyForcefullyAndWait(Processes.newPidProcess(run.stoppableProcessId.getValue()));
                run.stoppableProcessId.setValue(-1);
                run.running.set(false);
                appendGreenText("Robot process killed.");
                updateResultsPanel();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void appendGreenText(String text) {
        this.tbOutput.appendText(text + "\n");
    }

    public void clickRun(ActionEvent actionEvent) {
        try {
            List<Scenario> testCases = getCheckedTestCases();
            if (testCases.size() == 0) {
                Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, "You didn't choose any test case. Do you want to run the entire suite?", ButtonType.YES, ButtonType.NO).showAndWait();
                if (buttonType.orElse(ButtonType.NO) == ButtonType.NO) {
                    // cancel
                    return;
                }
            }

            tbOutput.clear();
            tbLog.clear();
            mainForm.getTabs().getSelectionModel().select(tabRun);
            run.clear();
            updateResultsPanel();
            lblKeyword.setText("");
            rememberRunPageSettings();
            planRobots();

            String runner = tbScript.getText();
            File runnerFile = new File(runner);
            File runnerDirectory = runnerFile.getParentFile();

            ProcessBuilder processBuilder = new ProcessBuilder();
            List<String> command = composeScriptAndArguments(testCases);
            tbOutput.setText("> " + String.join(" ", command));
            processBuilder.command(command);
            processBuilder.directory(runnerDirectory);
            processBuilder.redirectErrorStream(true);
            Process start = processBuilder.start();
            run.stoppableProcessId.setValue(-1);
            run.running.set(true);
            executor.execute(() -> readFromOutput(start.getInputStream()));
            executor.execute(() -> this.waitForProcessExit(start));


        } catch (Exception ex) {
            tbOutput.setText(Extensions.toStringWithTrace(ex));
            new Alert(Alert.AlertType.WARNING, Extensions.toStringWithTrace(ex), ButtonType.CLOSE).showAndWait();
        }
    }

    private List<Scenario> getCheckedTestCases() {
        HighElement element = mainForm.getProjectTree().getRoot().getValue();
        List<Scenario> checkedStuff = new ArrayList<>();
        collectCheckedTestCases(element, checkedStuff);
        return checkedStuff;
    }

    private void collectCheckedTestCases(HighElement element, List<Scenario> checkedStuff) {
        if (element instanceof Scenario) {
            if (element.checkbox.isSelected()) {
                checkedStuff.add((Scenario) element);
            }
        }
        for(HighElement child : element.children) {
            collectCheckedTestCases(child, checkedStuff);
        }
    }

    private void readFromOutput(InputStream inputStream) {
        try {
            InputStreamReader twilight = new InputStreamReader(inputStream);
            char[] buffer = new char[255];
            int howManyRead = twilight.read(buffer);
            while (howManyRead != -1) {
                String s = new String(buffer, 0, howManyRead);
                Platform.runLater(()->{
                    this.appendBlackText(s);
                });
                howManyRead = twilight.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // end of business
        }
    }

    public void appendBlackText(String text) {
        this.tbOutput.appendText(text);
    }

    private void waitForProcessExit(Process start) {
        try {
            start.waitFor();
        } catch (InterruptedException e) {
            // doesn't matter
        }
        Platform.runLater(()->{
            run.stoppableProcessId.set(-1);
            run.running.set(false);
            updateResultsPanel();
        });
    }

    private List<String> composeScriptAndArguments(List<Scenario> testCases) {
        File argfile;
        File runnerAgent;
        try {
            runnerAgent = temporaryDirectory.resolve("TestRunnerAgent.py").toFile();
            argfile = File.createTempFile("argfile", ".txt", temporaryDirectory.toFile());
            createArgFile(argfile, testCases);
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

    private void createArgFile(File argfile, List<Scenario> testCases) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("--outputdir");
        lines.add(temporaryDirectory.toString());
        lines.add("-C");
        lines.add("ansi");
        for(Scenario testCase : testCases) {
            lines.add("--suite");
            lines.add(testCase.parent.getQualifiedName());
            lines.add("--test");
            lines.add(testCase.getQualifiedName());
        }
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

    public void updateResultsPanel() {
        if (run.forciblyKilled) {
            setHboxBackgroundColor(Color.LIGHTGRAY);
        } else if (run.countFailedTests > 0) {
            setHboxBackgroundColor(Color.SANDYBROWN);
        } else if (run.countPassedTests > 0) {
            if (run.isInProgress()) {
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

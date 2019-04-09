package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.ui.DeferredActions;
import cz.hudecekpetr.snowride.ui.Images;
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
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.SimpleEditableStyledDocument;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RunTab {
    public static final String INITIAL_STYLE = "-fx-font-family: Consolas; -fx-font-size: 10pt;";
    private MainForm mainForm;
    private FileChooser openScriptFileDialog;
    private TextField tbScript;
    public Run run = new Run();
    public BooleanProperty canRun = new SimpleBooleanProperty(true);
    public BooleanProperty canStop = new SimpleBooleanProperty(false);
    private StyledTextArea<String, String> tbOutput;
    public StyledTextArea<String, String> tbLog;
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
    private VBox vboxWithTags;
    private CheckBox cbWithoutTags;
    private TextField tbWithoutTags;
    private TextField tbWithTags;
    private CheckBox cbWithTags;
    public Label lblMultirun;


    public RunTab(MainForm mainForm) {
        this.mainForm = mainForm;
        this.tcpHost = new TcpHost(this, mainForm);
        this.tcpHost.start();
        this.multirunner = new Multirunner(this);
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
            return Files.createTempDirectory("Snowride");
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
        tbOutput = new StyledTextArea<String, String>( "", TextFlow::setStyle,
                INITIAL_STYLE, TextExt::setStyle,
                new SimpleEditableStyledDocument<>("", INITIAL_STYLE),
                true);
        tbOutput.setUseInitialStyleForInsertion(true);
        tbOutput.setEditable(false);
        tbOutput.setPadding(new Insets(4));
        tbLog = new StyledTextArea<String, String>( "", TextFlow::setStyle,
                INITIAL_STYLE, TextExt::setStyle,
                new SimpleEditableStyledDocument<>("", INITIAL_STYLE),
                true);
        tbLog.setEditable(false);
        VirtualizedScrollPane<StyledTextArea<String, String>> scrollPane = new VirtualizedScrollPane<>(tbOutput);
        SplitPane splitterOutput = new SplitPane(scrollPane, new VirtualizedScrollPane<>(tbLog));
        splitterOutput.setOrientation(Orientation.VERTICAL);
        Label lblScript = new Label("Script:");
        tbScript = new TextField(Settings.getInstance().runScript);
        Button bLoadScriptButton = new Button("...");
        bLoadScriptButton.setOnAction(this::loadScriptOnClick);
        HBox hboxScript = new HBox(5, lblScript, tbScript, bLoadScriptButton);
        hboxScript.setPadding(new Insets(2));
        hboxScript.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tbScript, Priority.ALWAYS);
        Button bRun = new Button("Run", new ImageView(Images.play));
        bRun.setOnAction(this::clickRun);
        bRun.disableProperty().bind(canRun.not());
        Button bStop = new Button("Stop", new ImageView(Images.stop));
        bStop.setOnAction(this::clickStop);
        bStop.disableProperty().bind(canStop.not());
        Button bLog = new Button("Log", new ImageView(Images.log));
        bLog.disableProperty().bind(Bindings.isNull(run.logFile));
        bLog.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openFile(RunTab.this.run.logFile.getValue());
            }
        });
        Button bReport = new Button("Report", new ImageView(Images.report));
        bReport.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openFile(RunTab.this.run.reportFile.getValue());
            }
        });
        bReport.disableProperty().bind(Bindings.isNull(run.reportFile));
        Button bRunAdvanced = new Button("Advanced run...", new ImageView(Images.play));
        bRunAdvanced.disableProperty().bind(canRun.not());
        ContextMenu advancedRunContextMenu = buildAdvancedRunContextMenu();
        bRunAdvanced.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                advancedRunContextMenu.hide();
                advancedRunContextMenu.show(bRunAdvanced, Side.RIGHT, 0, 0);
            }
        });
        lblMultirun = new Label("Running until failure (0 successes so far)");
        lblMultirun.managedProperty().bind(lblMultirun.visibleProperty());
        lblMultirun.setVisible(false);
        HBox hboxButtons = new HBox(5, bRun, bStop, bLog, bReport, bRunAdvanced, lblMultirun);
        hboxButtons.setAlignment(Pos.CENTER_LEFT);
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
        cbWithTags = new CheckBox("Run only tests with tags:");
        cbWithTags.setSelected(Settings.getInstance().cbWithTags);
        tbWithTags = new TextField(Settings.getInstance().tbWithTags);
        tbWithTags.setFont(MainForm.TEXT_EDIT_FONT);
        vboxWithTags = new VBox(2, cbWithTags, tbWithTags);
        cbWithoutTags = new CheckBox("Don't run tests with tags:");
        cbWithoutTags.setSelected(Settings.getInstance().cbWithoutTags);
        tbWithoutTags = new TextField(Settings.getInstance().tbWithoutTags);
        tbWithoutTags.setFont(MainForm.TEXT_EDIT_FONT);
        VBox vboxWithoutTags = new VBox(2, cbWithoutTags, tbWithoutTags);
        HBox hboxTags = new HBox(5, vboxWithTags, vboxWithoutTags);
        HBox.setHgrow(vboxWithTags, Priority.ALWAYS);
        HBox.setHgrow(vboxWithoutTags, Priority.ALWAYS);
        VBox vboxTabRun = new VBox(0, hboxScript, hboxButtons, hboxArguments, hboxTags, hboxExecutionLine, splitterOutput);
        VBox.setVgrow(splitterOutput, Priority.ALWAYS);
        tabRun = new Tab("Run", vboxTabRun);
        tabRun.setClosable(false);
        return tabRun;
    }

    Multirunner multirunner;

    private ContextMenu buildAdvancedRunContextMenu() {
        MenuItem untilFailureOrStop = new MenuItem("...until failure");
        MenuItem runOnlyFailedTests = new MenuItem("...only failed tests");
        untilFailureOrStop.disableProperty().bind(canRun.not());
        untilFailureOrStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                multirunner.runUntilFailure();
            }
        });
        runOnlyFailedTests.disableProperty().bind(canRun.not());
        runOnlyFailedTests.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                mainForm.selectFailedTests(mainForm.getProjectTree().getRoot().getValue());
                clickRun(event);
            }
        });
        return new ContextMenu(untilFailureOrStop, runOnlyFailedTests);
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
                multirunner.manuallyStopped();
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
        flushIntoTbOutput(text + "\n", "-fx-font-style: italic");
    }
    public void appendGreenTextNoNewline(String text) {
        flushIntoTbOutput(text, "-fx-font-style: italic");
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
            if (mainForm.canSave.get()) {
                ButtonType save_all = new ButtonType("Save all");
                ButtonType run_without_saving = new ButtonType("Run without saving");
                ButtonType cancel = new ButtonType("Cancel");
                ButtonType decision = new Alert(Alert.AlertType.CONFIRMATION,
                        "There are unsaved changes. Save all before running?",
                        save_all,
                        run_without_saving,
                        cancel).showAndWait().orElse(cancel);
                if (decision == save_all) {
                    mainForm.saveAll(null);
                } else if (decision == run_without_saving) {
                    // Proceed as normal.
                } else {
                    // cancel
                    return;
                }
            }
            // Back to std. image
            mainForm.getProjectTree().getRoot().getValue().selfAndDescendantHighElements().forEach((he)->{
                if (he instanceof Scenario && ((Scenario) he).isTestCase()) {
                    ((Scenario)he).markTestStatus(TestResult.NOT_YET_RUN);
                }
            });

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
            tbOutput.clear();
            appendGreenTextNoNewline("> " + String.join(" ", command));
            processBuilder.command(command);
            processBuilder.directory(runnerDirectory);
            processBuilder.redirectErrorStream(true);
            Process start = processBuilder.start();
            run.stoppableProcessId.setValue(-1);
            run.running.set(true);
            multirunner.actuallyStarted();
            executor.execute(() -> readFromOutput(start.getInputStream()));
            executor.execute(() -> this.waitForProcessExit(start));


        } catch (Exception ex) {
            tbOutput.replaceText(Extensions.toStringWithTrace(ex));
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
            if (element.checkbox.isSelected() && ((Scenario)element).isTestCase()) {
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
                    this.appendAnsiText(s);
                });
                howManyRead = twilight.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // end of business
        }
    }

    AnsiOutputStream ansiOutputStream = new AnsiOutputStream();

    public void appendAnsiText(String text) {
        ansiOutputStream.add(text);
        ansiOutputStream.flushInto(this::flushIntoTbOutputPlusDefault);
    }

    private void flushIntoTbOutputPlusDefault(String text, String additionalStyle) {
        flushIntoTbOutput(text, INITIAL_STYLE + additionalStyle);
    }

    private void flushIntoTbOutput(String text, String style) {
        int current = this.tbOutput.getLength();
        this.tbOutput.appendText(text);
        int currentAfter = this.tbOutput.getLength();
        this.tbOutput.setStyle(current, currentAfter, style);
        this.tbOutput.showParagraphAtBottom(Integer.MAX_VALUE);
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
            multirunner.endedNormally();
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
        result.addAll(Arrays.asList(args));
        result.add(((FolderSuite) mainForm.getProjectTree().getRoot().getValue()).directoryPath.toString());
        return result;
    }

    private void createArgFile(File argfile, List<Scenario> testCases) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("--outputdir");
        lines.add(temporaryDirectory.toString());
        lines.add("-C");
        lines.add("ansi");
        if (cbWithTags.isSelected()) {
            for (String tag : StringUtils.splitByWholeSeparator(tbWithTags.getText(), ",")) {
                lines.add("--include");
                lines.add(tag.trim());
            }
        }
        if (cbWithoutTags.isSelected()) {
            for (String tag : StringUtils.splitByWholeSeparator(tbWithoutTags.getText(), ",")) {
                lines.add("--exclude");
                lines.add(tag.trim());
            }
        }
        for(Scenario testCase : testCases) {
            lines.add("--suite");
            lines.add(testCase.parent.getQualifiedName());
            lines.add("--test");
            lines.add(testCase.getQualifiedName());
        }
        if (Settings.getInstance().additionalFolders != null) {
            String[] folders = StringUtils.splitByWholeSeparator(Settings.getInstance().additionalFolders, "\n");
            for (String folder : folders) {
                lines.add("--pythonpath");
                lines.add(folder.trim());
            }
        }
        FileUtils.writeLines(argfile, lines);
    }

    private void planRobots() {
        // Don't plant yet. We don't have checkboxes yet.
    }

    private void rememberRunPageSettings() {
        Settings.getInstance().runArguments = tbArguments.getText();
        Settings.getInstance().runScript = tbScript.getText();
        Settings.getInstance().cbWithoutTags = cbWithoutTags.isSelected();
        Settings.getInstance().cbWithTags = cbWithTags.isSelected();
        Settings.getInstance().tbWithoutTags = tbWithoutTags.getText();
        Settings.getInstance().tbWithTags = tbWithTags.getText();
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

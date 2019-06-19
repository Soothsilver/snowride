package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Tag;
import cz.hudecekpetr.snowride.ui.DeferredActions;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.ui.settings.SettingsWindow;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.SimpleEditableStyledDocument;
import org.zeroturnaround.process.ProcessUtil;
import org.zeroturnaround.process.Processes;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RunTab {
    public static final String INITIAL_STYLE = "-fx-font-family: monospace; -fx-font-size: 10pt;";
    public Run run = new Run();
    public BooleanProperty canRun = new SimpleBooleanProperty(true);
    public BooleanProperty canStop = new SimpleBooleanProperty(false);
    public StyledTextArea<String, String> tbLog;
    public Label lblKeyword;
    public Label lblMultirun;
    public SimpleStringProperty runCaption = new SimpleStringProperty("Run");
    Multirunner multirunner;
    AnsiOutputStream ansiOutputStream = new AnsiOutputStream();
    private MainForm mainForm;
    private FileChooser openScriptFileDialog;
    private TextField tbScript;
    private StyledTextArea<String, String> tbOutput;
    private Tab tabRun;
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
    public SimpleIntegerProperty numberOfSuccessesToStop;

    public RunTab(MainForm mainForm) {
        this.mainForm = mainForm;
        this.tcpHost = new TcpHost(this, mainForm);
        this.tcpHost.start();
        this.numberOfSuccessesToStop = new SimpleIntegerProperty(Settings.getInstance().numberOfSuccessesBeforeEnd);
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
        tbOutput = new StyledTextArea<String, String>("", TextFlow::setStyle,
                INITIAL_STYLE, TextExt::setStyle,
                new SimpleEditableStyledDocument<>("", INITIAL_STYLE),
                true);
        tbOutput.setUseInitialStyleForInsertion(true);
        tbOutput.setEditable(false);
        tbOutput.setPadding(new Insets(4));
        tbLog = new StyledTextArea<String, String>("", TextFlow::setStyle,
                INITIAL_STYLE, TextExt::setStyle,
                new SimpleEditableStyledDocument<>("", INITIAL_STYLE),
                true);
        tbLog.setEditable(false);
        tbLog.setStyle("-fx-background-color: -fx-control-inner-background;");
        tbOutput.setStyle("-fx-background-color: -fx-control-inner-background");
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
        bRun.textProperty().bind(runCaption);
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
        lblTotalTime.setPadding(new Insets(0, 0, 0, 10));
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
        cbWithTags.selectedProperty().addListener(this::tagsCheckboxChanged);
        tbWithTags = new TextField(Settings.getInstance().tbWithTags);
        tbWithTags.setFont(MainForm.TEXT_EDIT_FONT);
        tbWithTags.textProperty().addListener(this::tagsTextChanged);
        vboxWithTags = new VBox(2, cbWithTags, tbWithTags);
        cbWithoutTags = new CheckBox("Don't run tests with tags:");
        cbWithoutTags.setSelected(Settings.getInstance().cbWithoutTags);
        cbWithoutTags.selectedProperty().addListener(this::tagsCheckboxChanged);
        tbWithoutTags = new TextField(Settings.getInstance().tbWithoutTags);
        tbWithoutTags.setFont(MainForm.TEXT_EDIT_FONT);
        tbWithoutTags.textProperty().addListener(this::tagsTextChanged);
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
    private void tagsTextChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        maybeRunNumberChanged();
    }
    private void tagsCheckboxChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
    {
        maybeRunNumberChanged();
    }
    private ContextMenu buildAdvancedRunContextMenu() {
        MenuItem untilFailureOrStop = new MenuItem("...until failure");
        MenuItem untilFailureOrXSuccess = new MenuItem("");
        untilFailureOrXSuccess.textProperty().bind(Bindings.concat("...until failure or ", numberOfSuccessesToStop , " successes"));
        MenuItem runOnlyFailedTests = new MenuItem("...only failed tests");
        CustomMenuItem runThenDeselect = new CustomMenuItem(new Label("...and then deselect passing tests"));
        untilFailureOrStop.disableProperty().bind(canRun.not());
        untilFailureOrStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                multirunner.runUntilFailure(Multirunner.BLADES_OF_GRASS_ON_SWEET_APPLE_ACRES);
            }
        });
        untilFailureOrXSuccess.disableProperty().bind(canRun.not());
        untilFailureOrXSuccess.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                multirunner.runUntilFailure(numberOfSuccessesToStop.getValue());
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
        runThenDeselect.disableProperty().bind(canRun.not());
        Tooltip.install(runThenDeselect.getContent(), new Tooltip("Snowride will clear the selection of tests that pass so that only failed tests remain selected to be run next time."));
        runThenDeselect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clickRun(event, true);
            }
        });
        return new ContextMenu(untilFailureOrStop, untilFailureOrXSuccess, runOnlyFailedTests, runThenDeselect);
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
        clickRun(actionEvent, false);
    }

    private boolean thenDeselectPassingTests = false;

    public void clickRun(ActionEvent actionEvent, boolean thenDeselectPassingTests) {
        startANewRun(thenDeselectPassingTests);
    }

    public void startANewRun(boolean thenDeselectPassingTests) {
        try {
            this.thenDeselectPassingTests = thenDeselectPassingTests;
            List<Scenario> testCases = getCheckedTestCases();
            if (testCases.size() == 0) {
                String warningText = "You didn't choose any test case. Do you want to run the entire suite?";
                if (!allTestsAreChosen) {
                    warningText = "Do you want to run all test cases with the chosen tags (" + numberOfTestsToBeRun + " test cases in total)?";
                }
                Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION,
                        warningText, ButtonType.YES, ButtonType.NO).showAndWait();
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
            mainForm.getProjectTree().getRoot().getValue().selfAndDescendantHighElements().forEach((he) -> {
                if (he instanceof Scenario && ((Scenario) he).isTestCase()) {
                    ((Scenario) he).markTestStatus(TestResult.NOT_YET_RUN);
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
            Process start;
            try {
                start = processBuilder.start();
            } catch (IOException exception) {
                throw new RuntimeException("Snowride couldn't start process '" + runner + "'. Are you sure you put an executable file name in the 'Script' field?", exception);
            }
            run.stoppableProcessId.setValue(-1);
            run.running.set(true);
            multirunner.actuallyStarted();
            executor.execute(() -> readFromOutput(start.getInputStream()));
            executor.execute(() -> this.waitForProcessExit(start));


        } catch (Exception ex) {
            tbOutput.replaceText(Extensions.toStringWithTrace(ex));
            new Alert(Alert.AlertType.WARNING,ex.getMessage(), ButtonType.CLOSE).showAndWait();
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
            if (element.checkbox.isSelected() && ((Scenario) element).isTestCase()) {
                checkedStuff.add((Scenario) element);
            }
        }
        for (HighElement child : element.children) {
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
                Platform.runLater(() -> {
                    this.appendAnsiText(s);
                });
                howManyRead = twilight.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // end of business
        }
    }

    public void appendAnsiText(String text) {
        ansiOutputStream.addFromOutside(text);
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
        Platform.runLater(() -> {
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
            if (!runnerAgent.exists()) {
                InputStream runnerAgentDataStream = this.getClass().getResourceAsStream("/TestRunnerAgent.py");
                byte[] testRunnerAgentData = IOUtils.toByteArray(runnerAgentDataStream);
                Files.write(runnerAgent.toPath(), testRunnerAgentData);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> result = new ArrayList<>();
        result.add(this.tbScript.getText());
        result.add("--argumentfile");
        result.add(argfile.toString());
        result.add("--listener");
        result.add(runnerAgent.toString() + ":" + tcpHost.portNumber + ":False");
        for(String path : StringUtils.split( Settings.getInstance().additionalFolders,  '\n')) {
            String trimPath = path.trim();
            if (!trimPath.isEmpty()) {
                result.add("--pythonpath");
                result.add(trimPath);
            }
        }
        String[] args = StringUtils.splitByWholeSeparator(this.tbArguments.getText(), " ");
        result.addAll(Arrays.asList(args));
        result.add(mainForm.getRootDirectoryElement().directoryPath.toString());
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
        for (Scenario testCase : testCases) {
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
            throw new RuntimeException(e.getMessage() + "\n\nIt's possible you don't have Windows set up to automatically open HTML files in a browser. You can do that in 'Default Apps' or with 'Choose default program...'.", e);
        }
    }

    private void loadScriptOnClick(ActionEvent actionEvent) {
        File file = openScriptFileDialog.showOpenDialog(mainForm.getStage());
        if (file != null) {
            tbScript.setText(file.getAbsoluteFile().toString());
        }
    }

    public boolean suppressRunNumberChangeNotifications = false;
    private boolean allTestsAreChosen = false;
    private int numberOfTestsToBeRun = 0;

    public void maybeRunNumberChanged() {
        if (suppressRunNumberChangeNotifications) {
            return;
        }
        int[] totalSelected = new int[]{0};
        int[] totalTests = new int[]{0};
        boolean tagsRequired = cbWithTags.isSelected();
        boolean tagsIgnored = cbWithoutTags.isSelected();
        boolean[] anythingIsChecked = new boolean[]{false};
        List<Tag> whatTagsMustBe = new ArrayList<>();
        List<Tag> whatTagsCannotBe = new ArrayList<>();
        if (tagsRequired) {
            for (String tag : StringUtils.split(tbWithTags.getText(), ',')) {
                whatTagsMustBe.add(new Tag(tag.trim().toLowerCase(), null, null));
            }
        }
        if (tagsIgnored) {
            for (String tag : StringUtils.split(tbWithoutTags.getText(), ',')) {
                whatTagsCannotBe.add(new Tag(tag.trim().toLowerCase(), null, null));
            }
        }
        mainForm.getRootElement().selfAndDescendantHighElements().forEach(he -> {
            if (he.checkbox.isSelected() && he instanceof Scenario && ((Scenario) he).isTestCase()) {
                anythingIsChecked[0] = true;
            }
        });
        boolean ignoreCheckboxes = !anythingIsChecked[0];
        mainForm.getRootElement().selfAndDescendantHighElements().forEach(he -> {
            if (he instanceof Scenario) {
                Scenario s = (Scenario) he;
                if (s.isTestCase()) {
                    totalTests[0]++;
                    if (ignoreCheckboxes || he.checkbox.isSelected()) {
                        if (!tagsRequired || Extensions.containsAny(s.actualTags, whatTagsMustBe)) {
                            if (!tagsIgnored || !Extensions.containsAny(s.actualTags, whatTagsCannotBe)) {
                                totalSelected[0]++;
                            }
                        }
                    }
                }
            }
        });
        numberOfTestsToBeRun = totalSelected[0];
        if (totalSelected[0] == totalTests[0]) {
            allTestsAreChosen = true;
            runCaption.set("Run");
        } else {
            allTestsAreChosen = false;
            runCaption.set("Run " + Extensions.englishCount(totalSelected[0], "test", "tests"));
        }
    }

    public void possiblyDeselectPassingTest(Scenario endingTest) {
        if (thenDeselectPassingTests) {
            endingTest.checkbox.setSelected(false);
        }
    }
}

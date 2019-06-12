package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.filesystem.Filesystem;
import cz.hudecekpetr.snowride.filesystem.FilesystemWatcher;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.runner.RunTab;
import cz.hudecekpetr.snowride.runner.TestResult;
import cz.hudecekpetr.snowride.semantics.externallibraries.ReloadExternalLibraries;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.ExternalResourcesElement;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.ISuite;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.tree.UltimateRoot;
import cz.hudecekpetr.snowride.ui.about.AboutChangelog;
import cz.hudecekpetr.snowride.ui.about.AboutKeyboardShortcuts;
import cz.hudecekpetr.snowride.ui.about.AboutSnowride;
import cz.hudecekpetr.snowride.ui.settings.SettingsWindow;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.controlsfx.control.NotificationPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainForm {
    public static final Font BIGGEST_FONT = new Font("System Regular", 18);
    public static final Font BIGGER_FONT = new Font("System Regular", 14);
    public static final Font TEXT_EDIT_FONT = new Font("Courier New", 12);
    public static MainForm INSTANCE;
    public static DocumentationPopup documentationPopup = new DocumentationPopup();
    private final SerializingTab serializingTab;
    private final ErrorsTab errorsTab;
    private final TabPane tabs;
    private final Tab tabTextEdit;
    private final TextEditTab textEditTab;
    private final NotificationPane notificationPane;
    public BooleanProperty canSave = new SimpleBooleanProperty(false);
    public RunTab runTab;
    public GateParser gateParser = new GateParser();
    public NavigationStack navigationStack = new NavigationStack();
    public LongRunningOperation projectLoad = new LongRunningOperation();
    boolean switchingTextEditContents = false;
    boolean humanInControl = true;
    BooleanProperty canNavigateBack = new SimpleBooleanProperty(false);
    BooleanProperty canNavigateForwards = new SimpleBooleanProperty(false);
    private Stage stage;
    private TreeView<HighElement> projectTree;
    private SearchSuites searchSuitesAutoCompletion;
    private Filesystem filesystem;
    private SeparatorMenuItem separatorBeforeRecentProjects;
    private SeparatorMenuItem separatorAfterRecentProjects;
    private DirectoryChooser openFolderDialog;
    private Menu projectMenu;
    private TextField tbSearchTests;
    private ContextMenu treeContextMenu;
    private GridTab gridTab;
    private ExecutorService projectLoader = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService endTheToastExecutor = Executors.newSingleThreadScheduledExecutor();
    private String notificationShowingWhat = null;
    private final ToolBar toolBar;
    private Button bStop;

    public MainForm(Stage stage) {
        INSTANCE = this;
        this.stage = stage;
        filesystem = new Filesystem(this);
        canNavigateBack.bindBidirectional(navigationStack.canNavigateBack);
        canNavigateForwards.bindBidirectional(navigationStack.canNavigateForwards);
        stage.setTitle("Snowride");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                boolean abort = maybeOfferSaveDiscardOrCancel();
                if (abort) {
                    event.consume();
                    return;
                }
                System.exit(0);
            }
        });
        runTab = new RunTab(this);
        Tab tabRun = runTab.createTab();
        MenuBar mainMenu = buildMainMenu();
        toolBar = buildToolBar();
        updateAdditionalToolbarButtonsVisibility();
        textEditTab = new TextEditTab(this);
        tabTextEdit = textEditTab.createTab();
        gridTab = new GridTab(this);
        Tab tabGrid = gridTab.createTab();
        serializingTab = new SerializingTab(this);
        Tab tabSerializing = serializingTab.createTab();
        VBox searchableTree = createLeftPane();
        errorsTab = new ErrorsTab(this);
        Tab tabErrors = errorsTab.tab;
        tabs = new TabPane(tabTextEdit, tabGrid, tabRun,
                // Serializing is now mostly stable, so we don't need the debugging tab:
                //  tabSerializing,
                tabErrors);
        tabs.getSelectionModel().select(tabGrid);
        tabs.getSelectionModel().selectedItemProperty().addListener(serializingTab::selTabChanged);
        tabs.getSelectionModel().selectedItemProperty().addListener(textEditTab::selTabChanged);
        tabs.getSelectionModel().selectedItemProperty().addListener(this::selectedTabChanged);
        SplitPane treeAndGrid = new SplitPane(searchableTree, tabs);
        treeAndGrid.setOrientation(Orientation.HORIZONTAL);
        SplitPane.setResizableWithParent(searchableTree, false);
        treeAndGrid.setDividerPosition(0, 0.3);
        VBox vBox = new VBox(mainMenu, toolBar, treeAndGrid);
        notificationPane = new NotificationPane(vBox);
        VBox.setVgrow(treeAndGrid, Priority.ALWAYS);
        Scene scene = new Scene(notificationPane, Settings.getInstance().width, Settings.getInstance().height);
        scene.getStylesheets().add(getClass().getResource("/snow.css").toExternalForm());
        addGlobalShortcuts(scene);
        stage.setScene(scene);
        if (Settings.getInstance().x != -1) {
            stage.setX(Settings.getInstance().x);
        }
        if (Settings.getInstance().y != -1) {
            stage.setY(Settings.getInstance().y);
        }
        stage.setMaximized(Settings.getInstance().maximized);
        stage.xProperty().addListener((observable, oldValue, newValue) -> mainWindowCoordinatesChanged());
        stage.yProperty().addListener((observable, oldValue, newValue) -> mainWindowCoordinatesChanged());
        stage.widthProperty().addListener((observable, oldValue, newValue) -> mainWindowCoordinatesChanged());
        stage.heightProperty().addListener((observable, oldValue, newValue) -> mainWindowCoordinatesChanged());
        stage.maximizedProperty().addListener((observable, oldValue, newValue) -> mainWindowCoordinatesChanged());
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/Snowflake3.png")));
    }

    private void selectedTabChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        if (getFocusedElement() != null) {
            if (oldValue == tabTextEdit) {
                getFocusedElement().applyText();
            }
            if (newValue == gridTab.getTabGrid()) {
                gridTab.loadElement(getFocusedElement());
            }
        }
    }

    public HighElement getFocusedElement() {
        TreeItem<HighElement> focusedItem = getProjectTree().getFocusModel().getFocusedItem();
        if (focusedItem != null) {
            return focusedItem.getValue();
        } else {
            return null;
        }
    }

    /**
     * Gets the folder suite that's the root of the project.
     */
    public UltimateRoot getRootElement() {
        return (UltimateRoot) getProjectTree().getRoot().getValue();
    }

    public TreeView<HighElement> getProjectTree() {
        return projectTree;
    }

    private void addGlobalShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.F5 || event.getCode() == KeyCode.F8) {
                    runTab.clickRun(null);
                    event.consume();
                } else if (event.getCode() == KeyCode.LEFT && event.isControlDown()) {
                    goBack(null);
                    event.consume();
                } else if (event.getCode() == KeyCode.RIGHT && event.isControlDown()) {
                    goForwards(null);
                    event.consume();
                } else if (event.getCode() == KeyCode.N && event.isControlDown()) {
                    tbSearchTests.requestFocus();
                    searchSuitesAutoCompletion.trigger();
                    event.consume();
                }
            }
        });
    }

    public void changeOccurredTo(HighElement whatChanged, LastChangeKind how) {
        whatChanged.unsavedChanges = how;
        whatChanged.treeNode.setValue(null);
        whatChanged.treeNode.setValue(whatChanged);
        // TODO It's possible the user change the tag which should cause the number of tests to update
        // but for performance reasons we choose not to update the number here.
        canSave.set(true);
    }

    private void mainWindowCoordinatesChanged() {
        Settings.getInstance().x = stage.getX();
        Settings.getInstance().y = stage.getY();
        Settings.getInstance().width = stage.getWidth();
        Settings.getInstance().height = stage.getHeight();
        Settings.getInstance().maximized = stage.isMaximized();
        Settings.getInstance().save();
    }

    private VBox createLeftPane() {
        tbSearchTests = new TextField();
        tbSearchTests.setPromptText("Search for tests, keywords or suites (Ctrl+N)...");
        searchSuitesAutoCompletion = new SearchSuites(this);
        searchSuitesAutoCompletion.bind(tbSearchTests);
        projectTree = new TreeView<HighElement>();
        projectTree.setFixedCellSize(16);
        projectTree.setShowRoot(false);
        projectTree.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.SPACE) {
                    TreeItem<HighElement> focusedItem = projectTree.getFocusModel().getFocusedItem();
                    if (focusedItem != null) {
                        HighElement element = focusedItem.getValue();
                        withUpdateSuppression(() -> {
                            invertCheckboxes(element);
                        });
                    }
                }
            }
        });
        projectTree.getFocusModel().focusedItemProperty().addListener(new ChangeListener<TreeItem<HighElement>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<HighElement>> observable, TreeItem<HighElement> oldValue, TreeItem<HighElement> newValue) {
                onFocusTreeNode(oldValue);
                focusTreeNode(newValue);
            }
        });
        treeContextMenu = new ContextMenu();
        treeContextMenu.getItems().add(new MenuItem("A"));


        projectTree.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {

                treeContextMenu.getItems().clear();
                TreeItem<HighElement> focused = projectTree.getFocusModel().getFocusedItem();
                if (focused != null) {
                    treeContextMenu.getItems().addAll(createContextMenuFor(focused));
                }
            }
        });

        projectTree.setContextMenu(treeContextMenu);
        /*
        projectTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    treeContextMenu.hide();
                }
            }
        });*/
        ProgressBar progressBar = new ProgressBar();
        progressBar.visibleProperty().bind(projectLoad.progress.lessThan(1));
        progressBar.progressProperty().bind(projectLoad.progress);
        StackPane stackPane = new StackPane(projectTree, progressBar);
        StackPane.setAlignment(progressBar, Pos.CENTER);
        VBox vBox = new VBox(tbSearchTests, stackPane);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        return vBox;
    }

    private void onFocusTreeNode(TreeItem<HighElement> oldValue) {
        if (oldValue != null) {
            oldValue.getValue().applyText();
        }
    }

    private List<MenuItem> createContextMenuFor(TreeItem<HighElement> forWhat) {
        List<MenuItem> menu = new ArrayList<>();
        HighElement element = forWhat.getValue();
        if (element instanceof FolderSuite) {
            MenuItem new_folder_suite = new MenuItem("New folder");
            new_folder_suite.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String name = TextFieldForm.askForText("Create new folder suite", "Enter folder name:", "Create new folder", "");
                    if (name != null) {
                        filesystem.createNewFolderInTree((FolderSuite) element, name);
                    }
                }
            });
            menu.add(new_folder_suite);
            MenuItem new_file_suite = new MenuItem("New file");
            new_file_suite.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String name = TextFieldForm.askForText("Create new file", "Enter file name (without .robot extension):", "Create new file", "");
                    if (name != null) {
                        try {
                            filesystem.createNewRobotFile((FolderSuite) element, name);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            menu.add(new_file_suite);
        }
        maybeAddSeparator(menu);
        if (element instanceof FileSuite) {
            MenuItem new_test_case = new MenuItem("New test case");
            new_test_case.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String name = TextFieldForm.askForText("New test case", "Test case name:", "Create new test case", "");
                    if (name != null) {
                        ((FileSuite) element).createNewChild(name, true, MainForm.this);
                        changeOccurredTo(element, LastChangeKind.STRUCTURE_CHANGED);
                    }
                }
            });
            menu.add(new_test_case);
            MenuItem open_in_external_editor = new MenuItem("Open in external editor");
            open_in_external_editor.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        Desktop.getDesktop().edit(((FileSuite) element).file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            menu.add(open_in_external_editor);
        }
        if (element instanceof ISuite) {
            MenuItem new_user_keyword = new MenuItem("New user keyword");
            new_user_keyword.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String name = TextFieldForm.askForText("New user keyword", "Keyword name:", "Create new user keyword", "");
                    if (name != null) {
                        ((ISuite) element).createNewChild(name, false, MainForm.this);
                        changeOccurredTo(element, LastChangeKind.STRUCTURE_CHANGED);
                    }
                }
            });
            menu.add(new_user_keyword);
        }
        maybeAddSeparator(menu);
        if (element instanceof FolderSuite || element instanceof FileSuite) {
            MenuItem select_all_tests = new MenuItem("Select all tests");
            select_all_tests.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setCheckboxes(element, true);
                }
            });
            menu.add(select_all_tests);
            if (element.selfAndDescendantHighElements().anyMatch(he -> he instanceof Scenario && ((Scenario) he).lastTestResult == TestResult.FAILED)) {
                MenuItem select_failed_tests = new MenuItem("Select failed tests only");
                select_failed_tests.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        withUpdateSuppression(() -> {
                            selectFailedTests(element);
                        });
                    }
                });
                menu.add(select_failed_tests);
            }
            MenuItem deselect_all_tests = new MenuItem("Deselect all tests");
            deselect_all_tests.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setCheckboxes(element, false);
                }
            });
            menu.add(deselect_all_tests);
        }
        if (element != getRootElement() && element.parent != getRootElement()
                && element.parent != null && !(element.parent instanceof ExternalResourcesElement)) {
            maybeAddSeparator(menu);
            // Everything except for root directories and the two special elements can be deleted or renamed
            MenuItem rename = new MenuItem("Rename");
            rename.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String newName = TextFieldForm.askForText("Rename " + element, "New name:", "Rename", element.getShortName());
                    if (newName != null) {
                        element.renameSelfTo(newName, MainForm.this);
                    }
                }
            });
            menu.add(rename);
            MenuItem delete = new MenuItem("Delete");
            delete.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    ButtonType deleteAnswer = new ButtonType("Delete");
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + element + "?",
                            deleteAnswer,
                            ButtonType.NO);
                    if (alert.showAndWait().orElse(ButtonType.NO) == deleteAnswer) {
                        delete(element);
                    }
                }
            });
            menu.add(delete);
        }
        if (menu.size() == 0) {
            MenuItem menuItemNothing = new MenuItem("(you can't do anything with this tree node)");
            menuItemNothing.setDisable(true);
            menu.add(menuItemNothing);
        }
        return menu;
    }

    public void selectFailedTests(HighElement element) {
        element.checkbox.setSelected(element instanceof Scenario && ((Scenario) element).lastTestResult == TestResult.FAILED);
        for (HighElement child : element.children) {
            selectFailedTests(child);
        }
    }

    /**
     * Runs a block of code while suppressing some changes to UI elements during that time, then updates the UI afterwards.
     * This improves performance.
     */
    private void withUpdateSuppression(Runnable block) {
        runTab.suppressRunNumberChangeNotifications = true;
        block.run();
        runTab.suppressRunNumberChangeNotifications = false;
        runTab.maybeRunNumberChanged();
    }

    private void invertCheckboxes(HighElement element) {
        element.checkbox.setSelected(!element.checkbox.isSelected());
        for (HighElement child : element.children) {
            invertCheckboxes(child);
        }
    }

    private void setCheckboxes(HighElement element, boolean shouldBeChecked) {
        withUpdateSuppression(() -> {
            setCheckboxesRecursive(element, shouldBeChecked);
        });
    }

    private void setCheckboxesRecursive(HighElement element, boolean shouldBeChecked) {
        element.checkbox.setSelected(shouldBeChecked);
        for (HighElement child : element.children) {
            setCheckboxesRecursive(child, shouldBeChecked);
        }
    }

    private void delete(HighElement element) {
        element.deleteSelf(this);
    }

    private void maybeAddSeparator(List<MenuItem> menu) {
        if (menu.size() > 0 && !(menu.get(menu.size() - 1) instanceof SeparatorMenuItem)) {
            menu.add(new SeparatorMenuItem());
        }
    }

    private ToolBar buildToolBar() {
        Button bNavigateBack = new Button(null, loadIcon(Images.goLeft));
        bNavigateBack.setTooltip(new Tooltip("Navigate back"));
        Button bNavigateForwards = new Button(null, loadIcon(Images.goRight));
        bNavigateForwards.setTooltip(new Tooltip("Navigate forwards"));
        Button bSaveAll = new Button("Save all", loadIcon(Images.save));
        Button bRun = new Button("Run", loadIcon(Images.play));
        bRun.textProperty().bind(runTab.runCaption);
        bStop = new Button("Stop", loadIcon(Images.stop));
        bNavigateBack.disableProperty().bind(canNavigateBack.not());
        bNavigateBack.setOnAction(this::goBack);
        bNavigateForwards.disableProperty().bind(canNavigateForwards.not());
        bNavigateForwards.setOnAction(this::goForwards);
        bSaveAll.disableProperty().bind(canSave.not());
        bSaveAll.setOnAction(this::saveAll);
        bRun.disableProperty().bind(runTab.canRun.not());
        bRun.setOnAction(runTab::clickRun);
        bStop.disableProperty().bind(runTab.canStop.not());
        bStop.setOnAction(runTab::clickStop);
        ToolBar toolBar = new ToolBar(bNavigateBack, bNavigateForwards, bSaveAll, bRun, bStop);
        return toolBar;
    }

    public void updateAdditionalToolbarButtonsVisibility() {
        int removeAt = toolBar.getItems().indexOf(bStop) + 1;
        while (toolBar.getItems().size() > removeAt) {
            toolBar.getItems().remove(removeAt);
        }
        if (Settings.getInstance().toolbarReloadAll) {
            Button bReloadAll = new Button("Reload all", loadIcon(Images.refresh));
            bReloadAll.setTooltip(new Tooltip("Reloads the current Robot project as though you restarted Snowride."));
            bReloadAll.setOnAction(this::reloadAll);
            toolBar.getItems().add(bReloadAll);
        }
        if (Settings.getInstance().toolbarDeselectEverything) {
            Button bDeselectAll = new Button("Deselect all");
            bDeselectAll.setTooltip(new Tooltip("Deselects all tests so that everything would be run the next time you run tests."));
            bDeselectAll.setOnAction(this::deselectAll);
            toolBar.getItems().add(bDeselectAll);
        }
    }

    private void deselectAll(ActionEvent actionEvent) {
        setCheckboxes(getRootElement(), false);
    }

    public void reloadAll(ActionEvent actionEvent) {
        loadProjectFromFolder(new File(Settings.getInstance().lastOpenedProject));
    }

    // Hello
    private ImageView loadIcon(Image image) {
        return new ImageView(image);
    }

    private void focusTreeNode(TreeItem<HighElement> focusedNode) {
        if (focusedNode != null) {
            if (humanInControl) {
                navigationStack.standardEnter(focusedNode.getValue());
            }
            reloadElementIntoTabs(focusedNode.getValue());
        }
    }

    public void reloadElementIntoTabs(HighElement element) {
        reloadElementIntoTabs(element, true);
    }

    public void reloadElementIntoTabs(HighElement element, boolean andSwitchToGrid) {
        switchingTextEditContents = true;
        textEditTab.loadElement(element);
        gridTab.loadElement(element);
        serializingTab.loadElement(element);
        switchingTextEditContents = false;
        if (element != null && andSwitchToGrid) {
            tabs.getSelectionModel().select(gridTab.getTabGrid());
        }
    }

    public void saveAll(ActionEvent event) {
        try {
            projectTree.getRoot().getValue().saveAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        canSave.setValue(false);
    }

    private MenuBar buildMainMenu() {
        MenuBar mainMenu = new MenuBar();
        // --------- Project
        projectMenu = new Menu("Project");

        MenuItem bLoadArbitrary = new MenuItem("Load directory...", loadIcon(Images.open));
        bLoadArbitrary.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        bLoadArbitrary.setOnAction(this::openDirectory);
        openFolderDialog = new DirectoryChooser();

        MenuItem bSaveAll = new MenuItem("Save all", loadIcon(Images.save));
        bSaveAll.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        bSaveAll.setOnAction(this::saveAll);
        bSaveAll.disableProperty().bind(canSave.not());

        MenuItem bSettings = new MenuItem("Settings", loadIcon(Images.keywordIcon));
        bSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SettingsWindow settingsWindow = new SettingsWindow(MainForm.this);
                settingsWindow.show();
            }
        });
        MenuItem bReloadAll = new MenuItem("Reload everything", loadIcon(Images.refresh));
        bReloadAll.setOnAction(this::reloadAll);
        MenuItem bReload = new MenuItem("Reload external libraries");
        bReload.setOnAction(event -> reloadExternalLibraries());

        MenuItem bExit = new MenuItem("Exit Snowride", loadIcon(Images.exit));
        bExit.setOnAction(event -> System.exit(0));
        separatorBeforeRecentProjects = new SeparatorMenuItem();
        separatorAfterRecentProjects = new SeparatorMenuItem();
        projectMenu.getItems().addAll(bLoadArbitrary, bSaveAll, separatorBeforeRecentProjects, separatorAfterRecentProjects, bSettings, bReloadAll, bReload, bExit);
        refreshRecentlyOpenMenu();

        MenuItem back = new MenuItem("Navigate back", loadIcon(Images.goLeft));
        back.disableProperty().bind(canNavigateBack.not());
        back.setAccelerator(new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN));
        back.setOnAction(this::goBack);

        MenuItem forwards = new MenuItem("Navigate forwards", loadIcon(Images.goRight));
        forwards.setOnAction(this::goForwards);
        forwards.setAccelerator(new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN));
        forwards.disableProperty().bind(canNavigateForwards.not());

        Menu navigateMenu = new Menu("Navigate", null, back, forwards);

        MenuItem run = new MenuItem("Run", loadIcon(Images.play));
        run.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        run.disableProperty().bind(runTab.canRun.not());
        run.textProperty().bind(runTab.runCaption);
        run.setOnAction(runTab::clickRun);
        MenuItem stop = new MenuItem("Stop", loadIcon(Images.stop));
        stop.disableProperty().bind(runTab.canStop.not());
        stop.setOnAction(runTab::clickStop);
        Menu runMenu = new Menu("Run", null, run, stop);

        MenuItem about = new MenuItem("About");
        about.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AboutSnowride aboutSnowride = new AboutSnowride();
                aboutSnowride.show();
            }
        });
        MenuItem shortcuts = new MenuItem("Keyboard shortcuts");
        shortcuts.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AboutKeyboardShortcuts aboutSnowride = new AboutKeyboardShortcuts();
                aboutSnowride.show();
            }
        });
        MenuItem robotFrameworkUserGuide = new MenuItem("Navigate to Robot Framework User Guide", loadIcon(Images.internet));
        robotFrameworkUserGuide.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                navigateToWebsite("http://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html");
            }
        });
        MenuItem robotFrameworkLibrariesDocumentation = new MenuItem("Navigate to Robot Framework libraries documentation", loadIcon(Images.internet));
        robotFrameworkLibrariesDocumentation.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                navigateToWebsite("http://robotframework.org/robotframework/#user-guide");
            }
        });
        MenuItem releaseNotes = new MenuItem("View Snowride changelog/release notes");
        releaseNotes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AboutChangelog aboutChangelog = new AboutChangelog();
                aboutChangelog.show();
            }
        });
        MenuItem bSettings2 = new MenuItem("Settings", loadIcon(Images.keywordIcon)); // we have to create another menu item, because a menu item can have only one parent
        // And we need this in Tools, because that's what people are used to.
        bSettings2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SettingsWindow settingsWindow = new SettingsWindow(MainForm.this);
                settingsWindow.show();
            }
        });
        Menu toolsMenu = new Menu("Tools", null, bSettings2);
        Menu helpMenu = new Menu("Help", null, about, shortcuts, robotFrameworkUserGuide, robotFrameworkLibrariesDocumentation, releaseNotes);
        mainMenu.getMenus().addAll(projectMenu, navigateMenu, runMenu, toolsMenu, helpMenu);
        return mainMenu;
    }

    private void navigateToWebsite(String uri) {
        try {
            Desktop.getDesktop().browse(URI.create(uri));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openDirectory(ActionEvent actionEvent) {
        File openWhat = openFolderDialog.showDialog(this.stage);
        if (openWhat != null) {
            loadProjectFromFolder(openWhat);
        }
    }

    private void goBack(ActionEvent event) {
        if (navigationStack.canNavigateBack.getValue()) {
            selectProgrammatically(navigationStack.navigateBackwards());
        }
    }

    private void goForwards(ActionEvent event) {
        if (navigationStack.canNavigateForwards.getValue()) {
            selectProgrammatically(navigationStack.navigateForwards());
        }
    }

    public void selectProgrammatically(HighElement navigateTo) {
        if (navigateTo == null) {
            return;
        }
        TreeItem<HighElement> selectWhat = navigateTo.treeNode;
        humanInControl = false;
        expandUpTo(selectWhat);
        Platform.runLater(() -> {
            int index = projectTree.getRow(selectWhat);
            projectTree.getFocusModel().focus(index);
            projectTree.getSelectionModel().select(index);
            projectTree.scrollTo(index);
            humanInControl = true;
        });
    }

    private void expandUpTo(TreeItem<HighElement> expandUpTo) {
        if (expandUpTo.getParent() != null) {
            expandUpTo(expandUpTo.getParent());
        }
        expandUpTo.setExpanded(true);
    }

    public void loadProjectFromFolder(File path) {

            boolean abort = maybeOfferSaveDiscardOrCancel();
            if (abort){
                return;
            }
        projectLoad.progress.set(0);
        navigationStack.clear();
        reloadElementIntoTabs(null);
        projectLoader.submit(() -> {
            try {
                FilesystemWatcher.getInstance().forgetEverything();
                File canonicalPath = path.getAbsoluteFile().getCanonicalFile();
                FolderSuite folderSuite = gateParser.loadDirectory(canonicalPath, projectLoad, 0.8);

                List<HighElement> twoChildren = new ArrayList<>();
                List<File> additionalFiles = Settings.getInstance().getAdditionalFoldersAsFiles();
                ExternalResourcesElement externalResources = gateParser.createExternalResourcesElement(additionalFiles, projectLoad, 0.2);
                UltimateRoot ultimateRoot = new UltimateRoot(folderSuite, externalResources);
                ultimateRoot.selfAndDescendantHighElements().forEachOrdered(he -> {
                    if (he instanceof Suite) {
                        ((Suite) he).analyzeSemantics();
                    }
                });
                Platform.runLater(() -> {
                    projectLoad.progress.set(1);
                    navigationStack.clear();
                    reloadElementIntoTabs(null);
                    humanInControl = false;
                    projectTree.setRoot(ultimateRoot.treeNode);
                    tbSearchTests.requestFocus();
                    projectTree.getSelectionModel().select(0);
                    projectTree.getFocusModel().focus(0);
                    runTab.maybeRunNumberChanged();
                    humanInControl = true;

                    Settings.getInstance().lastOpenedProject = canonicalPath.toString();
                    Settings.getInstance().addToRecentlyOpen(canonicalPath.toString());
                    refreshRecentlyOpenMenu();
                    Settings.getInstance().save();
                    reloadExternalLibraries();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

    }

    /**
     * Opens a Save/Don't save/Cancel dialog.
     * @return Returns true if the action that prompted this dialog should be aborted.
     */
    private boolean maybeOfferSaveDiscardOrCancel() {
        if (canSave.getValue()) {
            // dialog
            ButtonType save = new ButtonType("Save changes");
            ButtonType dontSave = new ButtonType("Don't save");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You have unsaved changes.",
                    save,
                    dontSave,
                    cancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                ButtonType pressedButton = result.get();
                if (pressedButton == save) {
                    saveAll(null);
                    return false;
                } else if (pressedButton == dontSave) {
                    // proceed
                    return false;
                } else {
                    // abort
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void refreshRecentlyOpenMenu() {
        int startAt = projectMenu.getItems().indexOf(separatorBeforeRecentProjects) + 1;
        int endAt = projectMenu.getItems().indexOf(separatorAfterRecentProjects);
        projectMenu.getItems().remove(startAt, endAt);
        List<MenuItem> newItems = new ArrayList<>();
        for (String whatShouldBeThere : Settings.getInstance().lastOpenedProjects) {
            MenuItem newItem = new MenuItem(whatShouldBeThere);
            newItem.setOnAction(event -> loadProjectFromFolder(new File(whatShouldBeThere)));
            newItems.add(newItem);
        }
        projectMenu.getItems().addAll(startAt, newItems);
    }

    public void show() {
        stage.show();
    }

    public void selectProgrammaticallyAndRememberInHistory(HighElement enterWhere) {
        navigationStack.standardEnter(enterWhere);
        selectProgrammatically(enterWhere);
    }

    public Window getStage() {
        return this.stage;
    }

    public TabPane getTabs() {
        return this.tabs;
    }

    public Scenario findTestByFullyQualifiedName(String longname) {
        return (Scenario) getProjectTree().getRoot().getValue().selfAndDescendantHighElements().filter(he -> {
            return he.getQualifiedName().toLowerCase().replace('_', ' ').equals(longname.replace('_', ' ').toLowerCase());
        }).findFirst().get();
    }

    public void toast(String toastMessage) {
        notificationShowingWhat = toastMessage;
        notificationPane.show(toastMessage);
        endTheToastExecutor.schedule(() -> endTheToast(toastMessage), 5, TimeUnit.SECONDS);
    }

    private void endTheToast(String toastMessage) {
        Platform.runLater(() -> {
            //noinspection StringEquality -- reference comparison on purpose
            if (this.notificationShowingWhat == toastMessage) {
                notificationPane.hide();
            }
        });
    }


    public void reloadExternalLibraries() {
        ReloadExternalLibraries.reload(() -> {
            humanInControl = false;
            // Reload current thing
            reloadElementIntoTabs(getFocusedElement(), false);
            humanInControl = true;
        });
    }

    public FolderSuite getRootDirectoryElement() {
        return getRootElement().getRootDirectory();
    }

    public ExternalResourcesElement getExternalResourcesElement() {
        return getRootElement().getExternalResourcesElement();
    }
}

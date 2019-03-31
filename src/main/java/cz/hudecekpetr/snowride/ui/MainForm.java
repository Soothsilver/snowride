package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.filesystem.Filesystem;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.runner.RunTab;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.*;
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
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.stage.Window;
import org.controlsfx.control.PopOver;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainForm {
    public static MainForm INSTANCE = null;
    public static DocumentationPopOver popOver;
    public static final Font TEXT_EDIT_FONT = new Font("Consolas", 12);
    private static final Font TREE_VIEW_FONT = new Font("System Regular", 8);
    private final SerializingTab serializingTab;
    GateParser gateParser = new GateParser();
    private NavigationStack navigationStack = new NavigationStack();
    private Stage stage;
    private TreeView<HighElement> projectTree;
    private SearchSuites searchSuitesAutoCompletion;
    boolean switchingTextEditContents = false;
    private final TabPane tabs;
    private final Tab tabTextEdit;
    private Filesystem filesystem;
    private SeparatorMenuItem separatorBeforeRecentProjects;
    private SeparatorMenuItem separatorAfterRecentProjects;
    private DirectoryChooser openFolderDialog;
    private Menu projectMenu;
    private final TextEditTab textEditTab;

    private void selectedTabChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        if (oldValue == tabTextEdit) {
            getFocusedElement().applyAndValidateText();
        }
        if (newValue == gridTab.getTabGrid()) {
            gridTab.loadElement(getFocusedElement());
        }
    }

    public HighElement getFocusedElement() {
        return getProjectTree().getFocusModel().getFocusedItem().getValue();
    }

    public TreeView<HighElement> getProjectTree() {
        return projectTree;
    }

    boolean humanInControl = true;
    BooleanProperty canSave = new SimpleBooleanProperty(false);
    BooleanProperty canNavigateBack = new SimpleBooleanProperty(false);
    BooleanProperty canNavigateForwards = new SimpleBooleanProperty(false);
    private ContextMenu treeContextMenu;
    private RunTab runTab;
    private GridTab gridTab;

    public MainForm(Stage stage) {
        MainForm.INSTANCE = this;
        popOver = new DocumentationPopOver();
        this.stage = stage;
        filesystem = new Filesystem(this);
        canNavigateBack.bindBidirectional(navigationStack.canNavigateBack);
        canNavigateForwards.bindBidirectional(navigationStack.canNavigateForwards);
        stage.setTitle("Snowride");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
        runTab = new RunTab(this);
        Tab tabRun = runTab.createTab();
        MenuBar mainMenu = buildMainMenu();
        ToolBar toolBar = buildToolBar();

        textEditTab = new TextEditTab(this);
        tabTextEdit = textEditTab.createTab();
        gridTab = new GridTab(this);
        Tab tabGrid = gridTab.createTab();
        serializingTab = new SerializingTab(this);
        Tab tabSerializing = serializingTab.createTab();
        tabs = new TabPane(tabTextEdit, tabGrid, tabRun, tabSerializing);
        tabs.getSelectionModel().selectedItemProperty().addListener(serializingTab::selTabChanged);
        tabs.getSelectionModel().selectedItemProperty().addListener(this::selectedTabChanged);
                VBox searchableTree = createLeftPane();
        SplitPane treeAndGrid = new SplitPane(searchableTree, tabs);
        treeAndGrid.setOrientation(Orientation.HORIZONTAL);
        SplitPane.setResizableWithParent(searchableTree, false);
        treeAndGrid.setDividerPosition(0,0.3);
        VBox vBox = new VBox(mainMenu, toolBar, treeAndGrid);
        VBox.setVgrow(treeAndGrid, Priority.ALWAYS);
        Scene scene = new Scene(vBox, Settings.getInstance().width, Settings.getInstance().height);
        scene.getStylesheets().add(getClass().getResource("/snow.css").toExternalForm());
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
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/Snowflake2.png")));
    }

    public void changeOccurredTo(HighElement whatChanged, LastChangeKind how) {
        whatChanged.unsavedChanges = how;
        whatChanged.treeNode.setValue(null);
        whatChanged.treeNode.setValue(whatChanged);
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
        TextField tbSearchTests = new TextField();
        tbSearchTests.setPromptText("Search for tests or suites...");
        searchSuitesAutoCompletion = new SearchSuites(this);
        searchSuitesAutoCompletion.bind(tbSearchTests);
        projectTree = new TreeView<HighElement>();
        projectTree.setFixedCellSize(16);
        projectTree.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.SPACE) {
                    TreeItem<HighElement> focusedItem = projectTree.getFocusModel().getFocusedItem();
                    if (focusedItem != null) {
                        HighElement element = focusedItem.getValue();
                        element.checkbox.setSelected(!element.checkbox.isSelected());
                    }
                }
            }
        });
        projectTree.getFocusModel().focusedItemProperty().addListener(new ChangeListener<TreeItem<HighElement>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<HighElement>> observable, TreeItem<HighElement> oldValue, TreeItem<HighElement> newValue) {
                onfocusTreeNode(oldValue);
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

    private void onfocusTreeNode(TreeItem<HighElement> oldValue) {
        if (oldValue != null) {
            oldValue.getValue().applyAndValidateText();
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
                            filesystem.createNewRobotFile((FolderSuite)element, name);
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
                        ((FileSuite)element).createNewChild(name, true);
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
                        Desktop.getDesktop().edit(((FileSuite)element).file);
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
                        ((ISuite)element).createNewChild(name, false);
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
            MenuItem deselect_all_tests = new MenuItem("Deselect all tests");
            deselect_all_tests.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    setCheckboxes(element, false);
                }
            });
            menu.add(deselect_all_tests);
        }
        maybeAddSeparator(menu);
        MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String newName = TextFieldForm.askForText("Rename " + element, "New name:", "Rename", element.shortName);
                if (newName != null) {
                    element.renameSelfTo(newName);
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
        return menu;
    }

    private void delete(HighElement element) {
        element.deleteSelf();
    }

    private void setCheckboxes(HighElement element, boolean shouldBeChecked) {
        element.checkbox.setSelected(shouldBeChecked);
        for (HighElement child : element.children) {
            setCheckboxes(child, shouldBeChecked);
        }
    }

    private void maybeAddSeparator(List<MenuItem> menu) {
        if (menu.size() > 0 && !(menu.get(menu.size() - 1) instanceof SeparatorMenuItem)) {
            menu.add(new SeparatorMenuItem());
        }
    }

    private ToolBar buildToolBar() {
        Button bNavigateBack = new Button("Navigate back", loadIcon("GoLeft.png"));
        Button bNavigateForwards = new Button("Navigate forwards", loadIcon("GoRight.png"));
        Button bSaveAll = new Button("Save all");
        Button bRun = new Button("Run");
        Button bStop = new Button("Stop");
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

    // Hello
    private ImageView loadIcon(String path) {
        Image image = new Image(getClass().getResourceAsStream("/icons/" + path), 16, 16, false, false);
        ImageView imageView = new ImageView(image);
        return imageView;
    }

    private void focusTreeNode(TreeItem<HighElement> focusedNode) {
        if (focusedNode != null) {
            if (humanInControl) {
                navigationStack.standardEnter(focusedNode.getValue());
            }

            switchingTextEditContents = true;
            textEditTab.loadElement(focusedNode.getValue());
            gridTab.loadElement(focusedNode.getValue());
            serializingTab.loadElement(focusedNode.getValue());
            switchingTextEditContents = false;
            if (focusedNode.getValue() instanceof Scenario) {
                tabs.getSelectionModel().select(gridTab.getTabGrid());
            }
        }
    }

    private void saveAll(ActionEvent event) {
        try {
            projectTree.getRoot().getValue().saveAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        canSave.setValue(false);
    }

    private MenuBar buildMainMenu() {
        MenuBar mainMenu = new MenuBar();
        projectMenu = new Menu("Project");

        MenuItem bLoadArbitrary = new MenuItem("Load directory...");
        bLoadArbitrary.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        bLoadArbitrary.setOnAction(this::openDirectory);
        openFolderDialog = new DirectoryChooser();

        MenuItem bSaveAll = new MenuItem("Save all");
        bSaveAll.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        bSaveAll.setOnAction(this::saveAll);
        bSaveAll.disableProperty().bind(canSave.not());

        MenuItem bExit = new MenuItem("Exit Snowride");
        bExit.setOnAction(event -> System.exit(0));
        separatorBeforeRecentProjects = new SeparatorMenuItem();
        separatorAfterRecentProjects = new SeparatorMenuItem();
        projectMenu.getItems().addAll(bLoadArbitrary, bSaveAll, separatorBeforeRecentProjects, separatorAfterRecentProjects, bExit);
        refreshRecentlyOpenMenu();

        MenuItem back = new MenuItem("Navigate back", loadIcon("GoLeft.png"));
        back.disableProperty().bind(canNavigateBack.not());
        back.setAccelerator(new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN));
        back.setOnAction(this::goBack);
        MenuItem forwards = new MenuItem("Navigate forwards", loadIcon("GoRight.png"));
        forwards.setOnAction(this::goForwards);
        forwards.setAccelerator(new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN));
        forwards.disableProperty().bind(canNavigateForwards.not());
        Menu navigateMenu = new Menu("Navigate", null, back, forwards);
        mainMenu.getMenus().addAll(projectMenu, navigateMenu);
        return mainMenu;
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
        expandUpTo(selectWhat);
        int index = projectTree.getRow(selectWhat);
        humanInControl = false;
        projectTree.getFocusModel().focus(index);
        projectTree.getSelectionModel().select(index);
        humanInControl = true;
    }

    private void expandUpTo(TreeItem<HighElement> expandUpTo) {
        if (expandUpTo.getParent() != null) {
            expandUpTo(expandUpTo.getParent());
        }
        expandUpTo.setExpanded(true);
    }

    private LongRunningOperation projectLoad = new LongRunningOperation();
    private ExecutorService projectLoader = Executors.newSingleThreadExecutor();

    public void loadProjectFromFolder(File path) {
        projectLoad.progress.set(0);
        projectLoader.submit(()->{
            try {
                File canonicalPath = path.getAbsoluteFile().getCanonicalFile();
                FolderSuite folderSuite = gateParser.loadDirectory(canonicalPath, projectLoad, 1);
                Platform.runLater(()-> {
                    projectLoad.progress.set(1);
                    projectTree.setRoot(folderSuite.treeNode);
                    projectTree.requestFocus();
                    projectTree.getSelectionModel().select(0);
                    projectTree.getFocusModel().focus(0);

                    Settings.getInstance().lastOpenedProject = canonicalPath.toString();
                    Settings.getInstance().addToRecentlyOpen(canonicalPath.toString());
                    refreshRecentlyOpenMenu();
                    Settings.getInstance().save();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

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
}

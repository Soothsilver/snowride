package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.fx.AutoCompletionTextFieldBinding;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.runner.RunTab;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.TestCase;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainForm {
    public static final Font TEXT_EDIT_FONT = new Font("Consolas", 12);
    private final TextArea tbTextEdit;
    GateParser gateParser = new GateParser();
    private NavigationStack navigationStack = new NavigationStack();
    private Stage stage;
    private TreeView<HighElement> projectTree;
    private SearchSuites searchSuitesAutoCompletion;
    boolean switchingTextEditContents = false;
    private final TabPane tabs;
    private final Tab tabTextEdit;

    public TreeView<HighElement> getProjectTree() {
        return projectTree;
    }

    boolean humanInControl = true;
    BooleanProperty canSave = new SimpleBooleanProperty(false);
    BooleanProperty canNavigateBack = new SimpleBooleanProperty(false);
    BooleanProperty canNavigateForwards = new SimpleBooleanProperty(false);
    public BooleanProperty canRun = new SimpleBooleanProperty(true);
    public BooleanProperty canStop = new SimpleBooleanProperty(false);
    private ContextMenu treeContextMenu;
    private RunTab runTab;
    private GridTab gridTab;

    public MainForm(Stage stage) {
        this.stage = stage;
        canNavigateBack.bindBidirectional(navigationStack.canNavigateBack);
        canNavigateForwards.bindBidirectional(navigationStack.canNavigateForwards);
        stage.setTitle("Snowride");
        MenuBar mainMenu = buildMainMenu();
        ToolBar toolBar = buildToolBar();
        tbTextEdit = new TextArea();
        tbTextEdit.setPromptText("This will display text...");
        tbTextEdit.setFont(TEXT_EDIT_FONT);
        tbTextEdit.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!switchingTextEditContents) {
                    HighElement whatChanged = projectTree.getFocusModel().getFocusedItem().getValue();
                    System.out.println(whatChanged.name + " changed.");
                    whatChanged.changedByUser = true;
                    whatChanged.contents = newValue;
                    whatChanged.treeNode.setValue(null);
                    whatChanged.treeNode.setValue(whatChanged);
                    canSave.set(true);
                }
            }
        });
        tabTextEdit = new Tab("Edit entire file as text", tbTextEdit);
        tabTextEdit.setClosable(false);
        gridTab = new GridTab(this);
        Tab tabGrid = gridTab.createTab();
        runTab = new RunTab(this);
        Tab tabRun = runTab.createTab();
        tabs = new TabPane(tabTextEdit, tabGrid, tabRun);
        VBox searchableTree = createLeftPane();
        SplitPane treeAndGrid = new SplitPane(searchableTree, tabs);
        treeAndGrid.setOrientation(Orientation.HORIZONTAL);
        treeAndGrid.setDividerPosition(0,0.3);
        VBox vBox = new VBox(mainMenu, toolBar, treeAndGrid);
        VBox.setVgrow(treeAndGrid, Priority.ALWAYS);
        stage.setScene(new Scene(vBox, 800, 700));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/Snowflake2.png")));
    }

    private VBox createLeftPane() {
        TextField tbSearchTests = new TextField();
        tbSearchTests.setPromptText("Search for tests or suites...");
        searchSuitesAutoCompletion = new SearchSuites(this);
        searchSuitesAutoCompletion.bind(tbSearchTests);
        projectTree = new TreeView<HighElement>();
        projectTree.getFocusModel().focusedItemProperty().addListener(new ChangeListener<TreeItem<HighElement>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<HighElement>> observable, TreeItem<HighElement> oldValue, TreeItem<HighElement> newValue) {

                focusTreeNode(newValue);
            }
        });
        treeContextMenu = new ContextMenu();
        treeContextMenu.getItems().add(new MenuItem("A"));
        treeContextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                treeContextMenu.getItems().clear();
                TreeItem<HighElement> focused = projectTree.getFocusModel().getFocusedItem();
                if (focused != null) {
                    treeContextMenu.getItems().addAll(createContextMenuFor(focused));
                }
            }
        });
        projectTree.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                treeContextMenu.show(projectTree, event.getScreenX(), event.getScreenY());
            }
        });
        projectTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    treeContextMenu.hide();
                }
            }
        });
        VBox vBox = new VBox(tbSearchTests, projectTree);
        VBox.setVgrow(projectTree, Priority.ALWAYS);
        return vBox;
    }

    private List<MenuItem> createContextMenuFor(TreeItem<HighElement> forWhat) {
        List<MenuItem> menu = new ArrayList<>();
        HighElement element = forWhat.getValue();
        if (element instanceof FolderSuite) {
            menu.add(new MenuItem("New folder suite"));
            menu.add(new MenuItem("New file suite/resource file"));
        }
        maybeAddSeparator(menu);
        if (element instanceof FileSuite) {
            menu.add(new MenuItem("New test case"));
        }
        if (element instanceof FolderSuite || element instanceof FileSuite) {
            menu.add(new MenuItem("New user keyword"));
        }
        maybeAddSeparator(menu);
        if (element instanceof FolderSuite || element instanceof FileSuite) {
            menu.add(new MenuItem("Select all tests"));
            menu.add(new MenuItem("Deselect all tests"));
        }
        maybeAddSeparator(menu);
        menu.add(new MenuItem("Rename"));
        menu.add(new MenuItem("Delete"));
        return menu;
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
        bRun.disableProperty().bind(canRun.not());
        bStop.disableProperty().bind(canStop.not());
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
            tbTextEdit.setText(focusedNode.getValue().contents);
            gridTab.loadElement(focusedNode.getValue());
            switchingTextEditContents = false;
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
        final Menu projectMenu = new Menu("Project");
        MenuItem bLoadCurrentDir = new MenuItem("Load project from current directory");
        bLoadCurrentDir.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                loadProjectFromCurrentDir();
            }
        });
        MenuItem bSaveAll = new MenuItem("Save all");
        bSaveAll.setOnAction(this::saveAll);
        bSaveAll.disableProperty().bind(canSave.not());
        MenuItem bReparseChanged = new MenuItem("Reparse changed files");
        bReparseChanged.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               // projectTree.getRoot().getValue().reparseWhatChanged();
            }
        });
        MenuItem bExit = new MenuItem("Exit Snowride");
        bExit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Platform.exit();
            }
        });
        projectMenu.getItems().addAll(bLoadCurrentDir, bSaveAll,bReparseChanged, bExit);
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

    public void loadProjectFromCurrentDir() {
        FolderSuite folderSuite = null;
        try {
            folderSuite = gateParser.loadDirectory(new File(".").getAbsoluteFile().getCanonicalFile());

            projectTree.setRoot(folderSuite.treeNode);
            projectTree.requestFocus();
            projectTree.getSelectionModel().select(0);
            projectTree.getFocusModel().focus(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void show() {
        stage.show();
    }
}

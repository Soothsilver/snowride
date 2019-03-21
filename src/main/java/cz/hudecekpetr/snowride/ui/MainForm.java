package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.runner.RunTab;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
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
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainForm {
    private static final Font TEXT_EDIT_FONT = new Font("Consolas", 12);
    private final TextArea tbTextEdit;
    GateParser gateParser = new GateParser();
    private Stage stage;
    private TreeView<HighElement> projectTree;
    boolean switchingTextEditContents = false;
    BooleanProperty canSave = new SimpleBooleanProperty(false);
    BooleanProperty canNavigateBack = new SimpleBooleanProperty(false);
    BooleanProperty canNavigateForwards = new SimpleBooleanProperty(false);
    public BooleanProperty canRun = new SimpleBooleanProperty(true);
    public BooleanProperty canStop = new SimpleBooleanProperty(false);
    private ContextMenu treeContextMenu;
    RunTab runTab;

    public MainForm(Stage stage) {
        this.stage = stage;
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
                }
            }
        });
        Tab tabTextEdit = new Tab("Edit entire file as text", tbTextEdit);
        tabTextEdit.setClosable(false);
        Tab tabGrid = new Tab("Assisted grid editing");
        tabGrid.setClosable(false);
        runTab = new RunTab(this);
        Tab tabRun = runTab.createTab();
        TabPane tabs = new TabPane(tabTextEdit, tabGrid, tabRun);
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
        bNavigateForwards.disableProperty().bind(canNavigateForwards.not());
        bSaveAll.disableProperty().bind(canSave.not());
        bRun.disableProperty().bind(canRun.not());
        bStop.disableProperty().bind(canStop.not());
        ToolBar toolBar = new ToolBar(bNavigateBack, bNavigateForwards, bSaveAll, bRun, bStop);
        return toolBar;
    }

    private ImageView loadIcon(String path) {
        Image image = new Image(getClass().getResourceAsStream("/icons/" + path), 16, 16, false, false);
        ImageView imageView = new ImageView(image);
        return imageView;
    }

    private void focusTreeNode(TreeItem<HighElement> focusedNode) {
        if (focusedNode != null) {
            switchingTextEditContents = true;
            tbTextEdit.setText(focusedNode.getValue().contents);
            switchingTextEditContents = false;
        }
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
        bSaveAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    projectTree.getRoot().getValue().saveAll();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
        MenuItem forwards = new MenuItem("Navigate forwards", loadIcon("GoRight.png"));
        forwards.disableProperty().bind(canNavigateForwards.not());
        Menu navigateMenu = new Menu("Navigate", null, back, forwards);
        mainMenu.getMenus().addAll(projectMenu, navigateMenu);
        return mainMenu;
    }

    public void loadProjectFromCurrentDir() {
        FolderSuite folderSuite = null;
        try {
            folderSuite = gateParser.loadDirectory(new File(".").getAbsoluteFile().getCanonicalFile());

            projectTree.setRoot(folderSuite.treeNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void show() {
        stage.show();
    }
}

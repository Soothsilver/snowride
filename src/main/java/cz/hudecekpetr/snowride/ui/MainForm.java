package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.parser.GateParser;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


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
    BooleanProperty canRun = new SimpleBooleanProperty(true);
    BooleanProperty canStop = new SimpleBooleanProperty(false);

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
        Tab tabRun = new Tab("Run");
        tabRun.setClosable(false);
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
        VBox vBox = new VBox(tbSearchTests, projectTree);
        VBox.setVgrow(projectTree, Priority.ALWAYS);
        return vBox;
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

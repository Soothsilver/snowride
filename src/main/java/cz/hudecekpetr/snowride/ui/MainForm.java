package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


public class MainForm {
    private static final Font TEXT_EDIT_FONT = new Font("Consolas", 8);
    private final TextArea tbTextEdit;
    GateParser gateParser = new GateParser();
    private Stage stage;
    private final TreeView<HighElement> projectTree;
    boolean switchingTextEditContents = false;

    public MainForm(Stage stage) {
        this.stage = stage;
        stage.setTitle("Snowride");
        MenuBar mainMenu = buildMainMenu();
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
        TabPane tabs = new TabPane(tabTextEdit, tabGrid);
        projectTree = new TreeView<HighElement>();
        projectTree.getFocusModel().focusedItemProperty().addListener(new ChangeListener<TreeItem<HighElement>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<HighElement>> observable, TreeItem<HighElement> oldValue, TreeItem<HighElement> newValue) {
                focusTreeNode(newValue);
            }
        });
        SplitPane treeAndGrid = new SplitPane(projectTree, tabs);
        treeAndGrid.setOrientation(Orientation.HORIZONTAL);
        VBox vBox = new VBox(mainMenu, treeAndGrid);
        VBox.setVgrow(treeAndGrid, Priority.ALWAYS);
        stage.setScene(new Scene(vBox, 800, 700));
    }

    private void focusTreeNode(TreeItem<HighElement> focusedNode) {
       switchingTextEditContents = true;
       tbTextEdit.setText(focusedNode.getValue().contents);
       switchingTextEditContents = false;
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
        MenuItem bExit = new MenuItem("Exit Snowride");
        bExit.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                Platform.exit();
            }
        });
        projectMenu.getItems().addAll(bLoadCurrentDir, bSaveAll, bExit);
        mainMenu.getMenus().add(projectMenu);
        return mainMenu;
    }

    public void loadProjectFromCurrentDir() {
        FolderSuite folderSuite = null;
        try {
            folderSuite = gateParser.loadDirectory(new File(".").getAbsoluteFile());

            projectTree.setRoot(folderSuite.treeNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void show() {
        stage.show();
    }
}

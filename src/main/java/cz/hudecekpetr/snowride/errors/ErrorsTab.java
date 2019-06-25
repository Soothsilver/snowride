package cz.hudecekpetr.snowride.errors;

import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.findusages.Usage;
import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.tree.highelements.UltimateRoot;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ErrorsTab {
    private final TableView<SnowrideError> tableErrors;
    private final MainForm mainForm;
    public Tab tab;

    public ErrorsTab(MainForm mainForm) {
        this.mainForm = mainForm;
        tableErrors = new TableView<>();
        tableErrors.getSelectionModel().setCellSelectionEnabled(false);
        tableErrors.setPlaceholder(new Label("Snowride detects no parse errors in this project."));

        // Column "Location"
        TableColumn<SnowrideError, HighElement> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(param -> param.getValue().where);
        locationColumn.setPrefWidth(150);
        locationColumn.setCellFactory(new Callback<TableColumn<SnowrideError, HighElement>, TableCell<SnowrideError, HighElement>>() {
            @Override
            public TableCell<SnowrideError, HighElement> call(TableColumn<SnowrideError, HighElement> param) {
                return new TableCell<SnowrideError, HighElement>() {
                    @Override
                    protected void updateItem(HighElement item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.getShortName());
                            setGraphic(null);
                        }
                    }
                };
            }
        });
        tableErrors.getColumns().add(locationColumn);

        // Column "Severity"
        TableColumn<SnowrideError, Severity> severityColumn = new TableColumn<>("Severity");
        severityColumn.setCellValueFactory(param -> param.getValue().severity);
        severityColumn.setCellFactory(new Callback<TableColumn<SnowrideError, Severity>, TableCell<SnowrideError, Severity>>() {
            @Override
            public TableCell<SnowrideError, Severity> call(TableColumn<SnowrideError, Severity> param) {
                return new TableCell<SnowrideError, Severity>() {
                    @Override
                    protected void updateItem(Severity item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(null);
                            if (item == Severity.ERROR) {
                                setGraphic(new ImageView(Images.error));
                                setText("Error");
                            } else {
                                setGraphic(new ImageView(Images.warning));
                                setText("Warning");
                            }
                        }
                    }
                };
            }
        });
        severityColumn.setPrefWidth(100);
        tableErrors.getColumns().add(severityColumn);

        // Column "Type"
        TableColumn<SnowrideError, ErrorKind> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(param -> param.getValue().type);
        typeColumn.setPrefWidth(100);
        tableErrors.getColumns().add(typeColumn);

        // Column "Description"
        TableColumn<SnowrideError, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(param -> param.getValue().description);
        descriptionColumn.setPrefWidth(500);

        // Double-click to go to that file.
        tableErrors.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2) {
                SnowrideError selectedError = tableErrors.getFocusModel().getFocusedItem();
                if (selectedError != null) {
                    mainForm.selectProgrammaticallyAndRememberInHistory(selectedError.where.getValue());
                    // If the file is already selected, we wouldn't switch to the grid tab automatically, so let's switch to
                    // the grid tab explicitly here.
                    mainForm.getTabs().getSelectionModel().select(mainForm.gridTab.getTabGrid());
                }
            }
        });
        tableErrors.getColumns().add(descriptionColumn);

        // "Find usages" forces a semantic analysis on all lines of all tests and keywords which will trigger all imports.
        // Otherwise, imports are only processed when you select a file/folder in the treeview.
        Button bAnalyzeCode = new Button("Analyze code");
        bAnalyzeCode.setOnAction(event -> analyzeCode());
        HBox hErrors = new HBox(5, bAnalyzeCode, new Label("Double-click an error to switch to that file."));
        hErrors.setPadding(new Insets(2));
        hErrors.setAlignment(Pos.CENTER_LEFT);
        VBox vbErrors = new VBox(2, hErrors, tableErrors);
        VBox.setVgrow(tableErrors, Priority.ALWAYS);
        tab = new Tab("Errors", vbErrors);
        tab.setClosable(false);
        TreeItem<HighElement> root = mainForm.getProjectTree().getRoot();
        if (root != null) {
            tableErrors.setItems(root.getValue().allErrorsRecursive);
        }
        mainForm.getProjectTree().rootProperty().addListener((observable, oldValue, newValue) -> tableErrors.setItems(newValue.getValue().allErrorsRecursive));
    }

    private void analyzeCode() {
        List<HighElement> allTestsAndKeywords = mainForm.getRootElement().selfAndDescendantHighElements().collect(Collectors.toList());
        for (HighElement he : allTestsAndKeywords) {
            he.analyzeCodeInSelf();
        }
    }
}

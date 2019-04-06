package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.ErrorKind;
import cz.hudecekpetr.snowride.SnowrideError;
import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.controlsfx.validation.Severity;


public class ErrorsTab {
    private final TableView<SnowrideError> tableErrors;

    public ErrorsTab(MainForm mainForm) {
        tableErrors = new TableView<>();
        TableColumn<SnowrideError, HighElement> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(param -> param.getValue().where);
        tableErrors.getColumns().add(locationColumn);
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
                            } else {
                                setGraphic(new ImageView(Images.warning));
                            }
                        }
                    }
                };
            }
        });
        severityColumn.setPrefWidth(50);
        tableErrors.getColumns().add(severityColumn);
        TableColumn<SnowrideError, ErrorKind> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(param -> param.getValue().type);
        severityColumn.setPrefWidth(150);
        tableErrors.getColumns().add(typeColumn);
        TableColumn<SnowrideError, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(param -> param.getValue().description);
        descriptionColumn.setPrefWidth(500);
        tableErrors.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2) {
                    SnowrideError selectedError = tableErrors.getFocusModel().getFocusedItem();
                    if (selectedError != null) {
                        mainForm.selectProgrammaticallyAndRememberInHistory(selectedError.where.getValue());
                    }
                }
            }
        });
        tableErrors.getColumns().add(descriptionColumn);
        tab = new Tab("Errors", tableErrors);
        tab.setClosable(false);
        TreeItem<HighElement> root = mainForm.getProjectTree().getRoot();
        if (root != null) {
            tableErrors.setItems(root.getValue().allErrorsRecursive);
        }
        mainForm.getProjectTree().rootProperty().addListener(new ChangeListener<TreeItem<HighElement>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<HighElement>> observable, TreeItem<HighElement> oldValue, TreeItem<HighElement> newValue) {
                tableErrors.setItems(newValue.getValue().allErrorsRecursive);
            }
        });
    }
    public Tab tab;
}

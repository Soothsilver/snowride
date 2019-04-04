package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.SnowrideError;
import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;

import javax.print.attribute.standard.Severity;

public class ErrorsTab {
    private final TableView<SnowrideError> tableErrors;

    public ErrorsTab(MainForm mainForm) {
        tableErrors = new TableView<>();
        TableColumn<SnowrideError, HighElement> locationColumn = new TableColumn<>("Location");
        tableErrors.getColumns().add(locationColumn);
        TableColumn<SnowrideError, Severity> severityColumn = new TableColumn<>("Severity");
        severityColumn.setPrefWidth(50);
        tableErrors.getColumns().add(severityColumn);
        TableColumn<SnowrideError, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setPrefWidth(500);
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

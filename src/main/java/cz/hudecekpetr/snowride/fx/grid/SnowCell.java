
package cz.hudecekpetr.snowride.fx.grid;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
/*
public class SnowCell<S, T> extends TextFieldTableCell<S, T> {

    private TextField textField;
    private boolean escapePressed = false;
    private TablePosition<S, ?> tablePos = null;

    public SnowCell(final StringConverter<T> converter) {
        super(converter);
    }

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    public static <S,T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
            final StringConverter<T> converter) {
        return list -> new SnowCell<>(converter);
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();
        if (isEditing()) {
            if (textField == null) {
                textField = getTextField();
            }
            escapePressed = false;
            startEdit(textField);
            final TableView<S> table = getTableView();
            tablePos = table.getEditingCell();
        }
    }

    @Override
    public void commitEdit(T newValue) {
        if (!isEditing()) {
            return;
        }
        final TableView<S> table = getTableView();
        if (table != null) {
            // Inform the TableView of the edit being ready to be committed.
            TableColumn.CellEditEvent editEvent = new TableColumn.CellEditEvent(table, tablePos, TableColumn.editCommitEvent(), newValue);
            Event.fireEvent(getTableColumn(), editEvent);
        }
        // we need to setEditing(false):
        super.cancelEdit(); // this fires an invalid EditCancelEvent.
        textField.selectEnd();
        // update the item within this cell, so that it represents the new value
        updateItem(newValue, false);
        textField.selectRange(newValue.toString().length(), newValue.toString().length());
        if (table != null) {
            // reset the editing cell on the TableView
            table.edit(-1, null);
        }
    }

    @Override
    public void cancelEdit() {
        if (escapePressed) {
            // this is a cancel event after escape key
            super.cancelEdit();
            setText(getItemText()); // restore the original text in the view
        } else {
            // this is not a cancel event after escape key
            // we interpret it as commit.
            String newText = textField.getText();
            // commit the new text to the model
            this.commitEdit(getConverter().fromString(newText));
        }
        setGraphic(null); // stop editing with TextField
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        updateItem();
    }

    private TextField getTextField() {
        final TextField textField = new TextField(getItemText());
        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("hi");
            }
        });
        // Use onAction here rather than onKeyReleased (with check for Enter),
        textField.setOnAction(event -> {
            if (getConverter() == null) {
                throw new IllegalStateException("StringConverter is null.");
            }
            this.commitEdit(getConverter().fromString(textField.getText()));
            event.consume();
        });
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                                Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    commitEdit(getConverter().fromString(textField.getText()));
                }
            }
        });
        textField.setOnKeyPressed(t -> {
            escapePressed = t.getCode() == KeyCode.ESCAPE;
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                throw new IllegalArgumentException(
                        "did not expect esc key releases here.");
            }
        });
        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                textField.setText(getConverter().toString(getItem()));
                cancelEdit();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT ||
                    event.getCode() == KeyCode.TAB) {
                getTableView().getSelectionModel().selectNext();
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                getTableView().getSelectionModel().selectPrevious();
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                getTableView().getSelectionModel().selectAboveCell();
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                getTableView().getSelectionModel().selectBelowCell();
                event.consume();
            }
        });
        return textField;
    }

    private String getItemText() {
        return getConverter() == null ?
                getItem() == null ? "" : getItem().toString() :
                getConverter().toString(getItem());
    }

    private void updateItem() {
        if (isEmpty()) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItemText());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getItemText());
                setGraphic(null);
            }
        }
    }

    private void startEdit(final TextField textField) {
        if (textField != null) {
            textField.setText(getItemText());
        }
        setText(null);
        setGraphic(textField);
        textField.selectAll();
        // requesting focus so that key input can immediately go into the
        // TextField
        textField.requestFocus();
    }
}*/
/*
 tableView.getItems().add(row1);
        tableView.getItems().add(row2);
        tableView.setEditable(true);
        tableView.getSelectionModel().cellSelectionEnabledProperty().set(true);

        tableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().isLetterKey() || event.getCode().isDigitKey()) {
                    editFocusedCell();

                } else if (event.getCode() == KeyCode.RIGHT ||
                        event.getCode() == KeyCode.TAB) {
                    tableView.getSelectionModel().selectNext();
                    event.consume();
                } else if (event.getCode() == KeyCode.LEFT) {
                    // work around due to
                    // TableView.getSelectionModel().selectPrevious() due to a bug
                    // stopping it from working on
                    // the first column in the last row of the table
                    selectPrevious();
                    event.consume();
                }
            }
        });

        BorderPane root = new BorderPane();
        root.setCenter(tableView);
        root.setLeft(tree);
        primaryStage.setScene(new Scene(root, 1000, 500));
        primaryStage.show();
    }
    private void editFocusedCell() {
        final TablePosition< List<String>, ? > focusedCell = tableView
                .focusModelProperty().get().focusedCellProperty().get();
        tableView.edit(focusedCell.getRow(), focusedCell.getTableColumn());
    }

    private TableColumn < List<String>, ? > getTableColumn(
            final TableColumn <  List<String>, ? > column, int offset) {
        int columnIndex = tableView.getVisibleLeafIndex(column);
        int newColumnIndex = columnIndex + offset;
        return tableView.getVisibleLeafColumn(newColumnIndex);
    }
    private void selectPrevious() {
        if (tableView.getSelectionModel().isCellSelectionEnabled()) {
            // in cell selection mode, we have to wrap around, going from
            // right-to-left, and then wrapping to the end of the previous line
            TablePosition < List<String>, ? > pos = tableView.getFocusModel()
                    .getFocusedCell();
            if (pos.getColumn() - 1 >= 0) {
                // go to previous row
                tableView.getSelectionModel().select(pos.getRow(),
                        getTableColumn(pos.getTableColumn(), -1));
            } else if (pos.getRow() < tableView.getItems().size()) {
                // wrap to end of previous row
                tableView.getSelectionModel().select(pos.getRow() - 1,
                        tableView.getVisibleLeafColumn(
                                tableView.getVisibleLeafColumns().size() - 1));
            }
        } else {
            int focusIndex = tableView.getFocusModel().getFocusedIndex();
            if (focusIndex == -1) {
                tableView.getSelectionModel().select(tableView.getItems().size() - 1);
            } else if (focusIndex > 0) {
                tableView.getSelectionModel().select(focusIndex - 1);
            }
        }
    }
    private void addChildren(TreeItem<String> rootItem, int layer) {
        if (layer == 5) {
            return;
        }
        for (int i =0;i < 10; i++) {
            CheckBoxTreeItem<String> newItem = new CheckBoxTreeItem<String>("Layer " + layer + ", item " + (i + 1));
            rootItem.getChildren().add(newItem);
            addChildren(newItem, layer + 1);
        }
    }

    private class MyContextMenu extends ContextMenu {
        MyContextMenu() {
            super();
            MyContextMenu self = this;
            this.getItems().add(new MenuItem("BASIC ITEM"));
            this.setOnShowing(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    TreeItem<String> sel = tree.getSelectionModel().getSelectedItem();
                    self.getItems().add(new MenuItem("AHOJ " + (sel != null ? sel.getValue() : "null")));
                }
            });
        }
    }
}

 */
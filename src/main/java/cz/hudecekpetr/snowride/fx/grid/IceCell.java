package cz.hudecekpetr.snowride.fx.grid;

import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.TextAlignment;

public class IceCell extends TableCell<LogicalLine, Cell> {
    private TableColumn<LogicalLine, Cell> column;
    private int cellIndex;
    private TextField textField;

    public IceCell(TableColumn<LogicalLine, Cell> column, int cellIndex) {
        this.column = column;
        this.cellIndex = cellIndex;
        this.setPadding(new Insets(0));
        this.setStyle("-fx-padding: 0; -fx-background-insets: 0.0;");
        if (cellIndex < 1) {
            this.setEditable(false);
        }
    }


    @Override
    public void startEdit() {
        super.startEdit();
        if (this.isEditing()) {
            this.setGraphic(ensureTextField());
            textField.setText(this.getText());
            this.setText(null);
            textField.selectAll();
            textField.requestFocus();
        }
    }

    private TextField ensureTextField() {
        if (textField == null) {
            textField = new TextField();
            textField.setStyle("-fx-text-box-border: transparent; -fx-background-insets: 0; -fx-focus-color: transparent; -fx-border-width: 0;");
            textField.setPadding(new Insets(0));
            textField.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    commitEdit(new Cell(textField.getText(), "    "));
                }
            });
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        commitEdit(new Cell(textField.getText(), "    "));
                    }
                }
            });
        }
        return textField;
    }

    @Override
    protected void updateItem(Cell item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            String style = "";
            TextAlignment alignment = TextAlignment.LEFT;
            if (cellIndex == 0) {
                style += "-fx-text-fill: lavender;  ";
                alignment = TextAlignment.CENTER;
            }
            if (cellIndex == 1) {
                if (item.contents.startsWith("[") && item.contents.endsWith("]")) {
                    style += "-fx-text-fill: darkmagenta; ";
                }
                style += "-fx-font-weight: bold; ";
            }
            setStyle(style);
            setTextAlignment(alignment);
            setGraphic(null);
            setText(item.contents);
        }
    }
}

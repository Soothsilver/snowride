package cz.hudecekpetr.snowride.fx.grid;

import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.semantics.codecompletion.CodeCompletionBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class IceCell extends TableCell<LogicalLine, Cell> {
    private TableColumn<LogicalLine, Cell> column;

    public int getCellIndex() {
        return cellIndex;
    }

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
            // Order matters:
            String text = this.getText();
            TextField textField = ensureTextField();
            textField.setText(text);
            this.setText(null);
            this.setGraphic(textField);
            this.textField.selectAll();
            this.textField.requestFocus();
        }
    }

    @Override
    public void commitEdit(Cell newValue) {
        super.commitEdit(newValue);
        if (getScene().getFocusOwner() == textField) {
             column.getTableView().requestFocus();
        }
    }

    public void commit() {
        String oldTrivia = this.getItem().postTrivia;
        if (!oldTrivia.contains("\t") && !oldTrivia.contains("  ")) {
            oldTrivia = "    ";
        }
        // non-virtual:
        commitEdit(new Cell(textField.getText(), oldTrivia, getItem().partOfLine));
    }

    private TextField ensureTextField() {
        if (textField == null) {
            textField = new TextField();
            textField.parentProperty().addListener(new ChangeListener<Parent>() {
                @Override
                public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
                    System.out.println("Parent now: " + newValue);
                }
            });
            textField.setStyle("-fx-text-box-border: transparent; -fx-background-insets: 0; -fx-focus-color: transparent; -fx-border-width: 0;");
            textField.setPadding(new Insets(0));
            textField.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    commit();
                    event.consume();
                }
            });
            CodeCompletionBinding codeCompletionBinding = new CodeCompletionBinding(textField, this);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                        event.consume();
                    }
                }
            });
            textField.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    int oldCaret = textField.getCaretPosition();
                    int oldAnchor = textField.getAnchor();
                    textField.selectRange(0,0);
                    textField.selectRange(oldAnchor, oldCaret);
                }
            });
        }
        return textField;
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        updateStyle();
        setText(this.getItem().contents);
    }

    @Override
    protected void updateItem(Cell item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setStyle(null);
        } else {
            updateStyle();
            setText(item.contents);
            setGraphic(null);
        }
    }

    private void updateStyle() {
        String style = "";
        if (cellIndex <= 0) {
            style += "-fx-font-weight: bold; -fx-background-color: lavender; -fx-text-alignment: right; -fx-alignment: center; ";
        }
        if (cellIndex == 1) {
            if (getItem().contents.startsWith("[") && getItem().contents.endsWith("]")) {
                style += "-fx-text-fill: darkmagenta; ";
            }
            style += "-fx-font-weight: bold; ";
        }
        setStyle(style);
    }
}

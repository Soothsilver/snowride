package cz.hudecekpetr.snowride.fx.grid;

import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.semantics.codecompletion.CodeCompletionBinding;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;
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
import javafx.stage.Window;

public class IceCell extends TableCell<LogicalLine, Cell> {
    private TableColumn<LogicalLine, Cell> column;
    private CodeCompletionBinding codeCompletionBinding;

    public int getCellIndex() {
        return cellIndex;
    }

    private int cellIndex;
    private final SnowTableView snowTableView;
    private TextField textField;

    public IceCell(TableColumn<LogicalLine, Cell> column, int cellIndex, SnowTableView snowTableView) {
        this.column = column;
        this.cellIndex = cellIndex;
        this.snowTableView = snowTableView;
        this.setPadding(new Insets(0));
        this.setStyle("-fx-padding: 0; -fx-background-insets: 0.0;");
        if (cellIndex < 0) {
            this.setEditable(false);
        }
    }

    private void triggerDocumentation() {
        Cell focusedCell = getItem();
        if (focusedCell.hasDocumentation()) {
            MainForm.documentationPopup.setData(focusedCell);
            Window parent = IceCell.this.getScene().getWindow();
            MainForm.documentationPopup.show(parent,
                    parent.getX() + IceCell.this.localToScene(0.0D, 0.0D).getX() +
                            IceCell.this.getScene().getX() + IceCell.this.getWidth(),
                    parent.getY() + IceCell.this.localToScene(0.0D, 0.0D).getY() +
                            IceCell.this.getScene().getY() + 0);
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
            if (this.snowTableView.triggerAutocompletionNext) {
                this.codeCompletionBinding.trigger();
            }
        }
    }

    @Override
    public void commitEdit(Cell newValue) {
        super.commitEdit(newValue);
        if (snowTableView.snowTableKind == SnowTableKind.SETTINGS) {
            ((Suite) snowTableView.getScenario()).reparseResources();
        }
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
        snowTableView.considerAddingVirtualRowsAndColumns();
    }

    private TextField ensureTextField() {
        if (textField == null) {
            textField = new TextField();
            textField.setStyle("-fx-text-box-border: transparent; -fx-background-insets: 0; -fx-focus-color: transparent; -fx-border-width: 0;");
            textField.setPadding(new Insets(0));
            textField.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    commit();
                    event.consume();
                }
            });
            codeCompletionBinding = new CodeCompletionBinding(textField, this, snowTableView.snowTableKind);
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
            if (item.triggerDocumentationNext) {
                triggerDocumentation();
                item.triggerDocumentationNext = false;
            }
        }

    }

    private void updateStyle() {
        String style = "";
        if (cellIndex <= -1) {
            style += "-fx-font-weight: bold; -fx-background-color: lavender; -fx-text-alignment: right; -fx-alignment: center; ";
            setStyle(style);
        } else {
            if (snowTableView.snowTableKind.isScenario()) {
                setStyle(null);
                setStyle(getItem().getStyle());
            } else {
                if (cellIndex == 0) {
                    style += "-fx-font-weight: bold; ";
                    if (snowTableView.snowTableKind == SnowTableKind.SETTINGS) {
                        style += "-fx-text-fill: darkmagenta; ";
                    } else {
                        style += "-fx-text-fill: green; ";
                    }
                    setStyle(style);
                } else {
                    setStyle(null);
                }
            }
        }


    }
}

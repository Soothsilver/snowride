package cz.hudecekpetr.snowride.fx.grid;

import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.semantics.codecompletion.CodeCompletionBinding;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.undo.ChangeTextOperation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
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

    private static TablePosition<LogicalLine, Cell> fullDragStartedAt = null;

    public IceCell(TableColumn<LogicalLine, Cell> column, int cellIndex, SnowTableView snowTableView) {
        this.column = column;
        this.cellIndex = cellIndex;
        this.snowTableView = snowTableView;
        this.setPadding(new Insets(0));
        this.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (getItem() != null) {
                    startFullDrag();
                    fullDragStartedAt = new TablePosition<>(snowTableView, getItem().partOfLine.lineNumber.getValue(), column);
                    event.consume();
                }
            }
        });
        this.setOnMouseDragEntered(new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent event) {

                if (fullDragStartedAt != null && getItem() != null) {
                    TablePosition<LogicalLine, Cell> fullDragEndedAt = new TablePosition<>(snowTableView, getItem().partOfLine.lineNumber.getValue(), column);
                    snowTableView.getSelectionModel().clearSelection();
                    snowTableView.getSelectionModel().selectRange(fullDragStartedAt.getRow(), fullDragStartedAt.getTableColumn(),
                            fullDragEndedAt.getRow(), fullDragEndedAt.getTableColumn());
                    event.consume();
                }
            }
        });
        this.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent event) {
                    fullDragStartedAt = null;
            }
        });
        if (cellIndex < 0) {
            // Only the "Row" column has cells with 'cellIndex" less than 0 (it's -1).
            this.setEditable(false);
        }
    }

    private void triggerDocumentation() {
        Cell focusedCell = getItem();
        if (focusedCell.hasDocumentation()) {
            MainForm.documentationPopup.setData(focusedCell);
            Window parent = IceCell.this.getScene().getWindow();
            MainForm.documentationPopup.showRightIfPossible(parent,
                    parent.getX() + IceCell.this.localToScene(0.0D, 0.0D).getX() +
                            IceCell.this.getScene().getX(), IceCell.this.getWidth(),
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
        snowTableView.getScenario().getUndoStack().iJustDid(new ChangeTextOperation(snowTableView.getItems(), this.getItem().contents, newValue.contents, this.getItem().partOfLine.lineNumber.getValue(), this.getItem().partOfLine.cells.indexOf(this.getItem())));
        super.commitEdit(newValue);
        if (snowTableView.snowTableKind == SnowTableKind.SETTINGS) {
            ((Suite) snowTableView.getScenario()).reparseResources();
        }
        if (getScene().getFocusOwner() == textField) {
             column.getTableView().requestFocus();
        }
    }

    public void commit() {
        Cell newCell = constructNewCell();
        commitEdit(newCell);
        snowTableView.considerAddingVirtualRowsAndColumns();
    }

    private Cell constructNewCell() {
        String oldTrivia = this.getItem().postTrivia;
        if (!oldTrivia.contains("\t") && !oldTrivia.contains("  ")) {
            oldTrivia = "    ";
        }
        // non-virtual:
        return new Cell(textField.getText(), oldTrivia, getItem().partOfLine);
    }

    private void trueCancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        setText(this.getItem().contents);
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
                        trueCancelEdit();
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
        // Actually, we'd prefer a commit, thank you very much.
        Cell newCell = constructNewCell();
        snowTableView.getScenario().getUndoStack().iJustDid(new ChangeTextOperation(snowTableView.getItems(), this.getItem().contents, newCell.contents, this.getItem().partOfLine.lineNumber.getValue(), this.getItem().partOfLine.cells.indexOf(this.getItem())));
        getItem().partOfLine.getCellAsStringProperty(cellIndex, MainForm.INSTANCE).set(newCell);
        if (snowTableView.snowTableKind == SnowTableKind.SETTINGS) {
            ((Suite) snowTableView.getScenario()).reparseResources();
        }
        snowTableView.considerAddingVirtualRowsAndColumns();
        trueCancelEdit();
    }

    @Override
    protected void updateItem(Cell item, boolean empty) {

        super.updateItem(item, empty);
        styleProperty().unbind();
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setStyle(null);
        } else {
            setText(item.contents);
            setGraphic(null);
            if (item.isLineNumberCell) {
                String style = "-fx-padding: 0; -fx-background-insets: 0.0; ";
                style += "-fx-font-weight: bold; -fx-background-color: lavender; -fx-alignment: center; ";
                setStyle(style);
            } else {
                // TODO This is a potential performance bottleneck.
                styleProperty().bind(item.getStyleProperty());
            }
            if (item.triggerDocumentationNext) {
                triggerDocumentation();
                item.triggerDocumentationNext = false;
            }
        }
    }
}

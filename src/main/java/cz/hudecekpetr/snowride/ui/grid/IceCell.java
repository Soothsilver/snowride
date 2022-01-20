package cz.hudecekpetr.snowride.ui.grid;

import cz.hudecekpetr.snowride.fx.Underlining;
import cz.hudecekpetr.snowride.semantics.codecompletion.CodeCompletionBinding;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.undo.ChangeTextOperation;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.robotframework.jaxb.BodyItemStatusValue;
import org.robotframework.jaxb.Keyword;

public class IceCell extends TableCell<LogicalLine, Cell> {
    private static final String LINE_NUMBER_CELL_STYLE = "-fx-padding: 0; -fx-background-insets: 0.0; -fx-font-weight: bold;  -fx-alignment: center;";

    private static TablePosition<LogicalLine, Cell> fullDragStartedAt = null;
    private final SnowTableView snowTableView;
    private TableColumn<LogicalLine, Cell> column;
    private CodeCompletionBinding codeCompletionBinding;
    private int cellIndex;
    private TextField textField;

    public IceCell(TableColumn<LogicalLine, Cell> column, int cellIndex, SnowTableView snowTableView) {
        this.column = column;
        this.cellIndex = cellIndex;
        this.snowTableView = snowTableView;
        this.setOnMouseEntered(event -> Underlining.updateCellTo(getItem()));
        this.setOnMouseExited(event -> {
            if (Underlining.getActiveCell() == getItem()) {
                Underlining.updateCellTo(null);
            }
        });
        this.setPadding(new Insets(0));
        this.setOnDragDetected(event -> {
            if (getItem() != null && getItem().partOfLine != null) {
                startFullDrag();
                fullDragStartedAt = new TablePosition<>(snowTableView, getItem().partOfLine.lineNumber.getValue(), column);
                event.consume();
            }
        });
        this.setOnMouseDragEntered(event -> {

            if (fullDragStartedAt != null && getItem() != null && getItem().partOfLine != null) {
                TablePosition<LogicalLine, Cell> fullDragEndedAt = new TablePosition<>(snowTableView, getItem().partOfLine.lineNumber.getValue(), column);
                snowTableView.getSelectionModel().clearSelection();
                snowTableView.getSelectionModel().selectRange(fullDragStartedAt.getRow(), fullDragStartedAt.getTableColumn(),
                        fullDragEndedAt.getRow(), fullDragEndedAt.getTableColumn());
                event.consume();
            }
        });
        this.setOnMouseDragReleased(event -> fullDragStartedAt = null);
        if (cellIndex < 0) {
            // Only the "Row" column has cells with 'cellIndex" less than 0 (it's -1).
            this.setEditable(false);
        }
    }

    public int getCellIndex() {
        return cellIndex;
    }

    private void triggerDocumentation() {
        Cell focusedCell = getItem();
        if (focusedCell.hasDocumentation()) {
            MainForm.documentationPopup.setData(focusedCell);
            MainForm.documentationPopup.showRightIfPossible(this);
        }
    }

    private void triggerMessages(Cell item) {
        if (item.partOfLine.keywordArguments != null) {
            MainForm.messagesPopup.setArgs(item.partOfLine.keywordArguments);
            MainForm.messagesPopup.showRightIfPossible(this);
        } else if (item.partOfLine.forIterations != null) {
            MainForm.messagesPopup.setIterations(item.partOfLine.forIterations);
            MainForm.messagesPopup.showRightIfPossible(this);
        } else if (item.partOfLine.forLoop != null) {
            MainForm.messagesPopup.setMessages(item.partOfLine.forLoop.getMsg());
            MainForm.messagesPopup.showRightIfPossible(this);
        } else if (item.partOfLine.keyword != null) {
            MainForm.messagesPopup.setKeyword(item.partOfLine.keyword);
            MainForm.messagesPopup.showRightIfPossible(this);
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
        snowTableView.getScenario().getUndoStack().iJustDid(new ChangeTextOperation(snowTableView.getItems(), this.getItem().contents, newValue.contents, this.getItem().postTrivia, this.getItem().partOfLine.lineNumber.getValue(), this.getItem().partOfLine.cells.indexOf(this.getItem())));
        super.commitEdit(newValue);
        if (snowTableView.snowTableKind == SnowTableKind.SETTINGS) {
            ((Suite) snowTableView.getScenario()).reparseAndRecalculateResources();
        }
        if (getScene().getFocusOwner() == textField) {
            column.getTableView().requestFocus();
        }

        // recalculate styles when new value is entered
        HighElement belongsToHighElement = newValue.partOfLine.getBelongsToHighElement();
        if (belongsToHighElement instanceof Scenario && Settings.getInstance().cbHighlightSameCells) {
            YellowHighlight.lastPositionSelectText = newValue.contents;
            Scenario scenario = (Scenario) belongsToHighElement;
            scenario.getLines().forEach(LogicalLine::recalcStyles);
        }

        snowTableView.considerAddingVirtualRowsAndColumns();
    }

    public void commit() {
        Cell newCell = constructNewCell();
        commitEdit(newCell);
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
        setTextAndGraphicTo(this.getItem());
    }

    private TextField ensureTextField() {
        if (textField == null) {
            textField = new TextField();
            textField.setStyle("-fx-text-box-border: transparent; -fx-background-insets: 0; -fx-focus-color: transparent; -fx-border-width: 0;");
            textField.setPadding(new Insets(0));
            textField.setOnAction(event -> {
                commit();
                event.consume();
            });
            codeCompletionBinding = new CodeCompletionBinding(textField, this, snowTableView.snowTableKind);
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    trueCancelEdit();
                    event.consume();
                }
                if (event.getCode() == KeyCode.TAB) {
                    commit();
                    snowTableView.selectCell(0, 1);
                    event.consume();
                }
            });
            textField.widthProperty().addListener((observable, oldValue, newValue) -> {
                int oldCaret = textField.getCaretPosition();
                int oldAnchor = textField.getAnchor();
                textField.selectRange(0, 0);
                textField.selectRange(oldAnchor, oldCaret);
            });
            textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                // prevent issues with uncommitted changes when switching from 'Grid editor' when editing cell
                if (!newValue && isEditing()) {
                    cancelEdit();
                }
            });
        }
        return textField;
    }

    @Override
    public void cancelEdit() {
        // Actually, we'd prefer a commit, thank you very much.
        Integer lineNumber = this.getItem().partOfLine.lineNumber.getValue();
        int columnIndex = this.getItem().partOfLine.cells.indexOf(this.getItem());
        if (lineNumber < 0 || columnIndex < 0) {
            // cancel for a line / column which is no longer available
            return;
        }
        if (!textField.getText().equals(this.getItem().contents)) {
            Cell newCell = constructNewCell();
            snowTableView.getScenario().getUndoStack().iJustDid(new ChangeTextOperation(snowTableView.getItems(), this.getItem().contents, newCell.contents, this.getItem().postTrivia, lineNumber, columnIndex));
            getItem().partOfLine.getCellAsStringProperty(cellIndex, MainForm.INSTANCE).set(newCell);
            if (snowTableView.snowTableKind == SnowTableKind.SETTINGS) {
                ((Suite) snowTableView.getScenario()).reparseAndRecalculateResources();
            }
            snowTableView.considerAddingVirtualRowsAndColumns();
        }
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
            setTextAndGraphicTo(item);
            updateLineNumberCellStyle(item);
            if (!item.isLineNumberCell) {
                // TODO This is a potential performance bottleneck.
                styleProperty().bind(item.getStyleProperty());
            }
            if (item.triggerDocumentationNext) {
                triggerDocumentation();
                item.triggerDocumentationNext = false;
            }
            if (item.triggerMessagesNext) {
                triggerMessages(item);
                item.triggerMessagesNext = false;
            }
        }
    }

    public void updateLineNumberCellStyle(Cell cell) {
        if (cell.isLineNumberCell) {
            if (cell.iceCell == null) {
                cell.iceCell = this;
            }
            String style = LINE_NUMBER_CELL_STYLE;
            String color = "lavender";
            if (cell.partOfLine != null && cell.partOfLine.status != null) {
                BodyItemStatusValue status = cell.partOfLine.status;
                if (status == BodyItemStatusValue.FAIL) {
                    color = "#ce3e01";
                    //style += "-fx-background-color: #ce3e01;";
                } else if (status == BodyItemStatusValue.PASS) {
                    color = "#97bd61";
                    //style += "-fx-background-color: #97bd61;";
                } else if (status == BodyItemStatusValue.SKIP) {
                    color = "#dddddd";
                    //style += "-fx-background-color: #dddddd;";
                } else if (status == BodyItemStatusValue.NOT_RUN) {
                    color = "lightgray";
                    //style += "-fx-background-color: lightgray;";
                }

                if (cell.partOfLine.doesNotMatch) {
                    style += "-fx-background-color: linear-gradient(from 12px 12px to 0px 0px, " + color + " 50%, #F4A460 10%);";
                } else {
                    style += "-fx-background-color: " + color + ";";
                }
            } else {
                style += "-fx-background-color: lavender;";
            }
            setStyle(style);
        }
    }


    private void setTextAndGraphicTo(Cell item) {
        setText(item.contents);
        setGraphic(null);
    }
}

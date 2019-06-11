package cz.hudecekpetr.snowride.fx.grid;

import com.sun.javafx.scene.control.skin.PrecursorTableViewSkin;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.TableClipboard;
import cz.hudecekpetr.snowride.fx.bindings.IntToCellBinding;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.undo.AddRowOperation;
import cz.hudecekpetr.snowride.undo.ChangeTextOperation;
import cz.hudecekpetr.snowride.undo.MassOperation;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class SnowTableView extends TableView<LogicalLine> {

    public SnowTableKind snowTableKind;
    public boolean triggerAutocompletionNext;
    public HighElement scenario;
    private MainForm mainForm;

    public SnowTableView(MainForm mainForm, SnowTableKind snowTableKind) {
        super();
        this.mainForm = mainForm;
        this.snowTableKind = snowTableKind;
        this.setEditable(true);
        this.getSelectionModel().setCellSelectionEnabled(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.setStyle("-fx-selection-bar: lightyellow;");
        this.skinProperty().addListener(new ChangeListener<Skin<?>>() {
            @Override
            public void changed(ObservableValue<? extends Skin<?>> observable, Skin<?> oldValue, Skin<?> newValue) {
                final TableHeaderRow header = (TableHeaderRow) lookup("TableHeaderRow");
                header.reorderingProperty().addListener((o, oldVal, newVal) -> header.setReordering(false));
            }
        });
        TableColumn<LogicalLine, Cell> rowColumn = createColumn(-1);
        rowColumn.setText("Row");
        rowColumn.setPrefWidth(30);
        rowColumn.setStyle("-fx-alignment: center;");
        this.getColumns().add(rowColumn);
        this.setOnKeyPressed(this::onKeyPressed);
        this.setOnMouseClicked(this::onMouseClicked);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PrecursorTableViewSkin<>(this);
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        MainForm.documentationPopup.hide();
        if (mouseEvent.isControlDown()) {
            if (snowTableKind.isScenario()) {
                Cell cell = getFocusedCell();
                IKnownKeyword keyword = cell.getKeywordInThisCell();
                if (keyword != null) {
                    Scenario highElement = keyword.getScenarioIfPossible();
                    if (highElement != null) {
                        mainForm.selectProgrammaticallyAndRememberInHistory(highElement);
                    } else {
                        mainForm.toast("Keyword '" + keyword.getAutocompleteText() + "' is not a known user keyword. Cannot go to definition.");
                    }
                }
            } else {
                Cell cell = getFocusedCellInSettingsTable();
                if (cell != null && cell.leadsToSuite != null) {
                    mainForm.selectProgrammaticallyAndRememberInHistory(cell.leadsToSuite);
                }
            }
        }
    }

    private Cell getFocusedCell() {
        TablePosition<LogicalLine, Cell> focusedCell = getFocusedTablePosition();
        SimpleObjectProperty<Cell> cellSimpleObjectProperty = tablePositionToCell(focusedCell);
        return cellSimpleObjectProperty.getValue();
    }

    private Cell getFocusedCellInSettingsTable() {
        TablePosition<LogicalLine, Cell> focusedCell = getFocusedTablePosition();
        int colIndex = focusedCell.getColumn() - 1;
        if (colIndex >= 0) {
            SimpleObjectProperty<Cell> cellSimpleObjectProperty = this.getItems().get(focusedCell.getRow()).getCellAsStringProperty(colIndex, mainForm);
            return cellSimpleObjectProperty.getValue();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private TablePosition<LogicalLine, Cell> getFocusedTablePosition() {
        return this.focusModelProperty().get().focusedCellProperty().get();
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        MainForm.documentationPopup.hide();
        if (keyEvent.getCode() == KeyCode.I && keyEvent.isControlDown()) {
            // Insert
            int whatFocused = this.getFocusModel().getFocusedIndex();
            this.getItems().add(whatFocused, createNewLine());
            int column = 1;
            if (this.getSelectionModel().getSelectedCells().size() > 0) {
                column = this.getSelectionModel().getSelectedCells().get(0).getColumn();
            }
            this.getSelectionModel().clearAndSelect(whatFocused, getVisibleLeafColumn(column));
            this.getScenario().getUndoStack().iJustDid(new AddRowOperation(getItems(), whatFocused, () -> createNewLine()));
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.A && keyEvent.isControlDown()) {
            // Append
            int whatFocused = this.getFocusModel().getFocusedIndex();
            this.getItems().add(whatFocused + 1, createNewLine());
            int column = 1;
            if (this.getSelectionModel().getSelectedCells().size() > 0) {
                column = this.getSelectionModel().getSelectedCells().get(0).getColumn();
            }
            this.getSelectionModel().clearAndSelect(whatFocused + 1, getVisibleLeafColumn(column));
            this.getScenario().getUndoStack().iJustDid(new AddRowOperation(getItems(), whatFocused + 1, () -> createNewLine()));
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.Z && keyEvent.isControlDown()) {
            getScenario().getUndoStack().undoIfAble();
        } else if (keyEvent.getCode() == KeyCode.Y && keyEvent.isControlDown()) {
            getScenario().getUndoStack().redoIfAble();
        } else if (keyEvent.getCode() == KeyCode.SPACE && keyEvent.isControlDown()) {
            TablePosition<LogicalLine, ?> focusedCell = getFocusedTablePosition();
            this.triggerAutocompletionNext = true;
            this.edit(focusedCell.getRow(), focusedCell.getTableColumn());
            this.triggerAutocompletionNext = false;
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode() == KeyCode.DELETE) {
            List<ChangeTextOperation> coordinates = new ArrayList<>();
            for (TablePosition tablePosition : this.getSelectionModel().getSelectedCells()) {
                SimpleObjectProperty<Cell> cell = tablePositionToCell(tablePosition);
                coordinates.add(new ChangeTextOperation(getItems(), cell.getValue().contents, "", tablePosition.getRow(), tableXToLogicalX(tablePosition.getColumn())));
                cell.set(new Cell("", cell.getValue().postTrivia, cell.getValue().partOfLine));
            }
            if (coordinates.size() > 0) {
                getScenario().getUndoStack().iJustDid(new MassOperation(coordinates));
            }
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.C && keyEvent.isControlDown()) {
            cutOrCopy(false);
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.X && keyEvent.isControlDown()) {
            cutOrCopy(true);
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.V && keyEvent.isControlDown()) {
            TablePosition<LogicalLine, Cell> focusedTablePosition = getFocusedTablePosition();
            pasteStartingAt(focusedTablePosition);
            keyEvent.consume();
        } else if ((keyEvent.getCode() == KeyCode.SLASH || keyEvent.getCode() == KeyCode.DIVIDE) && keyEvent.isControlDown()) {
            SimpleObjectProperty<Cell> cell = tablePositionToCell(getSelectionModel().getSelectedCells().get(0));
            LogicalLine theLine = cell.getValue().partOfLine;
            Cell firstCell = theLine.getCellAsStringProperty(1, mainForm).getValue();
            if (keyEvent.isShiftDown()) {
                // uncomment
                if (Extensions.toInvariant(firstCell.contents).equalsIgnoreCase("Comment")) {
                    theLine.shiftTrueCellsLeft(mainForm);
                }
                theLine.getCellAsStringProperty(0, mainForm).set(new Cell("", "    ", theLine));
            } else {
                // comment out
                theLine.shiftTrueCellsRight(mainForm);
                theLine.getCellAsStringProperty(1, mainForm).set(new Cell("Comment", "    ", theLine));
            }
        } else if ((keyEvent.getCode() == KeyCode.Q && keyEvent.isControlDown()) || keyEvent.getCode() == KeyCode.F1) {
            if (getSelectionModel().getSelectedCells().size() > 0) {
                SimpleObjectProperty<Cell> cell = tablePositionToCell(getSelectionModel().getSelectedCells().get(0));
                Cell copy = cell.getValue().copy();
                copy.triggerDocumentationNext = true;
                cell.set(copy);
                keyEvent.consume();
            }
        } else if (keyEvent.getCode() == KeyCode.TAB) {
            this.getSelectionModel().clearSelection();
            this.getSelectionModel().selectNext();
            keyEvent.consume();
        } else if (!keyEvent.getCode().isArrowKey() && !keyEvent.getCode().isFunctionKey() && !keyEvent.getCode().isModifierKey()
                && !keyEvent.getCode().isNavigationKey() && !keyEvent.getCode().isWhitespaceKey() && !keyEvent.isControlDown()
                && keyEvent.getCode() != KeyCode.ESCAPE) {
            TablePosition<LogicalLine, ?> focusedCell = getFocusedTablePosition();
            this.edit(focusedCell.getRow(), focusedCell.getTableColumn());
            keyEvent.consume();
        }
    }

    private void pasteStartingAt(TablePosition<LogicalLine, Cell> startingPosition) {
        String clipboardData = Clipboard.getSystemClipboard().getString();
        if (clipboardData == null) {
            return;
        }
        String[] lines = StringUtils.split(clipboardData, '\n');
        int atRow = startingPosition.getRow();
        List<ChangeTextOperation> operations = new ArrayList<>();
        for (String line : lines) {
            // Create lines if there's not enough of them:
            if (this.getItems().size() <= atRow) {
                this.getItems().add(createNewLine());
            }
            String[] cellSplit = StringUtils.split(line, '\t');
            int atColumn = startingPosition.getColumn();
            for (int xi = 0; xi < cellSplit.length; xi++) {
                int x = atColumn + xi;
                int y = atRow;
                SimpleObjectProperty<Cell> replacedCell = getItems().get(y).getCellAsStringProperty(snowTableKind == SnowTableKind.SCENARIO ? x : x - 1, mainForm);
                operations.add(new ChangeTextOperation(getItems(), replacedCell.getValue().contents, cellSplit[xi].trim(),  y, tableXToLogicalX(x)));
                replacedCell.set(new Cell(cellSplit[xi].trim(), "    ", getItems().get(y)));
            }
            atRow++;
        }
        if (operations.size() > 0) {
            getScenario().getUndoStack().iJustDid(new MassOperation(operations));
        }
        // t
    }

    private int tableXToLogicalX(int tableX) {
        if (snowTableKind == SnowTableKind.SCENARIO) {
            return tableX;
        } else {
            return tableX - 1;
        }
    }

    private void cutOrCopy(boolean alsoCut) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (TablePosition tp : getSelectionModel().getSelectedCells()) {
            if (tp.getRow() < minY) {
                minY = tp.getRow();
            }
            if (tp.getRow() > maxY) {
                maxY = tp.getRow();
            }
            if (tp.getColumn() < minX) {
                minX = tp.getColumn();
            }
            if (tp.getColumn() > maxX) {
                maxX = tp.getColumn();
            }
        }
        StringBuilder clipboardContents = new StringBuilder();

        List<ChangeTextOperation> coordinates = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                SimpleObjectProperty<Cell> cell = getItems().get(y).getCellAsStringProperty(snowTableKind == SnowTableKind.SCENARIO ? x : x - 1, mainForm);
                clipboardContents.append(cell.getValue().contents);
                if (alsoCut) {
                    coordinates.add(new ChangeTextOperation(getItems(), cell.getValue().contents, "", y, tableXToLogicalX(x)));
                    cell.set(new Cell("", cell.getValue().postTrivia, cell.getValue().partOfLine));

                }
                if (x != maxX) {
                    clipboardContents.append("\t");
                }
            }
            if (y != maxY) {
                clipboardContents.append("\n"); // Single newline only: this is important because JavaFX or Windows automatically replaces "\n"
                // and "\r" in clipboard with \r\n so "\r\n" translates to "\r\n\r\n".
            }
        }
        TableClipboard.store(clipboardContents.toString());
        if (coordinates.size() > 0) {
            getScenario().getUndoStack().iJustDid(new MassOperation(coordinates));
        }
    }

    private LogicalLine createNewLine() {
        LogicalLine newLine = new LogicalLine();
        newLine.belongsToHighElement = scenario;
        newLine.lineNumber = new PositionInListProperty<>(newLine, this.getItems());
        newLine.belongsWhere = snowTableKind;
        newLine.recalcStyles();
        return newLine;
    }

    private TableColumn<LogicalLine, Cell> createColumn(int cellIndex) {
        TableColumn<LogicalLine, Cell> column = new TableColumn<>();
        column.setSortable(false);
        column.setMinWidth(40);
        column.setCellFactory(new Callback<TableColumn<LogicalLine, Cell>, TableCell<LogicalLine, Cell>>() {
            @Override
            public TableCell<LogicalLine, Cell> call(TableColumn<LogicalLine, Cell> param) {
                return new IceCell(param, cellIndex, SnowTableView.this);
            }
        });
        column.setPrefWidth(200);
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<LogicalLine, Cell>, ObservableValue<Cell>>() {
            @Override
            public ObservableValue<Cell> call(TableColumn.CellDataFeatures<LogicalLine, Cell> param) {
                if (cellIndex == -1) {
                    return new IntToCellBinding(param.getValue().lineNumber.add(1));
                }
                if (param.getValue() != null) {
                    return param.getValue().getCellAsStringProperty(cellIndex, mainForm);
                } else {
                    return new ReadOnlyObjectWrapper<>(new Cell("(non-existing line)", "", null));
                }
            }
        });
        return column;
    }

    public void loadLines(HighElement highElement, ObservableList<LogicalLine> lines) {
        scenario = highElement;
        // For key-value tables:
        for (LogicalLine line : lines) {
            line.belongsToHighElement = highElement;
            line.belongsWhere = snowTableKind;
            line.recalcStyles();
        }
        // Renew data
        this.setItems(lines);
        // Column count
        int maxCellCount = lines.size() == 0 ? -1 : Extensions.max(lines, (LogicalLine line) -> line.cells.size()) - 1; // -1 for the first blank cell
        int columnCount = Math.max(maxCellCount + 1, 4) + 1; // +1 for "number of row"
        if (this.getColumns().size() > columnCount) {
            this.getColumns().remove(columnCount, this.getColumns().size());
        } else {
            int numberOfColumnsToAdd = columnCount - this.getColumns().size();
            List<TableColumn<LogicalLine, Cell>> newColumns = new ArrayList<>(numberOfColumnsToAdd);
            for (int i = this.getColumns().size(); i < columnCount; i++) {
                if (snowTableKind.isScenario()) {
                    newColumns.add(createColumn(i)); // start at cell 1, not 0 (0 is blank for test cases and keywords)
                } else {
                    newColumns.add(createColumn(i - 1));
                }
            }
            this.getColumns().addAll(newColumns);
        }
        this.considerAddingVirtualRowsAndColumns();
    }

    private SimpleObjectProperty<Cell> tablePositionToCell(TablePosition position) {
        return this.getItems().get(position.getRow()).getCellAsStringProperty(snowTableKind == SnowTableKind.SCENARIO ? position.getColumn() : position.getColumn() - 1, mainForm);
    }


    public void considerAddingVirtualRowsAndColumns() {
        int virtualRows = 0;
        for (int i = getItems().size() - 1; i >= 0; i--) {
            LogicalLine line = getItems().get(i);
            if (line.isFullyVirtual()) {
                virtualRows++;
            }
            if (virtualRows >= 4) {
                // That's enough. That's fine. We don't need more.
                return;
            }
        }
        while (virtualRows < 4) {
            getItems().add(createNewLine());
            virtualRows++;
        }
    }

    public void goRight() {
        this.getFocusModel().focusRightCell();
    }

    public HighElement getScenario() {
        return scenario;
    }
}

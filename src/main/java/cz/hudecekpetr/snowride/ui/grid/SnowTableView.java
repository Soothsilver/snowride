package cz.hudecekpetr.snowride.ui.grid;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.fx.TableClipboard;
import cz.hudecekpetr.snowride.fx.bindings.IntToCellBinding;
import cz.hudecekpetr.snowride.output.OutputMatcher;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.findusages.FindUsages;
import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.ui.SnowTableArrowSelectionHelper;
import cz.hudecekpetr.snowride.undo.AddRowOperation;
import cz.hudecekpetr.snowride.undo.ChangeTextOperation;
import cz.hudecekpetr.snowride.undo.MassOperation;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.*;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class SnowTableView extends TableView<LogicalLine> {
    private final static Integer DEFAULT_FONT_SIZE = 12;
    private final static Integer DEFAULT_CELL_SIZE = 23;
    private Integer fontSize = DEFAULT_FONT_SIZE;
    private Integer cellSize = DEFAULT_CELL_SIZE;

    public SnowTableKind snowTableKind;
    public boolean triggerAutocompletionNext;
    public HighElement scenario;
    private final MainForm mainForm;

    private final SnowTableArrowSelectionHelper arrowSelectionHelper = new SnowTableArrowSelectionHelper();

    public SnowTableView(MainForm mainForm, SnowTableKind snowTableKind) {
        super();
        this.mainForm = mainForm;
        this.snowTableKind = snowTableKind;
        this.getStyleClass().add("snow");
        this.setEditable(true);
        this.getSelectionModel().setCellSelectionEnabled(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.skinProperty().addListener((observable, oldValue, newValue) -> {
            final TableHeaderRow header = (TableHeaderRow) lookup("TableHeaderRow");
            header.reorderingProperty().addListener((o, oldVal, newVal) -> header.setReordering(false));
        });
        TableColumn<LogicalLine, Cell> rowColumn = createColumn(-1);
        rowColumn.setText("Row");
        rowColumn.setPrefWidth(30);
        rowColumn.setStyle("-fx-alignment: center;");
        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            arrowSelectionHelper.selectionModelChange(getSelectionModel());
        });
        this.getSelectionModel().getSelectedCells().addListener(this::cellSelectionChanged);
        this.getColumns().add(rowColumn);
        ContextMenu cmenu = new ContextMenu(new MenuItem("Something"));
        this.setOnContextMenuRequested(event -> {
            cmenu.getItems().clear();
            if (getSelectionModel().getSelectedItem() != null) {
                List<MenuItem> contextMenuItems = recreateContextMenu(event);
                cmenu.getItems().setAll(contextMenuItems);
            } else {
                event.consume();
            }
        });
        this.setContextMenu(cmenu);
        this.setOnKeyPressed(this::onKeyPressed);
        this.setOnMouseClicked(this::onMouseClicked);
        this.addEventHandler(ScrollEvent.ANY, event -> {
            if (event.isShortcutDown()) {
                if (event.getDeltaY() > 0) {
                    if (fontSize < 30) {
                        fontSize++;
                        cellSize = fontSize * 2 - 1;
                    }
                } else {
                    if (fontSize > 4) {
                        fontSize--;
                        cellSize = fontSize * 2 - 1;
                    }
                }
                applyFontSize();
                event.consume();
            }
        });
    }

    private List<MenuItem> recreateContextMenu(ContextMenuEvent contextMenuEvent) {
        List<MenuItem> contextMenuItems = new ArrayList<>();

        MenuItem miInsertCell = new MenuItem("Insert cell");
        miInsertCell.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHIFT_DOWN,
                KeyCombination.SHORTCUT_DOWN));
        miInsertCell.setOnAction(event -> insertSelectedCells());

        MenuItem miDeleteCell = new MenuItem("Delete cell");
        miDeleteCell.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN,
                KeyCombination.SHORTCUT_DOWN));
        miDeleteCell.setOnAction(event -> deleteSelectedCells());

        MenuItem miInsertRow = new MenuItem("Insert row before this");
        miInsertRow.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));
        miInsertRow.setOnAction(event -> insertRowBefore());

        MenuItem miAppendRow = new MenuItem("Append row after this");
        miAppendRow.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN));
        miAppendRow.setOnAction(event -> appendRowAfter());

        MenuItem miDeleteRows = new MenuItem("Delete all selected rows");
        miDeleteRows.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN));
        miDeleteRows.setOnAction(event -> deleteSelectedRows());

        if (getSelectionModel().getSelectedCells().size() > 0) {
            TablePosition lastPosition = getSelectionModel().getSelectedCells().get(0);
            if (lastPosition.getColumn() != -1 && lastPosition.getRow() != -1) {
                LogicalLine line = getItems().get(lastPosition.getRow());
                Cell thisCell = line.getCellAsStringProperty(tableXToLogicalX(lastPosition.getColumn()), mainForm).getValue();
                IKnownKeyword keywordInThisCell = thisCell.getKeywordInThisCell();
                if (keywordInThisCell != null) {
                    MenuItem miFindUsages = new MenuItem("Find usages");
                    miFindUsages.setOnAction(event -> {
                        MenuItem[] items = FindUsages.findUsages(keywordInThisCell, keywordInThisCell.getScenarioIfPossible(), mainForm.getRootElement()).toArray(new MenuItem[0]);
                        ContextMenu menuUsages = new ContextMenu(items);
                        menuUsages.show(mainForm.getStage(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                    });
                    contextMenuItems.add(miFindUsages);
                    contextMenuItems.add(new SeparatorMenuItem());
                } else if (thisCell.getSemantics().isKeyword) {
                    MenuItem miCreateThis = new MenuItem("Create this keyword in the same file");
                    miCreateThis.setOnAction(event -> {
                        Suite suite = scenario.parent;
                        Scenario newKeyword = suite.createNewChild(thisCell.contents, false, mainForm);
                        // TODO add arguments and return value rows
                        mainForm.changeOccurredTo(suite, LastChangeKind.STRUCTURE_CHANGED);
                    });
                    contextMenuItems.add(miCreateThis);
                    contextMenuItems.add(new SeparatorMenuItem());
                }
            }
        }
        if (snowTableKind == SnowTableKind.SCENARIO) {
            MenuItem miComment = new MenuItem("Comment/Uncomment");
            miComment.setOnAction(event -> commentOutOrUncomment());
            contextMenuItems.add(miComment);
        }

        contextMenuItems.add(miInsertCell);
        contextMenuItems.add(miDeleteCell);
        contextMenuItems.add(miInsertRow);
        contextMenuItems.add(miAppendRow);
        contextMenuItems.add(miDeleteRows);
        return contextMenuItems;
    }

    TablePosition<LogicalLine, Cell> lastPositionSelected;
    private boolean dontChangeLastPosition = false;

    private void cellSelectionChanged(ListChangeListener.Change<? extends TablePosition> change) {
        List<TablePosition> toClear = new ArrayList<>();
        List<TablePosition> toSelect = new ArrayList<>();
        // We want to scroll to the new column selected if it's not already visible in the viewport -- but not if it is.
        // I don't know how to do that, though.
        if (!dontChangeLastPosition) {
            lastPositionSelected = null;
        }
        while (change.next()) {
            boolean atLeastOneFirstColumnCellSelected = false;
            for (TablePosition tablePosition : change.getAddedSubList()) {
                if (tablePosition.getColumn() == 0) {
                    atLeastOneFirstColumnCellSelected = true;
                    toClear.add(tablePosition);
                }
                if (!dontChangeLastPosition) {
                    lastPositionSelected = tablePosition;
                    if (tablePosition.getColumn() >= 1) {
                        YellowHighlight.lastPositionSelectText = tablePositionToCell(tablePosition).getValue().contents;
                    }
                }
            }
            if (atLeastOneFirstColumnCellSelected) {
                for (TablePosition tablePosition : change.getRemoved()) {
                    if (tablePosition.getColumn() != 0) {
                        toSelect.add(tablePosition);
                    }
                }
            }
        }
        if (toClear.size() > 0 || toSelect.size() > 0) {
            // Has to be done later to avoid creating an inconsistent selection model (which throws exceptions) -
            // it's not legal to clear/select stuff from within this list change listener. I tried.
            Platform.runLater(() -> {
                dontChangeLastPosition = true;
                for (TablePosition tablePosition : toClear) {
                    this.getSelectionModel().clearSelection(tablePosition.getRow(), tablePosition.getTableColumn());
                }
                for (TablePosition tablePosition : toSelect) {
                    this.getSelectionModel().select(tablePosition.getRow(), tablePosition.getTableColumn());
                }
                dontChangeLastPosition = false;
            });
        }
        for (LogicalLine item : getItems()) {
            item.recalcStyles();
        }
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        MainForm.documentationPopup.hide();
        if (lastPositionSelected != null && lastPositionSelected.getColumn() == 0) {
            mainForm.toast("In Snowride, you must not click the 'Row' column. Right-click the next column instead to get the correct context menu.");
        }
        if (mouseEvent.isShortcutDown()) {
            Cell cell = getCurrentCell();
            if (cell != null) {
                if (cell.leadsToSuite != null) {
                    mainForm.selectProgrammaticallyAndRememberInHistory(cell.leadsToSuite);
                } else {
                    IKnownKeyword keyword = cell.getKeywordInThisCell();
                    if (keyword != null) {
                        Scenario highElement = keyword.getScenarioIfPossible();
                        if (highElement != null) {
                            // TestStack
                            mainForm.navigationStack.currentOutputElement = cell.partOfLine.keyword;
                            mainForm.selectProgrammaticallyAndRememberInHistory(highElement);
                        } else {
                            mainForm.toast("Keyword '" + keyword.getAutocompleteText() + "' is not a known user keyword. Cannot go to definition.");
                        }
                    }
                }
            }
        }
    }

    private Cell getCurrentCell() {
        Cell cell;
        if (snowTableKind.isScenario()) {
            cell = getFocusedCell();
        } else {
            cell = getFocusedCellInSettingsTable();
        }
        return cell;
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
        arrowSelectionHelper.onKeyPressed(keyEvent, getSelectionModel());
        if (keyEvent.isConsumed()) {
            return;
        }
        if ((keyEvent.getCode() == KeyCode.DIGIT0 || keyEvent.getCode() == KeyCode.NUMPAD0) && keyEvent.isShortcutDown()) {
            fontSize = DEFAULT_FONT_SIZE;
            cellSize = DEFAULT_CELL_SIZE;
            applyFontSize();
            keyEvent.consume();
        }
        if (keyEvent.getCode() == KeyCode.I && keyEvent.isShiftDown() && keyEvent.isShortcutDown()) {
            insertSelectedCells();
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.D && keyEvent.isShiftDown() && keyEvent.isShortcutDown()) {
            deleteSelectedCells();
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.I && keyEvent.isShortcutDown()) {
            insertRowBefore();
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.A && keyEvent.isShortcutDown()) {
            appendRowAfter();
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.D && keyEvent.isShortcutDown()) {
            deleteSelectedRows();
            keyEvent.consume();
        } else if (((keyEvent.getCode() == KeyCode.Z && keyEvent.isShiftDown()) || keyEvent.getCode() == KeyCode.Y) && keyEvent.isShortcutDown()) {
            getScenario().getUndoStack().redoIfAble();
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.Z && keyEvent.isShortcutDown()) {
            getScenario().getUndoStack().undoIfAble();
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.SPACE && keyEvent.isShortcutDown()) {
            TablePosition<LogicalLine, ?> focusedCell = getFocusedTablePosition();
            this.triggerAutocompletionNext = true;
            this.edit(focusedCell.getRow(), focusedCell.getTableColumn());
            this.triggerAutocompletionNext = false;
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode() == KeyCode.DELETE) {
            if (this.getSelectionModel().getSelectedCells().stream().anyMatch(tablePosition -> StringUtils.isNotBlank(tablePositionToCell(tablePosition).getValue().contents))) {
                List<ChangeTextOperation> coordinates = new ArrayList<>();
                // Prevent issues with removing empty lines - caused by 'applying changes' on Suite
                Extensions.doNotOptimizeLines = true;
                for (TablePosition tablePosition : this.getSelectionModel().getSelectedCells()) {
                    SimpleObjectProperty<Cell> cell = tablePositionToCell(tablePosition);
                    if (StringUtils.isNotBlank(cell.getValue().contents)) {
                        coordinates.add(new ChangeTextOperation(getItems(), cell.getValue().contents, "", cell.getValue().postTrivia, tablePosition.getRow(), tableXToLogicalX(tablePosition.getColumn())));
                        cell.set(new Cell("", cell.getValue().postTrivia, cell.getValue().partOfLine));
                    }
                    YellowHighlight.lastPositionSelectText = "";
                }
                getScenario().getUndoStack().iJustDid(new MassOperation(coordinates));
                Extensions.doNotOptimizeLines = false;
            }
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.C && keyEvent.isShortcutDown()) {
            cutOrCopy(false);
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.X && keyEvent.isShortcutDown()) {
            cutOrCopy(true);
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.V && keyEvent.isShortcutDown()) {
            TablePosition<LogicalLine, Cell> focusedTablePosition = getFocusedTablePosition();
            pasteStartingAt(focusedTablePosition);
            keyEvent.consume();
        } else if (
                ((keyEvent.getCode() == KeyCode.SLASH || keyEvent.getCode() == KeyCode.DIVIDE) && keyEvent.isShortcutDown()) ||
                        ((keyEvent.getCode() == KeyCode.NUMPAD3 || keyEvent.getCode() == KeyCode.DIGIT3) && keyEvent.isShortcutDown())
        ) {
            commentOutOrUncomment();
        } else if ((keyEvent.getCode() == KeyCode.Q && keyEvent.isShortcutDown()) || keyEvent.getCode() == KeyCode.F1) {
            if (getSelectionModel().getSelectedCells().size() > 0) {
                SimpleObjectProperty<Cell> cell = tablePositionToCell(getSelectionModel().getSelectedCells().get(0));
                Cell copy = cell.getValue().copy();
                copy.triggerDocumentationNext = true;
                cell.set(copy);
                keyEvent.consume();
            }
        } else if ((keyEvent.getCode() == KeyCode.W && keyEvent.isShortcutDown()) || keyEvent.getCode() == KeyCode.F4) {
            if (getSelectionModel().getSelectedCells().size() > 0) {
                SimpleObjectProperty<Cell> cell = tablePositionToCell(getSelectionModel().getSelectedCells().get(0));
                Cell copy = cell.getValue().copy();
                copy.triggerMessagesNext = true;
                cell.set(copy);
                keyEvent.consume();
            }
        } else if (keyEvent.getCode() == KeyCode.TAB) {
            selectCell(0, 1);
            keyEvent.consume();
        } else if (!keyEvent.getCode().isArrowKey() && !keyEvent.getCode().isFunctionKey() && !keyEvent.getCode().isModifierKey()
                && !keyEvent.getCode().isNavigationKey() && !keyEvent.getCode().isWhitespaceKey() && !keyEvent.isShortcutDown()
                && keyEvent.getCode() != KeyCode.ESCAPE) {
            TablePosition<LogicalLine, ?> focusedCell = getFocusedTablePosition();
            this.edit(focusedCell.getRow(), focusedCell.getTableColumn());
            keyEvent.consume();
        }
    }

    private void deleteSelectedRows() {
        Set<Integer> rows = new HashSet<>();
        for (TablePosition selectedCell : this.getSelectionModel().getSelectedCells()) {
            rows.add(selectedCell.getRow());
        }
        List<ChangeTextOperation> operations = new ArrayList<>();
        for (Integer row : rows) {
            LogicalLine line = getItems().get(row);
            for (int i = 0; i < line.cells.size(); i++) {
                SimpleObjectProperty<Cell> cellAsStringProperty = line.getCellAsStringProperty(i, mainForm);
                Cell current = cellAsStringProperty.get();
                operations.add(new ChangeTextOperation(getItems(), current.contents, "", current.postTrivia, row, i));
                cellAsStringProperty.set(new Cell("", current.postTrivia, current.partOfLine));
            }
        }
        getScenario().getUndoStack().iJustDid(new MassOperation(operations));
    }

    private void appendRowAfter() {
        // Append
        int whatFocused = this.getFocusModel().getFocusedIndex();
        this.getItems().add(whatFocused + 1, createNewLine());
        int column = 1;
        if (this.getSelectionModel().getSelectedCells().size() > 0) {
            column = this.getSelectionModel().getSelectedCells().get(0).getColumn();
        }
        this.getSelectionModel().clearAndSelect(whatFocused + 1, getVisibleLeafColumn(column));
        this.getScenario().getUndoStack().iJustDid(new AddRowOperation(getItems(), whatFocused + 1, scenario));
    }

    private void insertRowBefore() {
        int whatFocused = this.getFocusModel().getFocusedIndex();
        this.getItems().add(whatFocused, createNewLine());
        int column = 1;
        if (this.getSelectionModel().getSelectedCells().size() > 0) {
            column = this.getSelectionModel().getSelectedCells().get(0).getColumn();
        }
        this.getSelectionModel().clearAndSelect(whatFocused, getVisibleLeafColumn(column));
        this.getScenario().getUndoStack().iJustDid(new AddRowOperation(getItems(), whatFocused, scenario));
    }

    private void insertSelectedCells() {
        Set<Integer> rows = new HashSet<>();
        for (TablePosition selectedCell : getSelectionModel().getSelectedCells()) {
            rows.add(selectedCell.getRow());
        }

        // column index is always the most left position of selection
        TablePosition selectedCell = getSelectionModel().getSelectedCells().get(0);
        int columnIndex = selectedCell.getColumn();

        // process selected rows
        List<ChangeTextOperation> operations = new ArrayList<>();
        for (Integer row : rows) {
            LogicalLine line = getItems().get(row);
            int cellCount = line.cells.size();

            // shift all following cells to the right
            for (int i = cellCount - 1; i >= columnIndex; i--) {
                SimpleObjectProperty<Cell> rightCellAsString = line.getCellAsStringProperty(i + 1, mainForm);
                SimpleObjectProperty<Cell> currentCellAsString = line.getCellAsStringProperty(i, mainForm);
                Cell currentCellCopy = currentCellAsString.get().copy();
                operations.add(new ChangeTextOperation(
                        getItems(), rightCellAsString.get().contents, currentCellCopy.contents, rightCellAsString.get().postTrivia, row, i + 1));

                rightCellAsString.set(currentCellCopy);
            }

            // insert new empty cell (clear previous content)
            SimpleObjectProperty<Cell> previousCellAsString = line.getCellAsStringProperty(columnIndex, mainForm);
            Cell previousCell = previousCellAsString.get();
            Cell newCell = new Cell("", "    ", line);
            operations.add(new ChangeTextOperation(
                    getItems(), previousCell.contents, newCell.contents, previousCell.postTrivia, row, columnIndex));

            previousCellAsString.set(newCell);
        }
        getScenario().getUndoStack().iJustDid(new MassOperation(operations));
        considerAddingVirtualRowsAndColumns();
    }

    private void deleteSelectedCells() {
        Set<Integer> rows = new HashSet<>();
        for (TablePosition selectedCell : getSelectionModel().getSelectedCells()) {
            rows.add(selectedCell.getRow());
        }

        // column index is always the most left position of selection
        TablePosition selectedCell = getSelectionModel().getSelectedCells().get(0);
        int columnIndex = selectedCell.getColumn();

        // process selected rows
        List<ChangeTextOperation> operations = new ArrayList<>();
        for (Integer row : rows) {
            LogicalLine line = getItems().get(row);
            int cellCount = line.cells.size();

            // shift all following cells to the left
            for (int i = columnIndex + 1; i <= cellCount; i++) {
                SimpleObjectProperty<Cell> leftCellAsString = line.getCellAsStringProperty(i - 1, mainForm);
                Cell leftCell = leftCellAsString.get();
                if (i == cellCount) {
                    Cell newCell = new Cell("", "    ", line);
                    operations.add(new ChangeTextOperation(
                            getItems(), leftCell.contents, newCell.contents, leftCell.postTrivia, row, i - 1));

                    leftCellAsString.set(newCell);

                } else {
                    SimpleObjectProperty<Cell> currentCellAsString = line.getCellAsStringProperty(i, mainForm);
                    Cell currentCellCopy = currentCellAsString.get().copy();
                    operations.add(new ChangeTextOperation(
                            getItems(), leftCell.contents, currentCellCopy.contents, leftCell.postTrivia, row, i - 1));

                    leftCellAsString.set(currentCellCopy);
                }
            }
        }
        getScenario().getUndoStack().iJustDid(new MassOperation(operations));
        considerAddingVirtualRowsAndColumns();
    }

    private void commentOutOrUncomment() {
        Set<LogicalLine> lines = new HashSet<>();
        for (TablePosition selectedCell : getSelectionModel().getSelectedCells()) {
            int row = selectedCell.getRow();
            lines.add(getItems().get(row));
        }
        boolean uncomment = lines.stream().allMatch(line -> {
            Cell cell = line.getCellAsStringProperty(1, mainForm).getValue();
            return Extensions.toInvariant(cell.contents).equalsIgnoreCase("Comment");
        });
        for (LogicalLine theLine : lines) {
            if (uncomment) {
                // uncomment
                theLine.shiftTrueCellsLeft(mainForm);
                theLine.getCellAsStringProperty(0, mainForm).set(new Cell("", "    ", theLine));
            } else {
                // comment out
                theLine.shiftTrueCellsRight(mainForm);
                theLine.getCellAsStringProperty(1, mainForm).set(new Cell("Comment", "    ", theLine));
            }
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
            String[] cellSplit = StringUtils.splitPreserveAllTokens(line, '\t');
            int atColumn = startingPosition.getColumn();
            for (int xi = 0; xi < cellSplit.length; xi++) {
                int x = atColumn + xi;
                int y = atRow;
                SimpleObjectProperty<Cell> replacedCell = getItems().get(y).getCellAsStringProperty(snowTableKind == SnowTableKind.SCENARIO ? x : x - 1, mainForm);
                operations.add(new ChangeTextOperation(getItems(), replacedCell.getValue().contents, cellSplit[xi].trim(), replacedCell.getValue().postTrivia, y, tableXToLogicalX(x)));
                replacedCell.set(new Cell(cellSplit[xi].trim(), "    ", getItems().get(y)));
            }
            atRow++;
        }
        if (operations.size() > 0) {
            getScenario().getUndoStack().iJustDid(new MassOperation(operations));
        }
        considerAddingVirtualRowsAndColumns();
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
                    coordinates.add(new ChangeTextOperation(getItems(), cell.getValue().contents, "", cell.getValue().postTrivia, y, tableXToLogicalX(x)));
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

    public LogicalLine createNewLine() {
        return LogicalLine.createEmptyLine(snowTableKind, scenario, this.getItems());
    }

    private TableColumn<LogicalLine, Cell> createColumn(int cellIndex) {
        TableColumn<LogicalLine, Cell> column = new TableColumn<>();
        column.setSortable(false);
        column.setReorderable(false);
        column.setMinWidth(40);
        column.setCellFactory(param -> new IceCell(param, cellIndex, SnowTableView.this));
        if (cellIndex == 0 || cellIndex == 1) {
            column.setPrefWidth(240);
        } else {
            column.setPrefWidth(160);
        }
        column.setCellValueFactory(param -> {
            if (cellIndex == -1) {
                return new IntToCellBinding(param.getValue().lineNumber.add(1), param.getValue());
            }
            if (param.getValue() != null) {
                return param.getValue().getCellAsStringProperty(cellIndex, mainForm);
            } else {
                return new ReadOnlyObjectWrapper<>(new Cell("(non-existing line)", "", null));
            }
        });
        return column;
    }

    public void loadLines(HighElement highElement, ObservableList<LogicalLine> lines) {
        // match lines from output.xml
        OutputMatcher.matchLines(highElement, lines);

        boolean containsTemplate = false;

        scenario = highElement;
        // For key-value tables:
        for (LogicalLine line : lines) {
            line.setBelongsToHighElement(highElement);
            line.belongsWhere = snowTableKind;
            line.recalcStyles();

            if (!containsTemplate && line.cells.size() > 1) {
                Cell cell1 = line.cells.get(1);
                containsTemplate = cell1.contents.contains("Template");
            }
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

        if (scenario instanceof Scenario && ((Scenario) scenario).semanticsIsTemplateTestCase) {
            Scenario scenarioTyped = (Scenario) scenario;
            if (scenarioTyped.templateReferenced != null && scenarioTyped.templateReferenced.getLines().size() > 0) {
                // simply put into header what is in line 0 of referenced template
                LogicalLine line0 = scenarioTyped.templateReferenced.getLines().get(0);
                LogicalLine argLine = scenarioTyped.templateReferenced.getLines().stream()
                        .filter(line -> line.cells.get(1).contents.equalsIgnoreCase("[Arguments]")).findAny()
                        .orElse(line0);

                List<TableColumn<LogicalLine, ?>> colsToRename = this.getColumns().subList(1,
                    this.getColumns().size() - 1);
                for (int i = 0; i < colsToRename.size(); i++) {
                    // we need to shift indexes by -2 , expecting [Arguments] modifier first + empty col
                    if (argLine.cells.size() >= i + 3) {
                        TableColumn<LogicalLine, ?> col = colsToRename.get(i);
                        col.setText(argLine.cells.get(i + 2).contents);
                    } else {
                        colsToRename.get(i).setText("");
                    }
                }

            }
        } else {
            // clean the possible template artifacts
            this.getColumns().subList(1, this.getColumns().size() - 1).forEach(col -> col.setText(""));
        }
    }

    private SimpleObjectProperty<Cell> tablePositionToCell(TablePosition position) {
        return this.getItems().get(position.getRow()).getCellAsStringProperty(snowTableKind == SnowTableKind.SCENARIO ? position.getColumn() : position.getColumn() - 1, mainForm);
    }


    public void considerAddingVirtualRowsAndColumns() {
        // Rows
        int virtualRows = 0;
        int lastFullColumn = -1;

        for (int i = getItems().size() - 1; i >= 0; i--) {
            LogicalLine line = getItems().get(i);
            int numberOfColumnsOfThisLine = line.cells.size();
            if (numberOfColumnsOfThisLine > 0) {
                int lastNonemptyThisLine = -1;
                for (int j = line.cells.size() - 1; j > 0; j--) {
                    Cell cell = line.cells.get(j);
                    if (!StringUtils.isEmpty(cell.contents)) {
                        lastNonemptyThisLine = j;
                        break;
                    }
                }
                if (lastNonemptyThisLine > lastFullColumn) {
                    lastFullColumn = lastNonemptyThisLine;
                }
            }
            if (line.isFullyVirtual()) {
                virtualRows++;
            }
        }
        // The last cell that contains a value is at logical place "lastFullColumn"
        // The actual number of columns in the TableView is columnCount.
        // Of these, 1 is the "Row" column, so we ignore that.
        // In scenario, we count from cell 1, so the table is full if there are as many columns as lastFullColumn  + 1 (the column itself) +1 (the first column) -1 (the ignored first cell)
        // In settings, we count from cell 0, so the table is full if there are as many columns as lastFullColumn  + 1 (the column itself) +1 (the first column) )
        int tableFullIfNumberOfColumnsIs = (snowTableKind.isScenario() ? lastFullColumn + 1 : lastFullColumn + 2);
        int missingColumns = 1 + tableFullIfNumberOfColumnsIs - getColumns().size();
        for (int missingId = 1; missingId <= missingColumns; missingId++) {
            // add a column
            if (snowTableKind.isScenario()) {
                getColumns().add(createColumn(getColumns().size()));
            } else {
                getColumns().add(createColumn(getColumns().size() - 1));
            }
        }
        while (virtualRows < 4) {
            getItems().add(createNewLine());
            virtualRows++;
        }
        // Columns
    }

    public void goRight() {
        this.getFocusModel().focusRightCell();
    }

    public HighElement getScenario() {
        return scenario;
    }

    public void selectCell(int rowDiff, int columnDiff) {
        TableSelectionModel sm = getSelectionModel();
        if (sm == null) return;

        TableFocusModel fm = getFocusModel();
        if (fm == null) return;

        TablePositionBase<TableColumn> focusedCell = getFocusModel().getFocusedCell();
        int currentRow = focusedCell.getRow();
        int currentColumn = getVisibleLeafIndex(focusedCell.getTableColumn());

        if (rowDiff < 0 && currentRow <= 0) return;
        else if (rowDiff > 0 && currentRow >= getItems().size() - 1) return;
        else if (columnDiff < 0 && currentColumn <= 0) return;
        else if (columnDiff > 0 && currentColumn >= getVisibleLeafColumns().size() - 1) return;
        else if (columnDiff > 0 && currentColumn == -1) return;

        TableColumn tc = focusedCell.getTableColumn();
        tc = getVisibleLeafColumn(getVisibleLeafIndex(tc) + columnDiff);


        int row = focusedCell.getRow() + rowDiff;
        sm.clearAndSelect(row, tc);
    }

    private Cell getFocusedCell() {
        TablePosition<LogicalLine, Cell> focusedCell = getFocusedTablePosition();
        SimpleObjectProperty<Cell> cellSimpleObjectProperty = tablePositionToCell(focusedCell);
        return cellSimpleObjectProperty.getValue();
    }

    private void applyFontSize() {
        String style = this.getStyle();
        Pattern pattern = Pattern.compile("(.*)(-fx-font-size: .*?pt;)(.*)");
        Matcher matcher = pattern.matcher(style);
        if (matcher.find()) {
            this.setStyle(matcher.group(1) + "-fx-font-size: " + fontSize + "pt;" + matcher.group(3));
        }
        this.setFixedCellSize(cellSize);
    }
}

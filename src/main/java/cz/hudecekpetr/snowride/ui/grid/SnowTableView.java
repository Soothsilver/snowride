package cz.hudecekpetr.snowride.ui.grid;

import com.sun.javafx.scene.control.skin.PrecursorTableViewSkin;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.fx.TableClipboard;
import cz.hudecekpetr.snowride.fx.bindings.IntToCellBinding;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.semantics.findusages.FindUsages;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.undo.AddRowOperation;
import cz.hudecekpetr.snowride.undo.ChangeTextOperation;
import cz.hudecekpetr.snowride.undo.MassOperation;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SnowTableView extends TableView<LogicalLine> {

    public SnowTableKind snowTableKind;
    public boolean triggerAutocompletionNext;
    public HighElement scenario;
    private MainForm mainForm;

    public SnowTableView(MainForm mainForm, SnowTableKind snowTableKind) {
        super();
        this.mainForm = mainForm;
        this.snowTableKind = snowTableKind;
        this.getStyleClass().add("snow");
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
        this.getSelectionModel().getSelectedCells().addListener((ListChangeListener<TablePosition>) this::cellSelectionChanged);
        this.getColumns().add(rowColumn);
        ContextMenu cmenu = new ContextMenu(new MenuItem("Something"));
        this.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                cmenu.getItems().clear();
                if (getSelectionModel().getSelectedItem() != null) {
                    List<MenuItem> contextMenuItems = recreateContextMenu(event);
                    cmenu.getItems().setAll(contextMenuItems);
                } else {
                    event.consume();
                }
            }
        });
        this.setContextMenu(cmenu);
        this.setOnKeyPressed(this::onKeyPressed);
        this.setOnMouseClicked(this::onMouseClicked);
    }

    private List<MenuItem> recreateContextMenu(ContextMenuEvent contextMenuEvent) {
        List<MenuItem> contextMenuItems = new ArrayList<>();


        MenuItem miInsert = new MenuItem("Insert row before this");
        miInsert.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
        miInsert.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                insertRowBefore();
            }
        });
        MenuItem miAppend = new MenuItem("Append row after this");
        miAppend.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        miAppend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                appendRowAfter();
            }
        });

        MenuItem miDelete = new MenuItem("Delete all selected rows");
        miDelete.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        miDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteSelectedRows();
            }
        });
        if (getSelectionModel().getSelectedCells().size() > 0) {
            TablePosition lastPosition = getSelectionModel().getSelectedCells().get(0);
            if (lastPosition.getColumn() != -1 && lastPosition.getRow() != -1) {
                LogicalLine line = getItems().get(lastPosition.getRow());
                Cell thisCell = line.getCellAsStringProperty(tableXToLogicalX(lastPosition.getColumn()), mainForm).getValue();
                IKnownKeyword keywordInThisCell = thisCell.getKeywordInThisCell();
                if (keywordInThisCell != null) {
                    MenuItem miFindUsages = new MenuItem("Find usages");
                    miFindUsages.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            MenuItem[] items = FindUsages.findUsages(keywordInThisCell, keywordInThisCell.getScenarioIfPossible(), mainForm.getRootElement()).toArray(new MenuItem[0]);
                            ContextMenu menuUsages = new ContextMenu(items);
                            menuUsages.show(mainForm.getStage(), contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                        }
                    });
                    contextMenuItems.add(miFindUsages);
                    contextMenuItems.add(new SeparatorMenuItem());
                } else if (thisCell.getSemantics().isKeyword) {
                    MenuItem miCreateThis = new MenuItem("Create this keyword in the same file");
                    miCreateThis.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Suite suite = scenario.parent;
                            Scenario newKeyword = suite.createNewChild(thisCell.contents, false, mainForm);
                            // TODO add arguments and return value rows
                            mainForm.changeOccurredTo(suite, LastChangeKind.STRUCTURE_CHANGED);
                        }
                    });
                    contextMenuItems.add(miCreateThis);
                    contextMenuItems.add(new SeparatorMenuItem());
                }
            }
        }
        if (snowTableKind == SnowTableKind.SCENARIO) {
            MenuItem miComment = new MenuItem("Comment out");
            miComment.setAccelerator(new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN));
            miComment.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    commentOutOrUncomment(false);
                }
            });
            MenuItem miUncomment = new MenuItem("Uncomment");
            miUncomment.setAccelerator(new KeyCodeCombination(KeyCode.SLASH, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
            miUncomment.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    commentOutOrUncomment(true);
                }
            });
            contextMenuItems.add(miComment);
            contextMenuItems.add(miUncomment);
        }

        contextMenuItems.add(miInsert);
        contextMenuItems.add(miAppend);
        contextMenuItems.add(miDelete);
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

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PrecursorTableViewSkin<>(this);
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        MainForm.documentationPopup.hide();
        if (lastPositionSelected != null && lastPositionSelected.getColumn() == 0) {
            mainForm.toast("In Snowride, you must not click the 'Row' column. Right-click the next column instead to get the correct context menu.");
        }
        if (mouseEvent.isControlDown()) {
            Cell cell = null;
            if (snowTableKind.isScenario()) {
                cell = getFocusedCell();
            } else {
                cell = getFocusedCellInSettingsTable();
            }

            if (cell != null) {
                if (cell.leadsToSuite != null) {
                    mainForm.selectProgrammaticallyAndRememberInHistory(cell.leadsToSuite);
                } else {
                    IKnownKeyword keyword = cell.getKeywordInThisCell();
                    if (keyword != null) {
                        Scenario highElement = keyword.getScenarioIfPossible();
                        if (highElement != null) {
                            mainForm.selectProgrammaticallyAndRememberInHistory(highElement);
                        } else {
                            mainForm.toast("Keyword '" + keyword.getAutocompleteText() + "' is not a known user keyword. Cannot go to definition.");
                        }
                    }
                }
            }
        }
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
            insertRowBefore();
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.A && keyEvent.isControlDown()) {
            appendRowAfter();
            keyEvent.consume();
        } else if (keyEvent.getCode() == KeyCode.D && keyEvent.isControlDown()) {
            deleteSelectedRows();
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
            commentOutOrUncomment(keyEvent.isShiftDown());
        } else if ((keyEvent.getCode() == KeyCode.Q && keyEvent.isControlDown()) || keyEvent.getCode() == KeyCode.F1) {
            if (getSelectionModel().getSelectedCells().size() > 0) {
                SimpleObjectProperty<Cell> cell = tablePositionToCell(getSelectionModel().getSelectedCells().get(0));
                Cell copy = cell.getValue().copy();
                copy.triggerDocumentationNext = true;
                cell.set(copy);
                keyEvent.consume();
            }
        } else if (keyEvent.getCode() == KeyCode.TAB) {
            selectCell(0,1);
            keyEvent.consume();
        } else if (!keyEvent.getCode().isArrowKey() && !keyEvent.getCode().isFunctionKey() && !keyEvent.getCode().isModifierKey()
                && !keyEvent.getCode().isNavigationKey() && !keyEvent.getCode().isWhitespaceKey() && !keyEvent.isControlDown()
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
            for (int i =0; i < line.cells.size(); i++) {
                SimpleObjectProperty<Cell> cellAsStringProperty = line.getCellAsStringProperty(i, mainForm);
                Cell current = cellAsStringProperty.get();
                operations.add(new ChangeTextOperation(getItems(), current.contents, "", row, i));
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
        this.getScenario().getUndoStack().iJustDid(new AddRowOperation(getItems(), whatFocused + 1, this::createNewLine));
    }

    private void insertRowBefore() {
        int whatFocused = this.getFocusModel().getFocusedIndex();
        this.getItems().add(whatFocused, createNewLine());
        int column = 1;
        if (this.getSelectionModel().getSelectedCells().size() > 0) {
            column = this.getSelectionModel().getSelectedCells().get(0).getColumn();
        }
        this.getSelectionModel().clearAndSelect(whatFocused, getVisibleLeafColumn(column));
        this.getScenario().getUndoStack().iJustDid(new AddRowOperation(getItems(), whatFocused, this::createNewLine));
    }

    private void commentOutOrUncomment(boolean uncomment) {
        Set<LogicalLine> lines = new HashSet<>();
        for (TablePosition selectedCell : getSelectionModel().getSelectedCells()) {
            int row = selectedCell.getRow();
            lines.add(getItems().get(row));
        }
        for (LogicalLine theLine : lines) {
            Cell firstCell = theLine.getCellAsStringProperty(1, mainForm).getValue();
            if (uncomment) {
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
                operations.add(new ChangeTextOperation(getItems(), replacedCell.getValue().contents, cellSplit[xi].trim(), y, tableXToLogicalX(x)));
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

    public LogicalLine createNewLine() {
        LogicalLine newLine = new LogicalLine();
        newLine.setBelongsToHighElement(scenario);
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
        if (cellIndex == 0 || cellIndex == 1) {
            column.setPrefWidth(240);
        } else {
            column.setPrefWidth(160);
        }
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
            line.setBelongsToHighElement(highElement);
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
        // Rows
        int virtualRows = 0;
        int lastFullColumn = -1;

        for (int i = getItems().size() - 1; i >= 0; i--) {
            LogicalLine line = getItems().get(i);
            int numberOfColumnsOfThisLine = line.cells.size();
            if (numberOfColumnsOfThisLine > 0) {
                int lastNonemptyThisLine = -1;
                for (int j = line.cells.size()-1;j>0;j--) {
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
            System.out.println("Adding column for cell " + (lastFullColumn + missingId));
            // add a column
            if (snowTableKind.isScenario()) {
                getColumns().add(createColumn(getColumns().size()));
            } else {
                getColumns().add(createColumn(getColumns().size()-1));
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
        tc = getVisibleLeafColumn(getVisibleLeafIndex(tc) +  columnDiff);


        int row = focusedCell.getRow() + rowDiff;
        sm.clearAndSelect(row, tc);
    }private Cell getFocusedCell() {
        TablePosition<LogicalLine, Cell> focusedCell = getFocusedTablePosition();
        SimpleObjectProperty<Cell> cellSimpleObjectProperty = tablePositionToCell(focusedCell);
        return cellSimpleObjectProperty.getValue();
    }
}

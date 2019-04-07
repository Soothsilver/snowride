package cz.hudecekpetr.snowride.fx.grid;

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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;


public class SnowTableView extends TableView<LogicalLine> {

    private HighElement scenario;
    private MainForm mainForm;
    public SnowTableKind snowTableKind;
    public boolean triggerAutocompletionNext;

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
        addColumn(-1);
        this.getColumns().get(0).setText("Row");
        this.getColumns().get(0).setPrefWidth(30);
        this.getColumns().get(0).setStyle("-fx-alignment: center;");
        this.setOnKeyPressed(this::onKeyPressed);
        this.setOnMouseClicked(this::onMouseClicked);
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.isControlDown() && snowTableKind.isScenario()) {
            Cell cell = getFocusedCell();
            IKnownKeyword keyword = cell.getKeywordInThisCell();
            if (keyword != null) {
                Scenario highElement = keyword.getScenarioIfPossible();
                if (highElement != null) {
                    mainForm.selectProgrammaticallyAndRememberInHistory(highElement);
                } else {
                    mainForm.toast("Keyword '"  + keyword.getAutocompleteText() + "' is not a known user keyword. Cannot go to definition.");
                }
            }
        }
    }

    private Cell getFocusedCell() {
        TablePosition<LogicalLine, Cell> focusedCell = this.focusModelProperty().get().focusedCellProperty().get();
        SimpleObjectProperty<Cell> cellSimpleObjectProperty = tablePositionToCell(focusedCell);
        return cellSimpleObjectProperty.getValue();
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.I && keyEvent.isControlDown()) {
            // Insert
            int whatFocused = this.getFocusModel().getFocusedIndex();
            this.getItems().add(whatFocused, createNewLine());
        }
        else if (keyEvent.getCode() == KeyCode.A && keyEvent.isControlDown()) {
            // Append
            int whatFocused = this.getFocusModel().getFocusedIndex();
            this.getItems().add(whatFocused + 1, createNewLine());
            keyEvent.consume();
        }
        else if (keyEvent.getCode() == KeyCode.SPACE && keyEvent.isControlDown()) {
            TablePosition<LogicalLine, ?> focusedCell = this.focusModelProperty().get().focusedCellProperty().get();
            this.triggerAutocompletionNext = true;
            this.edit(focusedCell.getRow(), focusedCell.getTableColumn());
            this.triggerAutocompletionNext = false;
            keyEvent.consume();
        }
        else if (keyEvent.getCode() == KeyCode.BACK_SPACE || keyEvent.getCode() == KeyCode.DELETE) {
            this.getSelectionModel().getSelectedCells().forEach(tablePosition -> {
                SimpleObjectProperty<Cell> cell = tablePositionToCell(tablePosition);
                cell.set(new Cell("", cell.getValue().postTrivia, cell.getValue().partOfLine));
            });
            keyEvent.consume();
        }
        else if (keyEvent.getCode() == KeyCode.C && keyEvent.isControlDown()) {
            SimpleObjectProperty<Cell> cell = tablePositionToCell(getSelectionModel().getSelectedCells().get(0));
            TableClipboard.store(cell.getValue().contents);
            keyEvent.consume();
        }
        else if (keyEvent.getCode() == KeyCode.X && keyEvent.isControlDown()) {
            SimpleObjectProperty<Cell> cell = tablePositionToCell(getSelectionModel().getSelectedCells().get(0));
            TableClipboard.store(cell.getValue().contents);
            cell.set(new Cell("", cell.getValue().postTrivia, cell.getValue().partOfLine));
            keyEvent.consume();
        }
        else if (keyEvent.getCode() == KeyCode.V && keyEvent.isControlDown()) {
            SimpleObjectProperty<Cell> cell = tablePositionToCell(getSelectionModel().getSelectedCells().get(0));
            cell.set(new Cell(Clipboard.getSystemClipboard().getString(), cell.getValue().postTrivia, cell.getValue().partOfLine));
            keyEvent.consume();
        }


        else if (keyEvent.getCode() == KeyCode.TAB) {
            this.getSelectionModel().clearSelection();
            this.getSelectionModel().selectNext();
            keyEvent.consume();
        }
        else if (!keyEvent.getCode().isArrowKey() && !keyEvent.getCode().isFunctionKey() && !keyEvent.getCode().isModifierKey()
                && !keyEvent.getCode().isNavigationKey() && !keyEvent.getCode().isWhitespaceKey() && !keyEvent.isControlDown()) {
            TablePosition<LogicalLine, ?> focusedCell = this.focusModelProperty().get().focusedCellProperty().get();
            this.edit(focusedCell.getRow(), focusedCell.getTableColumn());
            keyEvent.consume();
        }
    }

    private LogicalLine createNewLine() {
        LogicalLine newLine = new LogicalLine();
        newLine.belongsToHighElement = scenario;
        newLine.lineNumber = new PositionInListProperty<>(newLine, this.getItems());
        return newLine;
    }

    private void addColumn(int cellIndex) {
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
        this.getColumns().add(column);
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
    }

    public void loadLines(HighElement highElement, ObservableList<LogicalLine> lines) {
        scenario = highElement;
        for (LogicalLine line : lines) {
            if (line.belongsToHighElement == null) {
                line.belongsToHighElement = highElement;
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
            while (this.getColumns().size() < columnCount) {
                if (snowTableKind.isScenario()) {
                    addColumn(this.getColumns().size()); // start at cell 1, not 0 (0 is blank for test cases and keywords)
                } else {
                    addColumn(this.getColumns().size() - 1);
                }
            }
        }
        this.considerAddingVirtualRowsAndColumns();
    }

    public SimpleObjectProperty<Cell> tablePositionToCell(TablePosition<LogicalLine, Cell> position) {
        return (SimpleObjectProperty<Cell>)this.getItems().get(position.getRow()).getCellAsStringProperty(position.getColumn(), mainForm);
    }

    public void considerAddingVirtualRowsAndColumns() {
        int virtualRows = 0;
        for (int i = getItems().size() - 1; i >= 0; i--) {
            LogicalLine line = getItems().get(i);
            if (line.isFullyVirtual()) {
                virtualRows++;
            }
            if (virtualRows >= 1) {
                // That's enough. That's fine. We don't need more.
                return;
            }
        }
        if (virtualRows < 1) {
            getItems().add(createNewLine());
        }
    }
}

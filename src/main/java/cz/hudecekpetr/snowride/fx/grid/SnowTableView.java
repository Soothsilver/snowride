package cz.hudecekpetr.snowride.fx.grid;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.bindings.IntToCellBinding;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;


public class SnowTableView extends TableView<LogicalLine> {

    private HighElement scenario;
    private MainForm mainForm;

    public SnowTableView(MainForm mainForm) {
        super();
        this.mainForm = mainForm;
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
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.I && keyEvent.isControlDown()) {
            // Insert
            int whatFocused = this.getFocusModel().getFocusedIndex();
            LogicalLine newLine = new LogicalLine();
            newLine.belongsToScenario = scenario;
            newLine.lineNumber = new PositionInListProperty<>(newLine, this.getItems());
            this.getItems().add(whatFocused, newLine);
        }
        else if (keyEvent.getCode() == KeyCode.A && keyEvent.isControlDown()) {
            // Append
            int whatFocused = this.getFocusModel().getFocusedIndex();
            LogicalLine newLine = new LogicalLine();
            newLine.belongsToScenario = scenario;
            newLine.lineNumber = new PositionInListProperty<>(newLine, this.getItems());
            this.getItems().add(whatFocused + 1, newLine);
            keyEvent.consume();
        }
        else if (keyEvent.getCode() == KeyCode.TAB) {
            this.getSelectionModel().clearSelection();
            this.getSelectionModel().selectNext();
            keyEvent.consume();
        }
        else if ((keyEvent.getCode().isLetterKey() || keyEvent.getCode().isDigitKey()) && !keyEvent.isControlDown()) {
            TablePosition<LogicalLine, ?> focusedCell = this.focusModelProperty().get().focusedCellProperty().get();
            this.edit(focusedCell.getRow(), focusedCell.getTableColumn());
            keyEvent.consume();
        }
    }

    private void addColumn(int cellIndex) {
        TableColumn<LogicalLine, Cell> column = new TableColumn<>();
        column.setSortable(false);
        column.setMinWidth(40);
        column.setCellFactory(new Callback<TableColumn<LogicalLine, Cell>, TableCell<LogicalLine, Cell>>() {
            @Override
            public TableCell<LogicalLine, Cell> call(TableColumn<LogicalLine, Cell> param) {
                return new IceCell(param, cellIndex);
            }
        });
        column.setPrefWidth(200);
        this.getColumns().add(column);
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<LogicalLine, Cell>, ObservableValue<Cell>>() {
            @Override
            public ObservableValue<Cell> call(TableColumn.CellDataFeatures<LogicalLine, Cell> param) {
                if (cellIndex == -1) {
                    return new IntToCellBinding(param.getValue().lineNumber);
                }
                if (param.getValue() != null) {
                    return param.getValue().getCellAsStringProperty(cellIndex, mainForm);
                } else {
                    return new ReadOnlyObjectWrapper<>(new Cell("(non-existing line)", "", null));
                }
            }
        });
    }

    public void loadLines(ObservableList<LogicalLine> lines) {
        // Renew data
        this.setItems(lines);
        // Column count
        int maxCellCount = lines.size() == 0 ? -1 : Extensions.max(lines, (LogicalLine line) -> line.cells.size()) - 1; // -1 for the first blank cell
        int columnCount = Math.max(maxCellCount + 1, 4) + 1; // +1 for "number of row"
        if (this.getColumns().size() > columnCount) {
            this.getColumns().remove(columnCount, this.getColumns().size());
        } else {
            while (this.getColumns().size() < columnCount) {
                addColumn(this.getColumns().size()); // start at cell 1, not 0 (0 is blank for test cases and keywords)
            }
        }
    }

    public void setScenario(HighElement scenario) {

        this.scenario = scenario;
    }
}

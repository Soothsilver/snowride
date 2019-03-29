package cz.hudecekpetr.snowride.fx.grid;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.List;


public class SnowTableView extends TableView<LogicalLine> {

    public SnowTableView() {
        super();
        this.setEditable(true);
        this.getSelectionModel().setCellSelectionEnabled(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
    }

    private void addColumn(int cellIndex) {
        TableColumn<LogicalLine, Cell> column = new TableColumn<>();
        column.setSortable(false);
        column.setMinWidth(40);
        column.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Cell>() {
                    @Override
                    public String toString(Cell object) {
                        return object.contents;
                    }

                    @Override
                    public Cell fromString(String string) {
                        return new Cell(string, "    ");
                    }
                }));
        column.setPrefWidth(200);
        this.getColumns().add(column);
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<LogicalLine, Cell>, ObservableValue<Cell>>() {
            @Override
            public ObservableValue<Cell> call(TableColumn.CellDataFeatures<LogicalLine, Cell> param) {
                if (cellIndex == -1) {
                    return new ReadOnlyObjectWrapper<>(new Cell(Integer.toString(param.getValue().lineNumber.get()), ""));
                }
                if (param.getValue() != null) {
                    if (param.getValue().cells.size() > cellIndex) {
                        return new ReadOnlyObjectWrapper<>(param.getValue().cells.get(cellIndex));
                    }
                }
                return new ReadOnlyObjectWrapper<>(new Cell("", ""));
            }
        });
    }

    public void loadLines(List<LogicalLine> lines) {
        // Clear data
        this.getItems().clear();
        // Column count
        int maxCellCount = Extensions.max(lines, (LogicalLine line) -> line.cells.size()) - 1; // -1 for the first blank cell
        int columnCount = Math.max(maxCellCount + 1 , 4) + 1; // +1 for "number of row"
        if (this.getColumns().size() > columnCount) {
            this.getColumns().remove(columnCount, this.getColumns().size());
        } else {
            while (this.getColumns().size() < columnCount) {
                addColumn(this.getColumns().size()); // start at cell 1, not 0 (0 is blank for test cases and keywords)
            }
        }
        // Add data
        this.getItems().setAll(lines);
    }
}

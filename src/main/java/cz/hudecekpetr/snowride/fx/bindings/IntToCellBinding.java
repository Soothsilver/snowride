package cz.hudecekpetr.snowride.fx.bindings;

import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;

/**
 * An observable value that creates a table view cell value from a number. Used to create cell values for the first
 * "Row" columns of tables.
 */
public class IntToCellBinding extends ObjectBinding<Cell> {
    private ObservableValue<Number> integer;
    private LogicalLine line;

    public IntToCellBinding(ObservableValue<Number> integer, LogicalLine line) {
        this.integer = integer;
        this.line = line;
        this.bind(integer);
    }

    @Override
    protected Cell computeValue() {
        Cell lineNumberCell = new Cell(integer.getValue().toString(), "irrelevant", line, true);
        line.lineNumberCell = lineNumberCell;
        return lineNumberCell;
    }
}

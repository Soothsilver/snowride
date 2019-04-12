package cz.hudecekpetr.snowride.fx.bindings;

import cz.hudecekpetr.snowride.lexer.Cell;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;

public class IntToCellBinding extends ObjectBinding<Cell> {
    private ObservableValue<Number> integer;

    public IntToCellBinding(ObservableValue<Number> integer) {
        this.integer = integer;
        this.bind(integer);
    }

    @Override
    protected Cell computeValue() {
        Cell lineNumberCell = new Cell(integer.getValue().toString(), "irrelevant", null);
        lineNumberCell.isLineNumberCell = true;
        return lineNumberCell;
    }
}

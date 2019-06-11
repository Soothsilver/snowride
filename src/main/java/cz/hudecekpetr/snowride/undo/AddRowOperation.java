package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import javafx.collections.ObservableList;

import java.util.function.Supplier;

public class AddRowOperation extends UndoableOperation {
    private final int rowIndex;
    private final Supplier<LogicalLine> lineCreator;

    public AddRowOperation(ObservableList<LogicalLine> allLines, int rowIndex, Supplier<LogicalLine> lineCreator) {
        super(allLines);
        this.rowIndex = rowIndex;
        this.lineCreator = lineCreator;
    }

    @Override
    public void redo() {
        allLines.add(rowIndex, lineCreator.get());
    }

    @Override
    public void undo() {
        allLines.remove(rowIndex);
    }

    @Override
    public String toString() {
        return "Add a row at index " + rowIndex;
    }
}

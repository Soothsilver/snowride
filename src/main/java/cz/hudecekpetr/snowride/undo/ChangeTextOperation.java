package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.ObservableList;

public class ChangeTextOperation extends UndoableOperation {
    private final String oldData;
    private final String newData;
    private final int lineNumber;
    private final int columnIndex;

    public ChangeTextOperation(ObservableList<LogicalLine> allLines, String oldData, String newData, int lineNumber, int columnIndex) {
        super(allLines);
        this.oldData = oldData;
        this.newData = newData;
        this.lineNumber = lineNumber;
        this.columnIndex = columnIndex;
    }

    @Override
    public String toString() {
        return "Change '" + oldData + "' to '" + newData + "' at " + lineNumber + ":" + columnIndex;
    }

    @Override
    public void undo() {
        allLines.get(lineNumber).getCellAsStringProperty(columnIndex, MainForm.INSTANCE).set(new Cell(oldData,"    ", allLines.get(lineNumber)));
    }

    @Override
    public void redo() {
        allLines.get(lineNumber).getCellAsStringProperty(columnIndex, MainForm.INSTANCE).set(new Cell(newData,"    ", allLines.get(lineNumber)));
    }
}

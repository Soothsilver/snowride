package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.ObservableList;

/**
 * The user changed the contents of one cell.
 */
public class ChangeTextOperation extends UndoableOperation {
    private final String oldData;
    private final String newData;
    private final String postTrivia;
    private final int lineNumber;
    private final int columnIndex;

    public ChangeTextOperation(ObservableList<LogicalLine> allLines, String oldData, String newData, String  postTrivia, int lineNumber, int columnIndex) {
        super(allLines);
        this.oldData = oldData;
        this.newData = newData;
        this.lineNumber = lineNumber;
        this.columnIndex = columnIndex;
        this.postTrivia = postTrivia;
    }

    public boolean ignore() {
        return lineNumber < 0 || columnIndex < 0 || oldData.equals(newData);
    }

    @Override
    public void redo() {
        allLines.get(lineNumber).getCellAsStringProperty(columnIndex, MainForm.INSTANCE).set(new Cell(newData, postTrivia, allLines.get(lineNumber)));
    }

    @Override
    public void undo() {
        allLines.get(lineNumber).getCellAsStringProperty(columnIndex, MainForm.INSTANCE).set(new Cell(oldData, postTrivia, allLines.get(lineNumber)));
    }

    @Override
    public void updateHighElement(HighElement highElement) {
        // nothing to do
    }

    @Override
    public String toString() {
        return "Change '" + oldData + "' to '" + newData + "' at " + lineNumber + ":" + columnIndex;
    }
}

package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.tree.LogicalLine;
import javafx.collections.ObservableList;

import java.util.Map;

/**
 * Rows were removed on "reformat" after user saved changes Ctrl+S or pressed the 'Reformat' button.
 */
public class RemoveRowsOperation extends UndoableOperation {
    private final Map<Integer, LogicalLine> indexes;

    public RemoveRowsOperation(ObservableList<LogicalLine> allLines, Map<Integer, LogicalLine> indexes) {
        super(allLines);
        this.indexes = indexes;
    }

    @Override
    public void redo() {
        allLines.removeAll(indexes.values());
    }

    @Override
    public void undo() {
        indexes.forEach(allLines::add);
    }

    @Override
    public String toString() {
        return "Remove rows on reformat.";
    }
}

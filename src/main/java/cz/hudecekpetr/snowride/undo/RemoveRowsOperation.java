package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        indexes.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList()).forEach(index -> allLines.remove(index.intValue()));
    }

    @Override
    public void undo() {
        indexes.forEach(allLines::add);
    }

    @Override
    public void updateHighElement(HighElement highElement) {
        indexes.values().forEach(logicalLine -> logicalLine.setBelongsToHighElement(highElement));
    }

    @Override
    public String toString() {
        return "Remove rows on reformat.";
    }
}

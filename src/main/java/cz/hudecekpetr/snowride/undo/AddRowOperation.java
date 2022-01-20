package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.ui.grid.SnowTableKind;
import javafx.collections.ObservableList;

import java.util.function.Supplier;

/**
 * New line was added.
 *  - by user (using Ctrl+I or Ctrl+A)
 *  - automatically when adding documentation using documentation text area
 */
public class AddRowOperation extends UndoableOperation {
    private final int rowIndex;
    private HighElement element;

    public AddRowOperation(ObservableList<LogicalLine> allLines, int rowIndex, HighElement element) {
        super(allLines);
        this.rowIndex = rowIndex;
        this.element = element;
    }

    @Override
    public void redo() {
        allLines.add(rowIndex, LogicalLine.createEmptyLine((element instanceof Scenario ? SnowTableKind.SCENARIO : SnowTableKind.SETTINGS), element, allLines));
    }

    @Override
    public void undo() {
        allLines.remove(rowIndex);
    }

    @Override
    public void updateHighElement(HighElement highElement) {
        element = highElement;
    }

    @Override
    public String toString() {
        return "Add a row at index " + rowIndex;
    }
}

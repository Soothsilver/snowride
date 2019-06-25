package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.tree.LogicalLine;
import javafx.collections.ObservableList;

/**
 *
 */
public abstract class UndoableOperation {
    /**
     * This operation will affect these lines. They may be part of a settings table, a variables table or a scenario.
     */
    protected final ObservableList<LogicalLine> allLines;

    public UndoableOperation(ObservableList<LogicalLine> allLines) {

        this.allLines = allLines;
    }

    /**
     * Reperform the action. You can assume that the high element is in the same state as it was before the user
     * performed the action for the first time.
     */
    public abstract void redo();
    /**
     * Undo the action. You can assume that the high element is in the same state as it was just after the user
     * performed the action for the first time.
     */
    public abstract void undo();
}

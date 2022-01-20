package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.tree.highelements.HighElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The user changes many cells at the same time, using Ctrl+X, Ctrl+C, Ctrl+V or Ctrl+D, for example.
 */
public class MassOperation extends UndoableOperation {
    private final List<? extends UndoableOperation> operations;
    private final List<? extends UndoableOperation> operationsReversed;

    public MassOperation(List<? extends UndoableOperation> operations) {
        super(operations.get(0).allLines);
        this.operations = operations;
        this.operationsReversed = new ArrayList<>(operations);
        Collections.reverse(operationsReversed);
    }

    @Override
    public void redo() {
        operations.forEach(UndoableOperation::redo);
    }

    @Override
    public void undo() {
        operationsReversed.forEach(UndoableOperation::undo);
    }

    @Override
    public void updateHighElement(HighElement highElement) {
        operations.forEach(o -> o.updateHighElement(highElement));
    }

    @Override
    public String toString() {
        return "Edit " + operations.size() + " cells";
    }
}

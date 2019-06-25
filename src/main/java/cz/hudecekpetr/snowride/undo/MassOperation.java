package cz.hudecekpetr.snowride.undo;

import java.util.List;

/**
 * The user changes many cells at the same time, using Ctrl+X, Ctrl+C, Ctrl+V or Ctrl+D, for example.
 */
public class MassOperation extends UndoableOperation {
    private final List<ChangeTextOperation> operations;

    public MassOperation(List<ChangeTextOperation> operations) {
        super(operations.get(0).allLines);
        this.operations = operations;
    }

    @Override
    public void redo() {
        operations.forEach(ChangeTextOperation::redo);
    }

    @Override
    public void undo() {
        operations.forEach(ChangeTextOperation::undo);
    }

    @Override
    public String toString() {
        return "Edit " + operations.size() + " cells";
    }
}

package cz.hudecekpetr.snowride.undo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UndoStack {

    private List<UndoableOperation> theStack = new ArrayList<>();
    private int iAmBeforeOperation = 0;


    private String getTheStack() {
        return String.join(" > ", theStack.stream().map(UndoableOperation::toString).collect(Collectors.toList()));
    }

    private void updatePossibilities() {
        canUndo.set(iAmBeforeOperation > 0);
        canRedo.set(iAmBeforeOperation < theStack.size());
        System.out.println(toString() + ": " + getTheStack());
    }

    public BooleanProperty canUndo = new SimpleBooleanProperty();
    public BooleanProperty canRedo = new SimpleBooleanProperty();

    public void clear() {
        theStack.clear();
        iAmBeforeOperation = 0;
        updatePossibilities();
    }

    public void iJustDid(UndoableOperation operation) {
        // Remove elements until you remove everything over our position
        while (iAmBeforeOperation < theStack.size()) {
            theStack.remove(theStack.size() - 1);
        }
        theStack.add(operation);
        iAmBeforeOperation++;
        updatePossibilities();
    }
    public void undoIfAble() {
        if (canUndo.getValue()) {
            iAmBeforeOperation--;
            updatePossibilities();
            UndoableOperation toUndo = theStack.get(iAmBeforeOperation);
            toUndo.undo();
        }

    }
    public void redoIfAble() {
        if (canRedo.getValue()) {
            UndoableOperation toRedo = theStack.get(iAmBeforeOperation);
            iAmBeforeOperation++;
            updatePossibilities();
            toRedo.redo();
        }

    }
}

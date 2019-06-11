package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import javafx.collections.ObservableList;

public class UndoableOperation {
    protected final ObservableList<LogicalLine> allLines;

    public UndoableOperation(ObservableList<LogicalLine> allLines) {

        this.allLines = allLines;
    }

    public void redo() {

    }

    public void undo() {

    }
}

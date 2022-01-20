package cz.hudecekpetr.snowride.undo;

import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

/**
 * File was reparsed after changes in 'Text edit'.
 */
public class ReparseOperation extends UndoableOperation {
    private List<LogicalLine> oldData;
    private List<LogicalLine> newData;

    public ReparseOperation(ObservableList<LogicalLine> allLines, List<LogicalLine> oldData, List<LogicalLine> newData) {
        super(allLines);
        this.oldData = oldData;
        this.newData = newData;
    }

    @Override
    public void redo() {
        // we need to update old data as all undo/redo operations were done on 'allLines'
        oldData = new ArrayList<>(allLines);
        allLines.clear();
        allLines.addAll(newData);
        allLines.stream().findFirst().ifPresent(logicalLine -> {
            logicalLine.getBelongsToHighElement().markAsStructurallyChanged(MainForm.INSTANCE);
        });
    }

    @Override
    public void undo() {
        allLines.clear();
        allLines.addAll(oldData);
        allLines.stream().findFirst().ifPresent(logicalLine -> {
            logicalLine.getBelongsToHighElement().markAsStructurallyChanged(MainForm.INSTANCE);
        });
    }

    @Override
    public void updateHighElement(HighElement highElement) {
        oldData.forEach(logicalLine -> logicalLine.setBelongsToHighElement(highElement));
        newData.forEach(logicalLine -> logicalLine.setBelongsToHighElement(highElement));
    }

    @Override
    public String toString() {
        return "REPARSE.";
    }
}

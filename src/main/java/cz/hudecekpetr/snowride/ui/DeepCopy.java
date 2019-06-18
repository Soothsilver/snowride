package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.tree.Scenario;

import java.util.ArrayList;
import java.util.List;

public class DeepCopy {
    public static void copyOldIntoNew(Scenario oldScenario, Scenario newScenario) {
        List<LogicalLine> copiedLines = new ArrayList<>();
        for (LogicalLine originalLine : oldScenario.getLines()) {
            copiedLines.add(copyLineInto(originalLine));
        }
        newScenario.getLines().setAll(copiedLines);
        for (int i = 0; i < copiedLines.size(); i++) {
            copiedLines.get(i).lineNumber = new PositionInListProperty<>(copiedLines.get(i), newScenario.getLines());
            copiedLines.get(i).setBelongsToHighElement(newScenario);
        }
    }

    private static LogicalLine copyLineInto(LogicalLine originalLine) {
        LogicalLine line = new LogicalLine();
        for (Cell cell : originalLine.cells) {
            line.cells.add(new Cell(cell.contents, cell.postTrivia, line));
        }
        return line;
    }
}

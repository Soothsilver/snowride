package cz.hudecekpetr.snowride.fx;

import cz.hudecekpetr.snowride.lexer.Cell;

public class Underlining {
    private static Cell activeCell;
    public static boolean ctrlDown;
    public static void updateCellTo(Cell newCell) {
        Cell updateStyleFor = activeCell;
        activeCell = newCell;
        if (updateStyleFor != null && updateStyleFor.partOfLine != null) {
            updateStyleFor.updateStyle(updateStyleFor.partOfLine.cells.indexOf(updateStyleFor));
        }
        update();
    }
    public static void update() {
        if (activeCell != null && activeCell.partOfLine != null) {
            activeCell.updateStyle(activeCell.partOfLine.cells.indexOf(activeCell));
        }
    }

    public static Cell getActiveCell() {
        return activeCell;
    }
}

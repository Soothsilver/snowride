package cz.hudecekpetr.snowride.fx;

import cz.hudecekpetr.snowride.tree.Cell;

public class Underlining {
    private static Cell activeCell;
    public static boolean ctrlDown;
    public static void updateCellTo(Cell newCell) {
        Cell updateStyleFor = activeCell;
        activeCell = newCell;
        if (updateStyleFor != null && updateStyleFor.partOfLine != null) {
            updateStyleFor.partOfLine.recalcStyles();
        }
        update();
    }
    public static void update() {
        if (activeCell != null && activeCell.partOfLine != null) {
            activeCell.partOfLine.recalcStyles();
        }
    }

    public static Cell getActiveCell() {
        return activeCell;
    }
}

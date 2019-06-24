package cz.hudecekpetr.snowride.fx;

import cz.hudecekpetr.snowride.tree.Cell;

/**
 * Makes cells "underlined" if you hover with CTRL pressed down over them and it's possible to go-to-definition
 * on that cell.
 */
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

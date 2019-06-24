package cz.hudecekpetr.snowride.tree.sections;

import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;

public class TestCaseName {
    private Cell cell;
    private LogicalLine restOfRow;

    public TestCaseName(Cell cell, LogicalLine restOfRow) {
        this.cell = cell;
        this.restOfRow = restOfRow;
    }

    public Cell getCell() {
        return cell;
    }

    public LogicalLine getRestOfRow() {
        return restOfRow;
    }
}

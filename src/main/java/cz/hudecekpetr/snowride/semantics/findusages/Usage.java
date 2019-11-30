package cz.hudecekpetr.snowride.semantics.findusages;

import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;

public class Usage {
    private final String text;
    private final LogicalLine usageLine;
    private final int usageCell;
    private final HighElement element;

    public String getText() {
        return text;
    }

    public HighElement getElement() {
        return element;
    }

    public Usage(String text, LogicalLine usageLine, int usageCell, HighElement element) {

        this.text = text;
        this.usageLine = usageLine;
        this.usageCell = usageCell;
        this.element = element;
    }

    public LogicalLine getUsageLine() {
        return usageLine;
    }

    public int getUsageCell() {
        return usageCell;
    }
}

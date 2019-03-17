package cz.hudecekpetr.snowride.lexer;

import java.util.ArrayList;
import java.util.List;

public class LogicalLine {
    public List<Cell> cells = new ArrayList<>();

    public boolean isStartOfSection() {
        return cells.get(0).contents.startsWith("*");
    }
}

package cz.hudecekpetr.snowride.lexer;

import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;

public class LogicalLine {
    public String preTrivia = "";
    public List<Cell> cells = new ArrayList<>();
    public SimpleIntegerProperty lineNumber = new SimpleIntegerProperty(0);

    public boolean isStartOfSection() {
        return cells.get(0).contents.startsWith("*");
    }

    public LogicalLine prepend(String cellspace, String cell) {
        cells.add(0, new Cell("", cellspace));
        cells.add(1, new Cell(cell, this.preTrivia));
        this.preTrivia = "";
        return this;
    }
    public LogicalLine prepend(String cell) {
        cells.add(0, new Cell(cell, this.preTrivia));
        this.preTrivia = "";
        return this;
    }

    public void serializeInto(StringBuilder sb) {
        cells.forEach(cell -> {
            sb.append(cell.contents);
            sb.append(cell.postTrivia);
        });
        sb.append("\n");
    }
}

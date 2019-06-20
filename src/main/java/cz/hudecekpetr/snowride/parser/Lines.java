package cz.hudecekpetr.snowride.parser;

import cz.hudecekpetr.snowride.tree.LogicalLine;

import java.util.List;

public class Lines {
    private List<LogicalLine> lines;

    public Lines(List<LogicalLine> lines) {
        this.lines = lines;
    }

    public List<LogicalLine> getLines() {
        return lines;
    }
}

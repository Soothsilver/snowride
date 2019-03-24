package cz.hudecekpetr.snowride.lexer;

import cz.hudecekpetr.snowride.antlr.RobotParser;

import java.util.List;
import java.util.stream.Stream;

public class Lines {
    private List<LogicalLine> lines;

    public Lines(List<LogicalLine> lines) {
        this.lines = lines;
    }

    public List<LogicalLine> getLines() {
        return lines;
    }
}

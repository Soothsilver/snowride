package cz.hudecekpetr.snowride.parser;

import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String text;
    private int position = 0;
    private LogicalLine constructingLine = null;
    private Cell constructingCell = null;
    public List<LogicalLine> lex(String text) {
        this.text = text;
        this.position = 0;
        List<LogicalLine> lines = new ArrayList<>();
        while (position < text.length()) {
            LogicalLine line = constructLine();
            lines.add(line);
        }
        return lines;
    }

    private LogicalLine constructLine() {
        LogicalLine line = new LogicalLine();
        while(true) {
            Cell cell = constructCell();
            if (cell == null) {
                break;
            }
            line.cells.add(cell);
        }
        return line;
    }

    private Cell constructCell() {
        StringBuilder construction = new StringBuilder();
        StringBuilder trivia = new StringBuilder();
        boolean inTriviaPhase = false;
        boolean lastWasSpace = false;
        while (position < text.length()) {
            char c = text.charAt(position);
            if (c == '\n') {
                if (construction.length() == 0) {
                    // Cell not even started
                    position++;
                    return null;
                } else {
                    // Cell finished
                    break;
                }
            }
            else if (c == ' ')
            {
                if (inTriviaPhase) {
                    trivia.append(' ');
                }
                else if (lastWasSpace) {
                    inTriviaPhase = true;
                    trivia.append("  ");
                }
                else {
                    lastWasSpace = true;
                }
            }
            else if (c == '\t') {
                // Trivia
                if (lastWasSpace) {
                    trivia.append(' ');
                }
                trivia.append(c);
                inTriviaPhase = true;
            }
            else if (c == '\r') {
                // Ignore \r.
            }
            else {
                if (inTriviaPhase) {
                    // Cell finished
                    break;
                }
                if (lastWasSpace) {
                    lastWasSpace = false;
                    construction.append(' ');
                }
                construction.append(c);
            }
            position++;
        }
        if (position == text.length()) {
            return null;
        }
        return new Cell(construction.toString(), trivia.toString());
    }
}

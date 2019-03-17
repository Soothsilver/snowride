package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.parser.SectionHeader;

import java.util.List;

public class TestDataSection {
    private final SectionHeader header;
    private final List<LogicalLine> lines;

    public TestDataSection(SectionHeader header, List<LogicalLine> lines) {

        this.header = header;
        this.lines = lines;
    }
}

package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.parser.SectionHeader;

import java.util.List;

public class TextOnlyTestDataSection extends TestDataSection {
    public TextOnlyTestDataSection(SectionHeader header, List<LogicalLine> lines) {
        super(header, lines);
    }
}

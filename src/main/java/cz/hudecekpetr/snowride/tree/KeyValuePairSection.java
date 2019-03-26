package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.lexer.LogicalLine;

import java.util.ArrayList;
import java.util.List;

public class KeyValuePairSection extends RobotSection {
    private final List<LogicalLine> pairs;

    public KeyValuePairSection(SectionHeader header, List<LogicalLine> pairs) {
        super(header);
        this.pairs = pairs;
    }

    @Override
    public void serializeInto(StringBuilder sb) {
        header.serializeInto(sb);
        for (LogicalLine line : pairs) {
            line.serializeInto(sb);
        }

    }

    @Override
    public List<? extends HighElement> getHighElements() {
        return new ArrayList<>();
    }
}

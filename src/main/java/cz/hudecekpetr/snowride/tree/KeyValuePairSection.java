package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.semantics.Setting;

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

    public List<Setting> createSettings() {
        List<Setting> settings = new ArrayList<>();
        for (LogicalLine line : pairs) {
            if (line.cells.size() >= 2) {
                settings.add(new Setting(line.cells.get(0).contents, line.cells.get(1).contents));
            }
        }
        return settings;
    }
}

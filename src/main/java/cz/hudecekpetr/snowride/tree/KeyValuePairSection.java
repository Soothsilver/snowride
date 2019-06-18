package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.semantics.Setting;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyValuePairSection extends RobotSection {
    public final ObservableList<LogicalLine> pairs;
    public boolean artificiallyCreated = false;

    public KeyValuePairSection(SectionHeader header, List<LogicalLine> pairs) {
        super(header);
        this.pairs = FXCollections.observableArrayList(pairs);
        for (LogicalLine line : pairs) {
            line.lineNumber = new PositionInListProperty<>(line, this.pairs);
        }
    }

    @Override
    public void serializeInto(StringBuilder sb) {
        if (artificiallyCreated) {
            if (pairs.stream().allMatch(LogicalLine::isFullyVirtual)) {
                // Do not serialize meaningless sections.
                return;
            }
        }
        header.serializeInto(sb);
        for (LogicalLine line : pairs) {
            line.serializeInto(sb);
        }

    }

    @Override
    public List<? extends HighElement> getHighElements() {
        return new ArrayList<>();
    }

    @Override
    public void optimizeStructure() {
        Extensions.optimizeLines(pairs);
    }

    @Override
    public void reformat() {
        optimizeStructure();
        for (LogicalLine pair : pairs) {
            pair.reformat(header.sectionKind);
        }
    }

    public List<Setting> createSettings() {
        List<Setting> settings = new ArrayList<>();
        for (LogicalLine line : pairs) {
            if (line.cells.size() >= 1) {
                settings.add(new Setting(line.cells.get(0).contents, line.cells.stream().skip(1).map(cell -> cell.contents).collect(Collectors.toList())));
            }
        }
        return settings;
    }
}

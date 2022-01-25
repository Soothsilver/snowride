package cz.hudecekpetr.snowride.tree.sections;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.semantics.Setting;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.ui.grid.SnowTableKind;
import cz.hudecekpetr.snowride.undo.ReparseOperation;
import cz.hudecekpetr.snowride.undo.UndoStack;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyValuePairSection extends RobotSection {
    private ObservableList<LogicalLine> pairs;
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
        pairs.filtered(line -> !line.isFullyVirtual()).forEach(ll -> ll.serializeInto(sb));
        sb.append("\n");
    }

    @Override
    public List<? extends HighElement> getHighElements() {
        return new ArrayList<>();
    }

    @Override
    public void optimizeStructure(UndoStack undoStack) {
        Extensions.optimizeLines(pairs, undoStack);
    }

    @Override
    public void reformat() {
        optimizeStructure(pairs.stream().map(logicalLine -> logicalLine.getBelongsToHighElement().getUndoStack()).findFirst().orElse( null));
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

    public ObservableList<LogicalLine> getPairs() {
        return pairs;
    }

    public void basedOn(HighElement highElement, KeyValuePairSection previous) {

        List<LogicalLine> currentLines = new ArrayList<>(pairs.filtered(logicalLine -> !logicalLine.isFullyVirtual()));
        List<LogicalLine> previousLines = new ArrayList<>(previous.pairs);

        // match number of virtual rows
        int virtualLines = pairs.filtered(LogicalLine::isFullyVirtual).size();
        int previousVirtualLines = previous.pairs.filtered(LogicalLine::isFullyVirtual).size();
        if (previousVirtualLines - virtualLines > 0) {
            for (int i = 0; i <= previousVirtualLines - virtualLines; i++) {
                SnowTableKind belongsWhere = previous.pairs.stream().findFirst().get().belongsWhere;
                currentLines.add(LogicalLine.createEmptyLine(belongsWhere, highElement, previous.pairs));
            }
        }
        StringBuilder contents = new StringBuilder();
        StringBuilder previousContents = new StringBuilder();
        pairs.filtered(line -> !line.isFullyVirtual()).forEach(ll -> ll.serializeInto(contents));
        previous.pairs.filtered(line -> !line.isFullyVirtual()).forEach(ll -> ll.serializeInto(previousContents));

        pairs = previous.pairs;
        pairs.clear();
        pairs.addAll(currentLines);


        if (!contents.toString().equals(previousContents.toString())) {
            highElement.getUndoStack().iJustDid(new ReparseOperation(pairs, previousLines, currentLines));
        }
    }
}

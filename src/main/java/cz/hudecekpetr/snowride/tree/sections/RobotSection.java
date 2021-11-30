package cz.hudecekpetr.snowride.tree.sections;

import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.undo.UndoStack;

import java.util.List;

public abstract class RobotSection {
    public final SectionHeader header;

    public RobotSection(SectionHeader header)  {
        this.header = header;
    }

    public abstract void serializeInto(StringBuilder sb);

    public abstract List<? extends HighElement> getHighElements();

    public abstract void optimizeStructure(UndoStack undoStack);

    public void removeChildIfAble(Scenario scenario) {
        // Most sections can't have children.
    }

    public abstract void reformat();
}

package cz.hudecekpetr.snowride.tree;

import java.util.Collection;
import java.util.List;

public abstract class RobotSection {
    protected final SectionHeader header;

    public RobotSection(SectionHeader header)  {
        this.header = header;
    }

    public abstract void serializeInto(StringBuilder sb);

    public abstract List<? extends HighElement> getHighElements();

    public abstract void optimizeStructure();
}

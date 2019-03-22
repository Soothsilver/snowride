package cz.hudecekpetr.snowride.tree;

public abstract  class RobotSection {
    private final SectionHeader header;

    public RobotSection(SectionHeader header)  {
        this.header = header;
    }
}

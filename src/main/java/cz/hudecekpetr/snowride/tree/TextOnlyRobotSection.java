package cz.hudecekpetr.snowride.tree;

public class TextOnlyRobotSection extends RobotSection {
    private final String text;

    public TextOnlyRobotSection(SectionHeader header, String text) {
        super(header);
        this.text = text;
    }
}

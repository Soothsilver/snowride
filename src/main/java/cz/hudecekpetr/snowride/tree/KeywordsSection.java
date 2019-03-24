package cz.hudecekpetr.snowride.tree;

import java.util.List;

public class KeywordsSection extends RobotSection {

    private final List<Scenario> keywords;

    public KeywordsSection(SectionHeader header, List<Scenario> keywords) {
        super(header);
        this.keywords = keywords;
    }

    @Override
    public void serializeInto(StringBuilder sb) {
        header.serializeInto(sb);
        keywords.forEach(tc -> {
            tc.serializeInto(sb);
        });
    }

    @Override
    public List<? extends HighElement> getHighElements() {
        return keywords;
    }
}

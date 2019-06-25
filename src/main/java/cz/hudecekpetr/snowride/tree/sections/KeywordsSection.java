package cz.hudecekpetr.snowride.tree.sections;

import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;

import java.util.List;

public class KeywordsSection extends RobotSection implements IScenarioSection {

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

    @Override
    public void optimizeStructure() {
        for (Scenario kw : keywords) {
            kw.optimizeStructure();
        }
    }

    public void addScenario(Scenario scenario) {
        keywords.add(scenario);
    }

    @Override
    public void removeChildIfAble(Scenario scenario) {
        keywords.removeIf(sc -> sc == scenario);
    }

    @Override
    public void reformat() {
        for (Scenario kw : keywords) {
            kw.reformat();
        }
    }

    @Override
    public List<Scenario> getScenarios() {
        return keywords;
    }
}

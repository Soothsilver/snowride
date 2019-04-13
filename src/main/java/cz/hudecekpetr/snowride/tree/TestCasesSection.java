package cz.hudecekpetr.snowride.tree;

import java.util.List;

public class TestCasesSection extends RobotSection {
    private final List<Scenario> testCases;

    public TestCasesSection(SectionHeader header, List<Scenario> testCases) {
        super(header);
        this.testCases = testCases;
    }

    @Override
    public void serializeInto(StringBuilder sb) {
        header.serializeInto(sb);
        testCases.forEach(tc -> {
            tc.serializeInto(sb);
        });
    }

    @Override
    public List<? extends  HighElement> getHighElements() {
        return testCases;
    }

    @Override
    public void optimizeStructure() {
        for (Scenario s : testCases) {
            s.optimizeStructure();
        }
    }

    public void addScenario(Scenario scenario) {
        testCases.add(scenario);
    }
}

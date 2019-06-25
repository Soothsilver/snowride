package cz.hudecekpetr.snowride.tree.sections;

import cz.hudecekpetr.snowride.tree.highelements.Scenario;

import java.util.List;

/**
 * Represents either a {@link TestCasesSection} or a {@link KeywordsSection}.
 */
public interface IScenarioSection {
    /**
     * Gets a modifiable in-order list of scenarios in this section.
     */
    List<Scenario> getScenarios();
}

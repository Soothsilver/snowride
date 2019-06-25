package cz.hudecekpetr.snowride.tree.highelements;

import cz.hudecekpetr.snowride.ui.MainForm;

public interface ISuite {
    /**
     * Creates a new scenario and adds it to this suite.
     *
     * @param name Name of the new scenario.
     * @param asTestCase Is it a test case? If not, it's a new user keyword.
     */
    default Scenario createNewChild(String name, boolean asTestCase, MainForm mainForm) {
        return createNewChild(name, asTestCase, mainForm, null);
    }
    /**
     * Creates a new scenario and adds it to this suite.
     *
     * @param name Name of the new scenario.
     * @param asTestCase Is it a test case? If not, it's a new user keyword.
     * @param justAfter Instead of adding the new scenario as the last scenario of the suite, add it just after this child.
     */
    Scenario createNewChild(String name, boolean asTestCase, MainForm mainForm, HighElement justAfter);
}

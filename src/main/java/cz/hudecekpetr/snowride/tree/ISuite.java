package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.ui.MainForm;

public interface ISuite {
    Scenario createNewChild(String name, boolean asTestCase, MainForm mainForm);
}

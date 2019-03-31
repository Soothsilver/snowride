package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCaseSettingOption implements IAutocompleteOption {

    public static List<TestCaseSettingOption> allOptions = Arrays.asList(
            new TestCaseSettingOption("Documentation"),
            new TestCaseSettingOption("Tags"),
            new TestCaseSettingOption("Arguments"),
            new TestCaseSettingOption("Return"),
            new TestCaseSettingOption("Teardown"),
            new TestCaseSettingOption("Timeout"),
            new TestCaseSettingOption("Template"),
            new TestCaseSettingOption("Setup")
    );
    private String setting;

    public TestCaseSettingOption(String setting) {
        this.setting = setting;
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.brackets;
    }

    @Override
    public String toString() {
        return "[" + setting + "]";
    }
}

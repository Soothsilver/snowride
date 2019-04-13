package cz.hudecekpetr.snowride.semantics.codecompletion;

public class SettingsTableOption extends TestCaseSettingOption {
    private String name;

    public SettingsTableOption(String name, String description) {
        super(name, description);
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

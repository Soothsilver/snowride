package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.ui.grid.SnowTableKind;

public class SettingsTableOption extends TestCaseSettingOption {
    private String name;

    public SettingsTableOption(String name, String description, int mandatoryArguments, int optionalArguments, int indexOfKeywordArgument) {
        super(name, description, mandatoryArguments, optionalArguments, indexOfKeywordArgument);
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean isLegalInContext(int cellIndex, SnowTableKind snowTableKind) {
        return cellIndex == 0 && snowTableKind == SnowTableKind.SETTINGS;
    }
}

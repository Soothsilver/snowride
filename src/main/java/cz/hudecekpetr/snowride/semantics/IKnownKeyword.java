package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.semantics.codecompletion.TestCaseSettingOption;
import cz.hudecekpetr.snowride.ui.grid.SnowTableKind;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;

public interface IKnownKeyword extends IAutocompleteOption {
    Scenario getScenarioIfPossible();
    int getNumberOfMandatoryArguments();
    int getNumberOfOptionalArguments();
    String getInvariantName();
    int getCompletionPriority();
    int getArgumentIndexOfKeywordArgument();

    default boolean isLegalInContext(int cellIndex, SnowTableKind snowTableKind) {
        return cellIndex >= 1;
    }

    /**
     * Returns true if this keyword is a [Template], [Setup] or other kind of {@link TestCaseSettingOption}. These kinds
     * of rows have go-to-definition and highlighting even in template cases.
     */
    default boolean isTestCaseOption() {
        return false;
    }
}

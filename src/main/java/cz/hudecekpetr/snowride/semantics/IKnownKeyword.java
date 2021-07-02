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

    /**
     * Gets the name that the source of this keyword would have in a full name of a keyword, as per the User Guide. For example,
     * for the resouce file "greetings.robot", it would be "greetings". For the library "cz.hudecekpetr.Utils", it would be
     * "cz.hudecekpetr.Utils".
     *
     * This returns an empty string for pseudo-keywords that have no source such as {@link TestCaseSettingOption}s. It
     * can't return null because it's used in a concurrent hash map which doesn't support nulls.
     */
    String getSourceName();

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

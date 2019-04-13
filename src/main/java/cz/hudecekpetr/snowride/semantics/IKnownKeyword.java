package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.tree.Scenario;

public interface IKnownKeyword extends IAutocompleteOption {
    Scenario getScenarioIfPossible();
    int getNumberOfMandatoryArguments();
    int getNumberOfOptionalArguments();
}

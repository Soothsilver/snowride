package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.semantics.externallibraries.ExternalKeyword;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

import java.util.List;

public class UserKeyword implements IKnownKeyword {
    private String name;
    private String documentation;
    private final int numberOfMandatoryArguments;
    private final int numberOfOptionalArguments;
    private Suite owningSuite;
    private Scenario scenario;

    public UserKeyword(String name, String documentation, int mandatoryArguments, int optionalArguments,  Suite owningSuite, Scenario scenario) {
        this.name = name;
        this.documentation = documentation;
        numberOfMandatoryArguments = mandatoryArguments;
        numberOfOptionalArguments = optionalArguments;
        this.owningSuite = owningSuite;
        this.scenario = scenario;
    }

    public static UserKeyword fromScenario(Scenario s) {
        List<String> arguments = s.getSemanticsArguments();
        int optionals = 0;
        int mandatories = 0;
        for (String arg : arguments) {
            if (arg.contains("*") || arg.contains("@") || arg.contains("&")) {
                optionals += ExternalKeyword.VARARGS_MEANS_INFINITE;
            } else if (arg.contains("=")) {
                optionals++;
            } else {
                mandatories++;
            }
        }
        return new UserKeyword(s.getShortName(), s.getDocumentation(), mandatories, optionals, (Suite) s.parent, s);
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.testIcon;
    }

    @Override
    public String getAutocompleteText() {
        return name;
    }

    @Override
    public String getFullDocumentation() {
        return documentation;
    }

    @Override
    public String getItalicsSubheading() {
        return "User keyword (from " + owningSuite.getShortName() + ")";
    }

    @Override
    public Scenario getScenarioIfPossible() {
        return scenario;
    }

    @Override
    public int getNumberOfMandatoryArguments() {
        return numberOfMandatoryArguments;
    }

    @Override
    public int getNumberOfOptionalArguments() {
        return numberOfOptionalArguments;
    }

    @Override
    public String getInvariantName() {
        return Extensions.toInvariant(name);
    }

    @Override
    public int getCompletionPriority() {
        return ExternalKeyword.PRIORITY_USER_KEYWORD + name.length();
    }

    @Override
    public int getArgumentIndexOfKeywordArgument() {
        return -1;
    }
}

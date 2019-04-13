package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

public class UserKeyword implements IKnownKeyword {
    private String name;
    private String documentation;
    private Suite owningSuite;
    private Scenario scenario;

    public UserKeyword(String name, String documentation, Suite owningSuite, Scenario scenario) {
        this.name = name;
        this.documentation = documentation;
        this.owningSuite = owningSuite;
        this.scenario = scenario;
    }

    public static UserKeyword fromScenario(Scenario s) {
        return new UserKeyword(s.getShortName(), s.getDocumentation(), (Suite) s.parent, s);
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
        return 0;
    }

    @Override
    public int getNumberOfOptionalArguments() {
        return 1000; // not yet implemented
    }
}

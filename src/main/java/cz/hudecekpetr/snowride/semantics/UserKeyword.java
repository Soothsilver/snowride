package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

public class UserKeyword implements IKnownKeyword {
    private String name;
    private Suite owningSuite;

    public UserKeyword(String name, Suite owningSuite) {
        this.name = name;
        this.owningSuite = owningSuite;
    }

    public static UserKeyword fromScenario(Scenario s) {
        return new UserKeyword(s.shortName, (Suite) s.parent);
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
        return "Documentation not yet provided";
    }

    @Override
    public String getItalicsSubheading() {
        return "User keyword (from " + owningSuite.shortName + ")";
    }
}

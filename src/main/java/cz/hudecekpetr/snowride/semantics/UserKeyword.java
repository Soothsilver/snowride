package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

public class UserKeyword implements IKnownKeyword {
    private String name;
    private String documentation;
    private Suite owningSuite;

    public UserKeyword(String name, String documentation, Suite owningSuite) {
        this.name = name;
        this.documentation = documentation;
        this.owningSuite = owningSuite;
    }

    public static UserKeyword fromScenario(Scenario s) {
        return new UserKeyword(s.shortName, s.getDocumentation(), (Suite) s.parent);
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
        return "User keyword (from " + owningSuite.shortName + ")";
    }
}

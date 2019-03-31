package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

public class UserKeyword implements IKnownKeyword {
    private String name;

    public UserKeyword(String name) {
        this.name = name;
    }

    public static UserKeyword fromScenario(Scenario s) {
        return new UserKeyword(s.shortName);
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.testIcon;
    }

    @Override
    public String toString() {
        return name;
    }
}

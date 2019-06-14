package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import javafx.scene.image.Image;

public class DummyAutocompleteOption implements IAutocompleteOption {
    @Override
    public String getAutocompleteText() {
        return "";
    }

    @Override
    public Image getAutocompleteIcon() {
        return null;
    }

    @Override
    public String getFullDocumentation() {
        return null;
    }

    @Override
    public String getItalicsSubheading() {
        return null;
    }

    @Override
    public boolean hasQuickDocumentation() {
        return false;
    }
}

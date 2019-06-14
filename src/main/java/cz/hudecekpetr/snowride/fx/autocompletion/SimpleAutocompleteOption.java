package cz.hudecekpetr.snowride.fx.autocompletion;

import javafx.scene.image.Image;

public class SimpleAutocompleteOption implements IAutocompleteOption {

    private final String text;
    private final Image icon;

    public SimpleAutocompleteOption(String text, Image icon) {
        this.text = text;
        this.icon = icon;
    }

    @Override
    public String getAutocompleteText() {
        return text;
    }

    @Override
    public boolean hasQuickDocumentation() {
        return false;
    }

    @Override
    public Image getAutocompleteIcon() {
        return icon;
    }

    @Override
    public String getFullDocumentation() {
        return "";
    }

    @Override
    public String getItalicsSubheading() {
        return "";
    }
}

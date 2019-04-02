package cz.hudecekpetr.snowride.fx;

import javafx.scene.image.Image;

public interface IAutocompleteOption {
    Image getAutocompleteIcon();
    String getAutocompleteText();

    String getFullDocumentation();

    String getItalicsSubheading();
}

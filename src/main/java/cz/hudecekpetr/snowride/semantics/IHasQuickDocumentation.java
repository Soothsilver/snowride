package cz.hudecekpetr.snowride.semantics;

import javafx.scene.image.Image;

public interface IHasQuickDocumentation {
    Image getAutocompleteIcon();

    String getQuickDocumentationCaption();

    String getFullDocumentation();

    String getItalicsSubheading();

}

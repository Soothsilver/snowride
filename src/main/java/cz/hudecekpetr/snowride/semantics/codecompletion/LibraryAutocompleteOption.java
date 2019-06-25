package cz.hudecekpetr.snowride.semantics.codecompletion;

import com.google.common.base.Objects;
import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

public class LibraryAutocompleteOption implements IAutocompleteOption {

    private final String libraryName;

    public LibraryAutocompleteOption(String libraryName) {
        this.libraryName = libraryName;
    }

    @Override
    public String getAutocompleteText() {
        return libraryName + ".";
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.library;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryAutocompleteOption that = (LibraryAutocompleteOption) o;
        return Objects.equal(libraryName, that.libraryName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(libraryName);
    }

    @Override
    public boolean thenRetriggerCompletion() {
        return true;
    }
}

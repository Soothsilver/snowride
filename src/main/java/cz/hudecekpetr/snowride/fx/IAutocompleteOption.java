package cz.hudecekpetr.snowride.fx;

public interface IAutocompleteOption extends IHasQuickDocumentation {

    String getAutocompleteText();

    @Override
    default String getQuickDocumentationCaption() {
        return getAutocompleteText();
    }

    default boolean hasQuickDocumentation() { return true; }
}

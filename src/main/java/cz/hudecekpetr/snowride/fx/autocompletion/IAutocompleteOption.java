package cz.hudecekpetr.snowride.fx.autocompletion;

import cz.hudecekpetr.snowride.fx.IHasQuickDocumentation;

public interface IAutocompleteOption extends IHasQuickDocumentation {

    String getAutocompleteText();

    @Override
    default String getQuickDocumentationCaption() {
        return getAutocompleteText();
    }

    default boolean hasQuickDocumentation() { return true; }
}

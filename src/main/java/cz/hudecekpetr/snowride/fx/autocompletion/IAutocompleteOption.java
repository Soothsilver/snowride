package cz.hudecekpetr.snowride.fx.autocompletion;

import cz.hudecekpetr.snowride.semantics.IHasQuickDocumentation;

public interface IAutocompleteOption extends IHasQuickDocumentation {

    String getAutocompleteText();

    @Override
    default String getQuickDocumentationCaption() {
        return getAutocompleteText();
    }

    default boolean hasQuickDocumentation() { return true; }
}

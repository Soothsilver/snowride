package cz.hudecekpetr.snowride.fx.autocompletion;

import cz.hudecekpetr.snowride.semantics.IHasQuickDocumentation;

/**
 * This can be shown in the autocompletion popup.
 */
public interface IAutocompleteOption extends IHasQuickDocumentation {

    /**
     * Gets the text that's displayed in the autocomplete listbox.
     */
    String getAutocompleteText();

    @Override
    default String getQuickDocumentationCaption() {
        return getAutocompleteText();
    }

    /**
     * If true, then the documentation popup should appear next to the autocompletion popup
     * if this item is focused (either it's the first item in the list or the user used arrow keys
     * to select it).
     */
    default boolean hasQuickDocumentation() { return true; }

    /**
     * If true and the user commits this completion option, the code completion window will immediately reappear to
     * allow the user to continue using code completion.
     */
    default boolean thenRetriggerCompletion() { return false;}
}

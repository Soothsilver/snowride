package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import javafx.scene.image.Image;

public class GherkinEnhancedOption implements IAutocompleteOption {
    private final String gherkinPrefix;
    private final IAutocompleteOption delegateTo;

    public GherkinEnhancedOption(String gherkinPrefix, IAutocompleteOption delegateTo) {
        this.gherkinPrefix = gherkinPrefix;
        this.delegateTo = delegateTo;
    }

    @Override
    public String getAutocompleteText() {
        return gherkinPrefix + delegateTo.getAutocompleteText();
    }

    @Override
    public String getQuickDocumentationCaption() {
        return delegateTo.getQuickDocumentationCaption();
    }

    @Override
    public boolean hasQuickDocumentation() {
        return delegateTo.hasQuickDocumentation();
    }

    @Override
    public boolean thenRetriggerCompletion() {
        return delegateTo.thenRetriggerCompletion();
    }

    @Override
    public Image getAutocompleteIcon() {
        return delegateTo.getAutocompleteIcon();
    }

    @Override
    public String getFullDocumentation() {
        return delegateTo.getFullDocumentation();
    }

    @Override
    public String getItalicsSubheading() {
        return delegateTo.getItalicsSubheading();
    }
}

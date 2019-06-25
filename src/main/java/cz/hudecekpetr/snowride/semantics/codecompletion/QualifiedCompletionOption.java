package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import javafx.scene.image.Image;

public class QualifiedCompletionOption implements IAutocompleteOption {
    private final IKnownKeyword kw;

    public QualifiedCompletionOption(IKnownKeyword kw) {
        this.kw = kw;
    }

    @Override
    public String getAutocompleteText() {
        return kw.getSourceName() + "." + kw.getAutocompleteText();
    }

    @Override
    public String getQuickDocumentationCaption() {
        return kw.getQuickDocumentationCaption();
    }

    @Override
    public boolean hasQuickDocumentation() {
        return kw.hasQuickDocumentation();
    }

    @Override
    public Image getAutocompleteIcon() {
        return kw.getAutocompleteIcon();
    }

    @Override
    public String getFullDocumentation() {
        return kw.getFullDocumentation();
    }

    @Override
    public String getItalicsSubheading() {
        return kw.getItalicsSubheading();
    }
}

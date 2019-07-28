package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

public class VariableCompletionOption implements IAutocompleteOption {
    private final String name;
    private final String documentation;

    public VariableCompletionOption(String name, String documentation) {
        this.name = name;
        this.documentation = documentation;
    }

    @Override
    public String getAutocompleteText() {
        return name;
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.dollarIcon;
    }

    @Override
    public String getFullDocumentation() {
        return documentation;
    }

    @Override
    public String getItalicsSubheading() {
        return "Variable";
    }
}

package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.Parameter;
import cz.hudecekpetr.snowride.tree.Scenario;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExternalKeyword implements IKnownKeyword {

    private final String canonicalName;
    private String documentation;
    private List<Parameter> parameters = new ArrayList<>();
    private final ExternalLibrary library;

    public ExternalKeyword(String canonicalName, String documentation, List<Parameter> parameters, ExternalLibrary library) {

        this.canonicalName = canonicalName;
        this.documentation = documentation;
        this.parameters = parameters;
        if (parameters.size() > 0) {
            this.documentation = "*Args:* " + parameters.stream().map(p -> p.text).collect(Collectors.joining(", ")) + "\n" + documentation;
        }
        this.library = library;
    }

    @Override
    public Image getAutocompleteIcon() {
        return library.getIcon();
    }

    @Override
    public String getAutocompleteText() {
        return this.canonicalName;
    }

    @Override
    public String getFullDocumentation() {
        return this.documentation;
    }

    @Override
    public String getItalicsSubheading() {
        return "External keyword (library " + library + ")";
    }

    @Override
    public Scenario getScenarioIfPossible() {
        return null;
    }
}

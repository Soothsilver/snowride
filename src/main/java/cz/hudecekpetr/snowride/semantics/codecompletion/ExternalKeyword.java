package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.Parameter;
import cz.hudecekpetr.snowride.semantics.ParameterKind;
import cz.hudecekpetr.snowride.tree.Scenario;
import javafx.scene.image.Image;

import java.util.List;
import java.util.stream.Collectors;

public class ExternalKeyword implements IKnownKeyword {

    public static final int PRIORITY_USER_KEYWORD = 0; // highest priority
    public static final int PRIORITY_EXTERNAL_LIBRARY = 1;
    public static final int PRIORITY_PACKED_IN = 2;
    public static final int VARARGS_MEANS_INFINITE = 1000;
    private final String canonicalName;
    private final ExternalLibrary library;
    private String documentation;
    private int numberOfMandatoryArguments = 0;
    private int numberOfOptionalArguments = 0;

    public ExternalKeyword(String canonicalName, String documentation, List<Parameter> parameters, ExternalLibrary library) {

        this.canonicalName = canonicalName;
        this.documentation = documentation;
        this.numberOfMandatoryArguments = (int) (parameters.stream().filter(p -> p.kind == ParameterKind.STANDARD)).count();
        this.numberOfOptionalArguments = (int) ((parameters.stream().filter(p -> p.kind == ParameterKind.NAMED)).count() + (parameters.stream().filter(p -> p.kind == ParameterKind.VARARGS)).count() * VARARGS_MEANS_INFINITE);
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
        String keywordKind = "External keyword";
        switch (library.getKind()) {
            case PACKED_IN:
                keywordKind = "Robot built-in library";
                break;
            case XML:
                keywordKind = "Keyword imported from XML";
                break;
            case PYTHON:
                keywordKind = "Python keyword";
                break;
        }
        return keywordKind + " (library " + library + ")";
    }

    @Override
    public Scenario getScenarioIfPossible() {
        return null;
    }

    @Override
    public int getNumberOfMandatoryArguments() {
        return numberOfMandatoryArguments;
    }

    @Override
    public int getNumberOfOptionalArguments() {
        return numberOfOptionalArguments;
    }

    @Override
    public String getInvariantName() {
        return Extensions.toInvariant(canonicalName);
    }

    @Override
    public int getCompletionPriority() {
        switch (library.getKind()) {
            case XML:
            case PYTHON:
                return PRIORITY_EXTERNAL_LIBRARY;
            case PACKED_IN:
                return PRIORITY_PACKED_IN;
            default:
                return 100; // unknown
        }
    }
}

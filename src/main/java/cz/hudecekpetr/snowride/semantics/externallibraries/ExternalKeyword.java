package cz.hudecekpetr.snowride.semantics.externallibraries;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.Parameter;
import cz.hudecekpetr.snowride.semantics.ParameterKind;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExternalKeyword implements IKnownKeyword {

    public static final int PRIORITY_TEST_OPTION = -1; // highest priority
    public static final int PRIORITY_USER_KEYWORD = 0;
    public static final int PRIORITY_EXTERNAL_LIBRARY = 1;
    public static final int PRIORITY_PACKED_IN = 2;
    public static final int VARARGS_MEANS_INFINITE = 1000;
    private final String canonicalName;
    private final ExternalLibrary library;
    private String documentation;
    private int numberOfMandatoryArguments;
    private int numberOfOptionalArguments;
    private int indexOfNextKeyword;

    public ExternalKeyword(String canonicalName, String documentation, List<Parameter> parameters, ExternalLibrary library) {

        this.canonicalName = canonicalName;
        this.documentation = documentation;
        this.numberOfMandatoryArguments = (int) (parameters.stream().filter(p -> p.kind == ParameterKind.STANDARD)).count();
        this.numberOfOptionalArguments = (int) ((parameters.stream().filter(p -> p.kind == ParameterKind.NAMED)).count() + (parameters.stream().filter(p -> p.kind == ParameterKind.VARARGS)).count() * VARARGS_MEANS_INFINITE);
        if (parameters.size() > 0) {
            this.documentation = "*Arguments:* \n" + parameters.stream().map(p -> p.text.trim().replaceAll("\n", " = ")).collect(Collectors.joining("\n")) + "\n\n*Documentation:*\n" + documentation;
        }
        this.library = library;
        this.indexOfNextKeyword = determineIndexOfNextKeyword();
    }

    private static Map<String, Integer> keywordToNextIndex = new HashMap<>();
    static {
        keywordToNextIndex.put("Wait Until Keyword Succeeds", 2);
        keywordToNextIndex.put("Repeat Keyword", 1);
        keywordToNextIndex.put("Run Keyword", 0);
        keywordToNextIndex.put("Run Keywords", 0);
        keywordToNextIndex.put("Run Keyword And Continue On Failure", 0);
        keywordToNextIndex.put("Run Keyword And Expect Error", 1);
        keywordToNextIndex.put("Run Keyword And Ignore Error", 0);
        keywordToNextIndex.put("Run Keyword And Return", 0);
        keywordToNextIndex.put("Run Keyword And Return If", 1);
        keywordToNextIndex.put("Run Keyword And Return Status", 0);
        keywordToNextIndex.put("Run Keyword If", 1);
        keywordToNextIndex.put("Run Keyword If All Critical Tests Passed", 0);
        keywordToNextIndex.put("Run Keyword If All Tests Passed", 0);
        keywordToNextIndex.put("Run Keyword If Any Critical Tests Failed", 0);
        keywordToNextIndex.put("Run Keyword If Any Tests Failed", 0);
        keywordToNextIndex.put("Run Keyword If Test Failed", 0);
        keywordToNextIndex.put("Run Keyword If Test Passed", 0);
        keywordToNextIndex.put("Run Keyword If Timeout Occurred", 0);
        keywordToNextIndex.put("Run Keyword Unless", 1);
    }
    private int determineIndexOfNextKeyword() {
        if (library.getKind() != LibraryKind.PACKED_IN) {
            return -1;
        }
        return keywordToNextIndex.getOrDefault(this.canonicalName, -1);
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
                return PRIORITY_EXTERNAL_LIBRARY + canonicalName.length();
            case PACKED_IN:
                return PRIORITY_PACKED_IN + canonicalName.length();
            default:
                return 100; // unknown
        }
    }

    @Override
    public int getArgumentIndexOfKeywordArgument() {
        return indexOfNextKeyword;
    }

    @Override
    public String getSourceName() {
        return library.getName();
    }
}

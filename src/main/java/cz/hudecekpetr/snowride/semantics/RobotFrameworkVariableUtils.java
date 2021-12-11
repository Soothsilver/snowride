package cz.hudecekpetr.snowride.semantics;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RobotFrameworkVariableUtils {
    public static String SCALAR_VARIABLE = "$";
    public static String LIST_VARIABLE = "@";
    public static String DICTIONARY_VARIABLE = "&";
    public static String ENVIRONMENT_VARIABLE = "%";
    public static List<String> ALL_VARIABLE_TYPE_IDENTIFIERS = Arrays.asList(SCALAR_VARIABLE, LIST_VARIABLE, DICTIONARY_VARIABLE, ENVIRONMENT_VARIABLE);
    public static List<String> VARIABLE_TYPE_IDENTIFIERS = Arrays.asList(SCALAR_VARIABLE, LIST_VARIABLE, DICTIONARY_VARIABLE);

    public static boolean isVariable(String contents) {
        return ALL_VARIABLE_TYPE_IDENTIFIERS.stream().anyMatch(typeIdentifier -> contents.startsWith(typeIdentifier + "{"));
    }

    public static boolean containsAnyVariable(String text) {
        return ALL_VARIABLE_TYPE_IDENTIFIERS.stream().anyMatch(typeIdentifier -> text.contains(typeIdentifier + "{"));
    }

    public static boolean containsVariable(String text) {
        return VARIABLE_TYPE_IDENTIFIERS.stream().anyMatch(typeIdentifier -> text.contains(typeIdentifier + "{"));
    }

    public static boolean containsVariable(String text, String variable) {
        if (variable.startsWith(ENVIRONMENT_VARIABLE)) {
            return false;
        }
        return VARIABLE_TYPE_IDENTIFIERS.stream().anyMatch(typeIdentifier -> text.contains(typeIdentifier + "{" + variable + "}"));
    }

    public static boolean containsVariable(String text, Set<String> variables) {
        return text.contains(ENVIRONMENT_VARIABLE + "{") || VARIABLE_TYPE_IDENTIFIERS.stream().anyMatch(typeIdentifier -> variables.stream().anyMatch(variable -> text.contains(typeIdentifier + "{" + variable + "}")));
    }

    public static String getVariableName(String variable) {
        return StringUtils.substringBetween(variable, "{", "}");
    }
}

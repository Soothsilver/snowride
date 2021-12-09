package cz.hudecekpetr.snowride.semantics;

import java.util.Arrays;
import java.util.List;

public class RobotFrameworkVariableUtils {
    public static String SCALAR_VARIABLE = "$";
    public static String LIST_VARIABLE = "@";
    public static String DICTIONARY_VARIABLE = "&";
    public static String ENVIRONMENT_VARIABLE = "%";
    public static List<String> ALL_VARIABLE_TYPE_IDENTIFIERS = Arrays.asList(SCALAR_VARIABLE, LIST_VARIABLE, DICTIONARY_VARIABLE, ENVIRONMENT_VARIABLE);
    public static List<String> VARIABLE_TYPE_IDENTIFIERS = Arrays.asList(SCALAR_VARIABLE, LIST_VARIABLE, DICTIONARY_VARIABLE);

    public static boolean isVariable(String contents) {
        return ALL_VARIABLE_TYPE_IDENTIFIERS.stream().anyMatch(typeIdentifier -> contents.startsWith(typeIdentifier + "{") && contents.endsWith("}"));
    }

    public static boolean containsVariable(String text, String variable) {
        if (variable.startsWith(ENVIRONMENT_VARIABLE)) {
            return false;
        }
        return VARIABLE_TYPE_IDENTIFIERS.stream().anyMatch(typeIdentifier -> text.contains(typeIdentifier + variable.substring(1)));
    }

    public static boolean containsVariable(String text) {
        return ALL_VARIABLE_TYPE_IDENTIFIERS.stream().anyMatch(typeIdentifier -> text.contains(typeIdentifier + "{"));
    }
}

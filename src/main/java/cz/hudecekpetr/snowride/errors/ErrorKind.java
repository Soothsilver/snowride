package cz.hudecekpetr.snowride.errors;

/**
 * Used only by the Errors tab.
 */
public enum ErrorKind {
    PARSE_ERROR("Parse error", false),
    IMPORT_ERROR("Failed import", false),
    BAD_KEYWORD("Unknown keyword", true),
    MISSING_ARGUMENT("Missing mandatory argument", true),
    TOO_MANY_ARGUMENTS("Too many arguments", true);

    private String humanReadable;
    private boolean lineError;

    ErrorKind(String humanReadable, boolean lineError) {
        this.humanReadable = humanReadable;
        this.lineError = lineError;
    }


    @Override
    public String toString() {
        return humanReadable;
    }

    public boolean isLineError() {
        return lineError;
    }
}

package cz.hudecekpetr.snowride.errors;

/**
 * Used only by the Errors tab.
 */
public enum ErrorKind {
    PARSE_ERROR("Parse error"),
    IMPORT_ERROR("Import error");

    private String humanReadable;

    ErrorKind(String humanReadable) {
        this.humanReadable = humanReadable;
    }


    @Override
    public String toString() {
        return humanReadable;
    }
}

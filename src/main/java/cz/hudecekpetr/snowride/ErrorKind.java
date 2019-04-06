package cz.hudecekpetr.snowride;

public enum ErrorKind {
    PARSE_ERROR("Parse error"),
    IMPORT_ERROR("Import error"),
    UNKNOWN_KEYWORD("Unknown keyword"),
    SERIALIZATION_ERROR("Incorrect serialization");

    private String humanReadable;

    ErrorKind(String humanReadable) {
        this.humanReadable = humanReadable;
    }


    @Override
    public String toString() {
        return humanReadable;
    }
}

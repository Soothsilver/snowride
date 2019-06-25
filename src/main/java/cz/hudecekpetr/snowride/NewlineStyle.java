package cz.hudecekpetr.snowride;

/**
 * What to use as line separators. By default, we use LF only, unless the file as loaded has CRLF.
 */
public enum NewlineStyle {
    /**
     * Unix-style separators.
     */
    LF,
    /**
     * Windows-style separators.
     */
    CRLF;

    public String convertToStyle(String str) {
        // Commit Unix-style:
        if (this == LF) {
            return str.replace("\r\n", "\n");
        } else {
            return str.replace("\r\n", "\n").replace("\n", "\r\n");
        }
    }
}

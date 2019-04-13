package cz.hudecekpetr.snowride;

public enum NewlineStyle {
    LF,
    CRLF;

    NewlineStyle() {
    }

    public String convertToStyle(String str) {
        // Commit Unix-style:
        if (this == LF) {
            return str.replace("\r\n", "\n");
        } else {
            return str.replace("\r\n", "\n").replace("\n", "\r\n");
        }
    }
}

package cz.hudecekpetr.snowride.semantics;

public class QualifiedKeyword {
    String source;
    String keyword;

    private QualifiedKeyword(String source, String keyword) {
        this.source = source;
        this.keyword = keyword;
    }

    public static QualifiedKeyword fromDottedString(String string) {
        int lastDot = string.lastIndexOf('.');
        if (lastDot != -1) {
            String source = string.substring(0, lastDot);
            String keyword = string.substring(lastDot + 1);
            return new QualifiedKeyword(source, keyword);
        } else {
            return new QualifiedKeyword(null, string);
        }
    }

    public String getSource() {
        return source;
    }

    public String getKeyword() {
        return keyword;
    }
}

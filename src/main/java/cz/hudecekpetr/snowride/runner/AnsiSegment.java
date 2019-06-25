package cz.hudecekpetr.snowride.runner;

/**
 * A colored part of text.
 */
public class AnsiSegment {
    public String text;
    public AnsiColor color;

    public AnsiSegment(String text, AnsiColor color) {
        this.text = text;
        this.color = color;
    }
}

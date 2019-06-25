package cz.hudecekpetr.snowride.runner;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiConsumer;

/**
 * Gathers a stream of characters from outside (using {@link #addFromOutside(String)} and splits it into {@link AnsiSegment}s
 * which are fed to the outside text area via {@link #flushInto(BiConsumer)}. Each segment represents text written
 * in a different color.
 */
public class AnsiOutputStream {
    private AnsiColor currentColor = AnsiColor.BLACK;
    private Queue<AnsiSegment> segments = new ArrayDeque<>();
    private String rememberedTextFragment = "";

    /**
     * Adds characters from the outside to this stream. These characters may include ANSI color codes.
     */
    public void addFromOutside(String text) {
        String theText = rememberedTextFragment;
        rememberedTextFragment = "";
        add(theText + text);
    }

    private void add(String text) {
        int firstAnsiChar = text.indexOf("\u001b[");
        if (firstAnsiChar == -1) {
            segments.add(new AnsiSegment(text, currentColor));
        } else if (firstAnsiChar == 0) {
            int whereStart = "\u001b[".length();
            int whereEnd = text.indexOf('m');
            if (whereEnd == -1) {
                // We didn't receive the ASCII color-change end character, maybe it will come in the future?
                if (text.length() < 5) {
                    // Yeah, it should probably come later and we'll solve it then
                    rememberedTextFragment = text;
                } else {
                    // Well, it will probably never come, let's just ignore the special character:
                    currentColor = AnsiColor.BLACK;
                    add(text.substring(whereStart));
                }
            } else {
                String asciiCode = text.substring(whereStart, whereEnd);
                updateCurrentColorBasedOnAsciiCode(asciiCode);
                add(text.substring(whereEnd + 1));
            }
        } else {
            segments.add(new AnsiSegment(text.substring(0, firstAnsiChar), currentColor));
            add(text.substring(firstAnsiChar));
        }
    }

    private void updateCurrentColorBasedOnAsciiCode(String asciiCode) {
        AnsiColor ansiThing = currentColor;
        switch (asciiCode) {
            case "0":
            case "30":
                ansiThing = AnsiColor.BLACK;
                break;
            case "31":
                ansiThing = AnsiColor.RED;
                break;
            case "32":
                ansiThing = AnsiColor.GREEN;
                break;
            case "33":
                ansiThing = AnsiColor.YELLOW;
                break;
            case "34":
                ansiThing = AnsiColor.BLUE;
                break;
            case "35":
                ansiThing = AnsiColor.MAGENTA;
                break;
            case "36":
                ansiThing = AnsiColor.CYAN;
                break;
            case "37":
                ansiThing = AnsiColor.WHITE;
                break;
        }
        currentColor = ansiThing;
    }

    /**
     * For each segment prepared by this stream, call the consumer on it. This removes the segment from the stream.
     * The first argument is the text of the segment. The second is the JavaFX CSS style that should be applied
     * to that segment.
     */
    public void flushInto(BiConsumer<String, String> textAndStyle) {
        AnsiSegment segment = segments.poll();
        while (segment != null) {
            textAndStyle.accept(segment.text, segment.color.toStyle());
            segment = segments.poll();
        }
    }
}

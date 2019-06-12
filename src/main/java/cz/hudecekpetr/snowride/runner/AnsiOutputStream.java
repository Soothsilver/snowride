package cz.hudecekpetr.snowride.runner;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiConsumer;

public class AnsiOutputStream {
    AnsiColor currentColor = AnsiColor.BLACK;
    Queue<AnsiSegment> segments = new ArrayDeque<>();
    private String rememberedTextFragment = "";

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
                    return;
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

    public void flushInto(BiConsumer<String, String> textAndStyle) {
        AnsiSegment segment = segments.poll();
        while (segment != null) {
            textAndStyle.accept(segment.text, segment.color.toStyle());
            segment = segments.poll();
        }
    }
}

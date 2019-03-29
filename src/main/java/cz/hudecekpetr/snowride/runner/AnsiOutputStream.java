package cz.hudecekpetr.snowride.runner;

import org.fxmisc.richtext.StyledTextArea;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiConsumer;

public class AnsiOutputStream {
    AnsiColor currentColor = AnsiColor.BLACK;
    Queue<AnsiSegment> segments = new ArrayDeque<>();

    public void add(String text) {
        int firstAnsiChar = text.indexOf("\u001b[");
        if (firstAnsiChar == -1)
        {
            segments.add(new AnsiSegment(text, currentColor));
        }
        else if (firstAnsiChar == 0)
        {
            int whereStart = "\u001b[".length();
            int whereEnd = text.indexOf('m');
            AnsiColor ansiThing = currentColor;
            switch (text.substring(whereStart, whereEnd))
            {
                case "0":
                case "30": ansiThing = AnsiColor.BLACK; break;
                case "31": ansiThing = AnsiColor.RED; break;
                case "32": ansiThing = AnsiColor.GREEN; break;
                case "33": ansiThing = AnsiColor.YELLOW; break;
                case "34": ansiThing = AnsiColor.BLUE; break;
                case "35": ansiThing = AnsiColor.MAGENTA; break;
                case "36": ansiThing = AnsiColor.CYAN; break;
                case "37": ansiThing = AnsiColor.WHITE; break;
            }
            currentColor = ansiThing;
            add(text.substring(whereEnd + 1));
        }
        else
        {
            segments.add(new AnsiSegment(text.substring(0, firstAnsiChar), currentColor));
            this.add(text.substring(firstAnsiChar));
        }
    }

    public void flushInto(BiConsumer<String, String> textAndStyle) {
        AnsiSegment segment = segments.poll();
        while (segment != null) {
            textAndStyle.accept(segment.text, segment.color.toStyle());
            segment = segments.poll();
        }
    }
}

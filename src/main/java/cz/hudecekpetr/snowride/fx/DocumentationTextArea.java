package cz.hudecekpetr.snowride.fx;

import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.text.TextFlow;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.EditableStyledDocument;
import org.fxmisc.richtext.model.SimpleEditableStyledDocument;
import org.fxmisc.richtext.model.TextOps;

import java.util.function.BiConsumer;

/**
 * Displays Robot Framework-style documentation in a semi-pretty way in the documentation popup. Even if it's called
 * a text area, it's not editable.
 */
public class DocumentationTextArea extends StyledTextArea<String, String> {

    public static final String DOC_STYLE = "-fx-font-size: 10pt; ";
    public static final String BOLD_STYLE = DOC_STYLE + "-fx-font-weight: bold; ";
    public static final String ITALICS_STYLE = DOC_STYLE + "-fx-font-style: italic; ";
    public static final String CODE_STYLE = DOC_STYLE + "-fx-font-family: monospace; ";

    /**
     * Using [[ITALICS]] instead of underscore because primitive handling of underscore would lead to many documentations
     * looking weird. For example, if an underscore is used in the middle of a word (such as in a variable or keyword name),
     * it shouldn't be counted as the beginning of italics. Italics maybe also shouldn't overflow end of line or at least
     * end of section (Args/Tags/Documentation proper).
     *
     * We still have the capability for italics so that we can add italics text programatically from within Snowride.
     */
    public static final String ITALICS_CHARACTER = "[[ITALICS]]";

    public DocumentationTextArea() {
        super("", TextFlow::setStyle,
                DOC_STYLE, TextExt::setStyle,
                new SimpleEditableStyledDocument<>("", DOC_STYLE),
                true);
        this.setWrapText(true);
        this.setBackground(Background.EMPTY);
        this.setUseInitialStyleForInsertion(true);
    }

    @Override
    public void requestFocus() {
        // Prevent this from getting focus.
    }

    public void setDocumentation(String fullDocumentation) {
        this.clear();
        if (fullDocumentation == null) {
            return;
        }
        int startFromIndex = 0;
        while (startFromIndex < fullDocumentation.length()) {
            // Go from start to end. When you encounter a special character (*, [[ITALICS]], or ``), make everything up to the second
            // instance of that character of that style, then proceeds. This is a slow and primitive implementation that
            // doesn't support multistyles (bold italics), tables, horizontal rulers, underscores and a bunch of other things,
            // I'm sure.
            int earliestAsterisk = fullDocumentation.indexOf('*', startFromIndex);
            int earliestUnderscore = fullDocumentation.indexOf(ITALICS_CHARACTER, startFromIndex);
            int earliestCode = fullDocumentation.indexOf("``", startFromIndex);
            int minimumSpecial = minNonMinusOne(earliestAsterisk, earliestUnderscore, earliestCode);
            if (minimumSpecial == -1) {
                appendText(fullDocumentation.substring(startFromIndex));
                break;
            } else {
                appendText(fullDocumentation.substring(startFromIndex, minimumSpecial));
                if (minimumSpecial == earliestAsterisk) {
                    int finalAsterisk = fullDocumentation.indexOf('*', earliestAsterisk+1);
                    if (finalAsterisk != -1) {
                        int then = this.getLength();
                        appendText(fullDocumentation.substring(earliestAsterisk+1, finalAsterisk));
                        int now = this.getLength();
                        setStyle(then, now, BOLD_STYLE);
                        startFromIndex = finalAsterisk +1;
                        continue;
                    }
                } else if (minimumSpecial == earliestCode) {
                    int finalCode  = fullDocumentation.indexOf("``", earliestCode+2);
                    if (finalCode != -1) {
                        int then = this.getLength();
                        appendText(fullDocumentation.substring(earliestCode+2, finalCode));
                        int now = this.getLength();
                        setStyle(then, now, CODE_STYLE);
                        startFromIndex = finalCode + 2;
                        continue;
                    }
                } else if (minimumSpecial == earliestUnderscore) {
                    int finalUnderscore  = fullDocumentation.indexOf(ITALICS_CHARACTER, earliestUnderscore + ITALICS_CHARACTER.length());
                    if (finalUnderscore != -1) {
                        int then = this.getLength();
                        appendText(fullDocumentation.substring(earliestUnderscore+ ITALICS_CHARACTER.length(), finalUnderscore));
                        int now = this.getLength();
                        setStyle(then, now, ITALICS_STYLE);
                        startFromIndex = finalUnderscore + ITALICS_CHARACTER.length();
                        continue;
                    }
                }
                appendText(Character.toString(fullDocumentation.charAt(minimumSpecial)));
                startFromIndex = minimumSpecial + 1;
            }
        }
    }

    /**
     * Returns the minimum of the the three values, but only counts values that aren't -1. If all three values
     * are -1, then returns -1.
     */
    private int minNonMinusOne(int earliestAsterisk, int earliestUnderscore, int earliestCode) {
        int bestSoFar = Integer.MAX_VALUE;
        if (earliestAsterisk < bestSoFar && earliestAsterisk != -1) {
            bestSoFar = earliestAsterisk;
        }
        if (earliestUnderscore < bestSoFar && earliestUnderscore != -1) {
            bestSoFar = earliestUnderscore;
        }
        if (earliestCode < bestSoFar && earliestCode != -1) {
            bestSoFar = earliestCode;
        }
        if (bestSoFar == Integer.MAX_VALUE) {
            return -1;
        }
        return bestSoFar;
    }
}

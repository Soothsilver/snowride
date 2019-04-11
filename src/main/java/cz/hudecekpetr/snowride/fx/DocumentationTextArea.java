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

public class DocumentationTextArea extends StyledTextArea<String, String> {
    public static final String DOC_STYLE = "-fx-font-size: 8pt; ";
    public static final String BOLD_STYLE = DOC_STYLE + "-fx-font-weight: bold; ";
    public static final String CODE_STYLE = DOC_STYLE + "-fx-font-family: Consolas, 'Courier New', monospace; ";

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
            int earliestAsterisk = fullDocumentation.indexOf('*', startFromIndex);
            int earliestUnderscore = fullDocumentation.indexOf('_', startFromIndex);
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
                }
                appendText(Character.toString(fullDocumentation.charAt(minimumSpecial)));
                startFromIndex = minimumSpecial + 1;
            }
        }
    }

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

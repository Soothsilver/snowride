package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.NewlineStyle;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.MultiChangeBuilder;
import org.fxmisc.richtext.model.TwoDimensional;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

import java.util.Collection;
import java.util.Collections;

import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCombination.SHIFT_ANY;
import static javafx.scene.input.KeyCombination.SHORTCUT_ANY;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.fxmisc.wellbehaved.event.EventPattern.anyOf;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

public class SnowCodeArea extends CodeArea {
    private final TextField searchBox;
    private boolean textLoaded = false;

    public SnowCodeArea(TextField searchBox) {
        super();
        this.searchBox = searchBox;
    }

    public void overrideDefaultKeybindings(NewlineStyle newlineStyle) {
        Nodes.addInputMap(this, InputMap.consume(
                anyOf(keyPressed(TAB, SHORTCUT_ANY, SHIFT_ANY))
        ));

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F3) {
                searchNext();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                String text = getText();
                if (text != null) {
                    clearStyle(0, text.length());
                }
            } else if (event.getCode() == KeyCode.TAB) {
                MultiChangeBuilder<Collection<String>, String, Collection<String>> multiChange = createMultiChange();
                String selectedText = getSelectedText();
                if (event.isShiftDown()) {
                    if (StringUtils.isBlank(selectedText)) {
                        int caretPosition = getCaretPosition();
                        int removedChars = tryToRemoveSpacesFromBeginningOfLine(multiChange, caretPosition);
                        multiChange.commit();
                        displaceCaret(caretPosition - removedChars);
                    } else {
                        boolean doCommit = false;
                        int selectionStart = getSelection().getStart();
                        int selectionEnd = getSelection().getEnd();
                        int firstLineRemovedChars = tryToRemoveSpacesFromBeginningOfLine(multiChange, selectionStart);
                        selectionEnd -= firstLineRemovedChars;

                        doCommit = firstLineRemovedChars != 0;
                        String separator = newlineStyle == NewlineStyle.CRLF ? "\r\n" : "\n";
                        int index = selectedText.indexOf(separator);
                        while (index >= 0) {
                            int removedChars = tryToRemoveSpacesFromBeginningOfLine(multiChange, selectionStart + index + separator.length());
                            doCommit = doCommit || removedChars != 0;
                            index = selectedText.indexOf(separator, index + separator.length());
                            selectionEnd -= removedChars;
                        }
                        if (doCommit) {
                            multiChange.commit();
                        }
                        selectRange(selectionStart - firstLineRemovedChars, selectionEnd);
                    }
                } else {
                    if (StringUtils.isBlank(selectedText)) {
                        insertText(getCaretPosition(), "    ");
                    } else {
                        int selectionStart = getSelection().getStart();
                        int selectionEnd = getSelection().getEnd();
                        TwoDimensional.Position pos = offsetToPosition(selectionStart, TwoDimensional.Bias.Backward);
                        multiChange.insertText(selectionStart - pos.getMinor(), "    ");
                        selectionEnd += 4;
                        String separator = newlineStyle == NewlineStyle.CRLF ? "\r\n" : "\n";
                        int index = selectedText.indexOf(separator);
                        while (index >= 0) {
                            multiChange.insertText(selectionStart + index + separator.length(), "    ");
                            index = selectedText.indexOf(separator, index + separator.length());
                            selectionEnd += 4;
                        }
                        multiChange.commit();
                        selectRange(selectionStart + 4, selectionEnd);
                    }
                }
            }
        });
    }

    public void loadElement(HighElement value, Scenario lastLoadedScenario) {
        if (value instanceof Suite) {
            int indexOfDifference = StringUtils.indexOfDifference(getText(), value.contents);
            if (indexOfDifference > 0) {
                replaceText(indexOfDifference, getText().length(), value.contents.substring(indexOfDifference));
            } else {
                replaceText(value.contents);
            }
        }
        if (!textLoaded) {
            textLoaded = true;
            getUndoManager().forgetHistory();
        }
        if (lastLoadedScenario != null) {
            String text = getText();
            int index = StringUtils.indexOf(text, lastLoadedScenario.getShortName());
            if (index > 0) {
                Platform.runLater(() -> {
                    // FIXME: this is tricky. Trying to ensure "whole" Scenario (test case) will be visible.
                    //        Uninformatively changing caret location and calling "requestFollowCaret()" twice immediately doesn't do the trick.
                    displaceCaret(text.length());
                    requestFollowCaret();
                    Platform.runLater(() -> {
                        requestFocus();
                        displaceCaret(index);
                        requestFollowCaret();
                    });
                });
            }
        }
    }

    public void searchBoxChanged(String newValue) {
        String text = getText();
        if (text != null) {
            clearStyle(0, text.length());
        } else {
            // Sometime this can happen, apparently...
            return;
        }
        if (StringUtils.isEmpty(newValue)) {
            searchBox.setStyle(null);
            return;
        }

        if (highlightAllOccurrences(newValue)) {
            int firstIndex = StringUtils.indexOfIgnoreCase(text, newValue, getCaretPosition());
            if (firstIndex == -1) {
                firstIndex = StringUtils.indexOfIgnoreCase(text, newValue);
            }
            Paint paint = Paint.valueOf("#bff2ff");
            searchBox.setStyle("-fx-control-inner-background: #" + paint.toString().substring(2));
            selectRange(firstIndex, firstIndex + newValue.length());
        } else {
            // I have no idea how this works.
            // But see https://stackoverflow.com/a/27708846/1580088
            Paint paint = Paint.valueOf("#ffa0b9");
            searchBox.setStyle("-fx-control-inner-background: #" + paint.toString().substring(2));
        }
    }

    public boolean highlightAllOccurrences(String newValue) {
        String text = getText();
        boolean contains = containsIgnoreCase(text, newValue);
        if (contains) {
            int index = 0;
            while (true) {
                index = StringUtils.indexOfIgnoreCase(text, newValue, index);
                if (index < 0) {
                    break;
                }
                setStyle(index, index + newValue.length(), Collections.singleton("search-highlight"));
                index += newValue.length();
            }
        }
        return contains;
    }

    /**
     * Selects the next instance of the searched text in the main editor.
     */
    public void searchNext() {
        if (getText() == null) {
            return;
        }
        String searchFor = searchBox.getText();
        int firstIndex = StringUtils.indexOfIgnoreCase(getText(), searchFor, getAnchor() + 1);
        if (firstIndex == -1) {
            int fromStartIndex = StringUtils.indexOfIgnoreCase(getText(), searchFor);
            if (fromStartIndex != -1) {
                selectRange(fromStartIndex, fromStartIndex + searchFor.length());
                requestFollowCaret();
            }
        } else {
            selectRange(firstIndex, firstIndex + searchFor.length());
            requestFollowCaret();
        }

    }

    private int tryToRemoveSpacesFromBeginningOfLine(MultiChangeBuilder<Collection<String>, String, Collection<String>> multiChange, int index) {
        TwoDimensional.Position pos = offsetToPosition(index, TwoDimensional.Bias.Backward);
        int lineStartIndex = index - pos.getMinor();
        if (getText(lineStartIndex, lineStartIndex + 4).equals("    ")) {
            multiChange.replaceText(lineStartIndex, lineStartIndex + 4, "");
            return 4;
        }
        return 0;
    }
}

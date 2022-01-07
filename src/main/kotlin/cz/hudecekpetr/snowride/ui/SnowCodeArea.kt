package cz.hudecekpetr.snowride.ui

import cz.hudecekpetr.snowride.tree.highelements.HighElement
import cz.hudecekpetr.snowride.tree.highelements.Scenario
import javafx.application.Platform
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import org.apache.commons.lang3.StringUtils
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.MultiChangeBuilder
import org.fxmisc.richtext.model.TwoDimensional
import org.fxmisc.wellbehaved.event.EventPattern
import org.fxmisc.wellbehaved.event.InputMap
import org.fxmisc.wellbehaved.event.Nodes

class SnowCodeArea(private val highElement: HighElement?) : CodeArea() {
    private val separator = "\n"

    init {
        reload()
        undoManager.forgetHistory()
        redefineKeyBehaviour()
        style = "-fx-font-family: \"JetBrains Mono\""
    }

    fun reload() {
        val contents = highElement?.contents?.replace("\r\n", "\n")
        if (contents != null && text != contents) {
            val indexOfDifference = StringUtils.indexOfDifference(text, contents)
            if (indexOfDifference > 0) {
                replaceText(indexOfDifference, text.length, contents.substring(indexOfDifference))
            } else {
                replaceText(contents)
                displaceCaret(0)
            }
        }
    }

    fun moveCaretToCurrentlyEditedScenario(scenario: Scenario?) {
        if (scenario != null) {
            val index = text.indexOf(scenario.shortName)
            if (index > 0) {
                Platform.runLater {
                    requestFocus()
                    displaceCaret(index)
                    requestFollowCaret()
                }
            }
        } else {
            displaceCaret(0)
            requestFollowCaret()
        }
    }

    fun getCurrentlyEditedScenario(): HighElement?  {
        var total = 0
        text.lines()
            .map {
                val start = total
                total += it.length + 1
                Pair(it, start)
            }
            .filter { (_, index) -> index <= caretPosition }
            .map { it.first }
            .lastOrNull { line -> line.matches("^\\w.*".toRegex()) }
            ?.let { line ->
                highElement?.children?.find { it.shortName == line }?.let {
                    return it
                }
            }
        return highElement;
    }

    private fun redefineKeyBehaviour() {
        Nodes.addInputMap(
            this,
            InputMap.consume(EventPattern.anyOf(EventPattern.keyPressed(KeyCode.TAB, KeyCombination.SHORTCUT_ANY, KeyCombination.SHIFT_ANY)))
        )
        setOnKeyPressed { event: KeyEvent ->
            if (event.code == KeyCode.F3 && event.isShiftDown) {
                SnowCodeAreaSearchBox.searchNext(reversed = true)
                event.consume()
            } else if (event.code == KeyCode.F3) {
                SnowCodeAreaSearchBox.searchNext()
                event.consume()
            } else if (event.code == KeyCode.ESCAPE) {
                val text = text
                if (text != null) {
                    clearStyle(0, text.length)
                }
            } else if (event.code == KeyCode.TAB) {
                fun tryToRemoveSpacesFromBeginningOfLine(multiChange: MultiChangeBuilder<Collection<String>, String, Collection<String>>, index: Int): Int {
                    val pos = offsetToPosition(index, TwoDimensional.Bias.Backward)
                    val lineStartIndex = index - pos.minor
                    if (getText(lineStartIndex, lineStartIndex + 4) == "    ") {
                        multiChange.replaceText(lineStartIndex, lineStartIndex + 4, "")
                        return 4
                    }
                    return 0
                }

                val multiChange = createMultiChange()
                val selectedText = selectedText
                if (event.isShiftDown) {
                    if (selectedText.isBlank()) {
                        val removedChars = tryToRemoveSpacesFromBeginningOfLine(multiChange, caretPosition)
                        multiChange.commit()
                        displaceCaret(caretPosition - removedChars)
                    } else {
                        var doCommit = false
                        val selectionStart = selection.start
                        var selectionEnd = selection.end
                        val firstLineRemovedChars = tryToRemoveSpacesFromBeginningOfLine(multiChange, selectionStart)
                        selectionEnd -= firstLineRemovedChars
                        doCommit = firstLineRemovedChars != 0
                        var index = selectedText.indexOf(separator)
                        while (index >= 0) {
                            val removedChars = tryToRemoveSpacesFromBeginningOfLine(multiChange, selectionStart + index + separator.length)
                            doCommit = doCommit || removedChars != 0
                            index = selectedText.indexOf(separator, index + separator.length)
                            selectionEnd -= removedChars
                        }
                        if (doCommit) {
                            multiChange.commit()
                        }
                        selectRange(selectionStart - firstLineRemovedChars, selectionEnd)
                    }
                } else {
                    if (selectedText.isBlank()) {
                        insertText(caretPosition, "    ")
                    } else {
                        val selectionStart = selection.start
                        var selectionEnd = selection.end
                        val pos = offsetToPosition(selectionStart, TwoDimensional.Bias.Backward)
                        multiChange.insertText(selectionStart - pos.minor, "    ")
                        selectionEnd += 4
                        var index = selectedText.indexOf(separator)
                        while (index >= 0) {
                            multiChange.insertText(selectionStart + index + separator.length, "    ")
                            index = selectedText.indexOf(separator, index + separator.length)
                            selectionEnd += 4
                        }
                        multiChange.commit()
                        selectRange(selectionStart + 4, selectionEnd)
                    }
                }
            }
        }
    }
}
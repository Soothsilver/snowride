package cz.hudecekpetr.snowride.ui

import cz.hudecekpetr.snowride.tree.highelements.HighElement
import javafx.application.Platform
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination.SHIFT_ANY
import javafx.scene.input.KeyCombination.SHORTCUT_ANY
import javafx.scene.input.KeyEvent
import org.apache.commons.lang3.StringUtils
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.MultiChangeBuilder
import org.fxmisc.richtext.model.TwoDimensional
import org.fxmisc.wellbehaved.event.EventPattern
import org.fxmisc.wellbehaved.event.EventPattern.keyPressed
import org.fxmisc.wellbehaved.event.InputMap
import org.fxmisc.wellbehaved.event.Nodes

class SnowCodeArea(var highElement: HighElement?) : CodeArea() {
    private val separator = "\n"
    var reloading = false

    init {
        reload()
        undoManager.forgetHistory()

        Nodes.addInputMap(
            this,
            InputMap.consume(
                EventPattern.anyOf(
                    keyPressed(KeyCode.TAB, SHORTCUT_ANY, SHIFT_ANY),
                    keyPressed(KeyCode.Y, SHORTCUT_ANY)
                )
            )
        )

        keyBindings()
        style = "-fx-font-family: \"JetBrains Mono\""
    }

    fun reload() {
        val contents = highElement?.contents?.replace("\r\n", "\n")
        if (contents != null && text != contents) {
            reloading = true
            val indexOfDifference = StringUtils.indexOfDifference(text, contents)
            if (indexOfDifference > 0) {
                replaceText(indexOfDifference, text.length, contents.substring(indexOfDifference))
            } else {
                replaceText(contents)
                displaceCaret(0)
            }
            reloading = false
        }
    }

    fun moveCaretToCurrentlyEditedScenario(scenarioName: String?) {
        val index = if (scenarioName != null) {
            text.indexOf("\n${scenarioName}") + 1
        } else {
            0
        }

        Platform.runLater {
            requestFocus()
            displaceCaret(index)
            requestFollowCaret()
        }
    }

    fun getCurrentlyEditedScenario(): HighElement? {
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

    private fun keyBindings() {
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
                event.consume()
            } else if (event.code == KeyCode.Y && event.isControlDown) {
                // Delete current line or selected lines
                if (selectedText.isBlank()) {
                    val line = line(caretPosition)
                    line.delete()
                } else {
                    val allLines = selectedLines().reversed()
                    allLines.forEach { it.delete() }
                }
                event.consume()
            } else if (event.code == KeyCode.D && event.isControlDown) {
                // Duplicate current line or selected text
                if (selectedText.isBlank()) {
                    val line = line(caretPosition)
                    replaceText(line.to, line.to, separator + line.text)
                } else {
                    replaceText(caretPosition, caretPosition, selectedText)
                }
                event.consume()
            } else if (event.code == KeyCode.SLASH && event.isControlDown) {
                // Comment/Uncomment current line or selected lines
                if (selectedText.isBlank()) {
                    val line = line(caretPosition)
                    if (line.isCommented) {
                        line.unComment()
                    } else {
                        line.comment()
                    }
                } else {
                    var startIndex = selection.start
                    var endIndex = selection.end

                    val allLines = selectedLines().reversed()

                    if (allLines.any { !it.isCommented }) {
                        allLines.forEach { it.comment() }
                        startIndex += 1
                        endIndex += allLines.size
                    } else {
                        allLines.forEach { it.unComment() }
                        startIndex -= 1
                        endIndex -= allLines.size
                    }
                    selectRange(startIndex, endIndex)
                }
                event.consume()
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
                        if (removedChars > 0) {
                            multiChange.commit()
                            displaceCaret(caretPosition - removedChars)
                        }
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

    data class Line(val text: String, val from: Int, val to: Int) {
        val isCommented = text.trim().startsWith("#")
    }

    private fun Line.unComment() {
        replaceText(from, to + 1, text.replaceFirst("#", "") + separator)
    }

    private fun Line.comment() {
        val commentIndex = text.length - text.trimStart().length
        replaceText(from, to + 1, text.substring(0, commentIndex) + "#" + text.substring(commentIndex) + separator)
    }

    private fun Line.delete() {
        replaceText(from, to + separator.length, "")
    }

    private fun selectedLines(): List<Line> {
        val startIndex = selection.start
        val firstLine = line(startIndex)
        var currentIndex = startIndex
        val otherLines = selectedText.split(separator).map {
            currentIndex += it.length + separator.length
            line(currentIndex)
        }.dropLast(1)
        return listOf(firstLine).plus(otherLines)
    }

    private fun line(index: Int): Line {
        val textBefore = text.substring(0, index).substringAfterLast(separator)
        val textAfter = text.substring(index).substringBefore(separator)
        val from = index - textBefore.length
        val to = index + textAfter.length
        return Line(textBefore + textAfter, from, to)
    }
}

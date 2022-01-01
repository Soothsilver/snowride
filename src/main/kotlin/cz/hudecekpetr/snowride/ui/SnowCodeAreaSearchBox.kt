package cz.hudecekpetr.snowride.ui

import cz.hudecekpetr.snowride.ui.SnowCodeAreaProvider.codeArea
import javafx.event.EventHandler
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Paint

object SnowCodeAreaSearchBox : TextField() {

    init {
        promptText = "Ctrl+F to search..."
        textProperty().addListener { _, _, newValue ->
            newValue?.let { searchBoxChanged(it) }
        }
        focusedProperty().addListener { _, oldValue, isFocused ->
            if (!oldValue!! && isFocused && codeArea.selectedText.isNotBlank()) {
                text = codeArea.selectedText
            }
            if (isFocused) {
                if (text.isNotEmpty() && codeArea.text != null) {
                    highlightAllOccurrences(text)
                }
            }
        }
        onKeyPressed = EventHandler { event: KeyEvent ->
            if (event.code == KeyCode.ENTER) {
                searchNext()
                event.consume()
            } else if (event.code == KeyCode.ESCAPE) {
                if (codeArea.text != null) {
                    codeArea.clearStyle(0, codeArea.text.length)
                }
                codeArea.requestFocus()
            }
        }

    }

    /**
     * Selects the next instance of the searched text in the main editor.
     */
    fun searchNext() {
        val searchPhrase = this.text
        codeArea.apply {
            if (searchPhrase == null) {
                return
            }
            val nextIndex = text.indexOf(searchPhrase, anchor + 1, ignoreCase = true)
            if (nextIndex == -1) {
                val fromStartIndex = text.indexOf(searchPhrase, ignoreCase = true)
                if (fromStartIndex != -1) {
                    selectRange(fromStartIndex, fromStartIndex + searchPhrase.length)
                    requestFollowCaret()
                }
            } else {
                selectRange(nextIndex, nextIndex + searchPhrase.length)
                requestFollowCaret()
            }
        }

    }

    private fun highlightAllOccurrences(searchPhrase: String) {
        var index = 0
        while (true) {
            index = codeArea.text.indexOf(searchPhrase, ignoreCase = true, startIndex = index)
            if (index < 0) {
                break
            }
            codeArea.setStyle(index, index + searchPhrase.length, setOf("search-highlight"))
            index += searchPhrase.length
        }
    }

    private fun searchBoxChanged(newValue: String) {
        val text: String? = codeArea.text
        text?.let { codeArea.clearStyle(0, it.length) } ?: return

        if (newValue.isEmpty()) {
            style = null
            return
        }

        if (text.contains(newValue, ignoreCase = true)) {
            highlightAllOccurrences(newValue)
            var firstIndex = text.indexOf(newValue, codeArea.caretPosition, ignoreCase = true)
            if (firstIndex == -1) {
                firstIndex = text.indexOf(newValue, ignoreCase = true)
            }
            val paint = Paint.valueOf("#bff2ff")
            style = "-fx-control-inner-background: #" + paint.toString().substring(2)
            selectRange(firstIndex, firstIndex + newValue.length)
        } else {
            // I have no idea how this works. But see https://stackoverflow.com/a/27708846/1580088
            val paint = Paint.valueOf("#ffa0b9")
            style = "-fx-control-inner-background: #" + paint.toString().substring(2)
        }
    }
}
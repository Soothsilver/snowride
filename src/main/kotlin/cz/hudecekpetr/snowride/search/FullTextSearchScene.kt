package cz.hudecekpetr.snowride.search

import cz.hudecekpetr.snowride.tree.highelements.FileSuite
import cz.hudecekpetr.snowride.tree.highelements.FolderSuite
import cz.hudecekpetr.snowride.tree.highelements.Suite
import cz.hudecekpetr.snowride.ui.SnowCodeAreaProvider
import cz.hudecekpetr.snowride.ui.MainForm
import javafx.animation.PauseTransition
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Stage
import javafx.util.Duration
import java.io.File
import java.util.*


object FullTextSearchScene {

    private val stage: Stage = Stage()
    private val searchResultsView = ListView<SearchResult>()
    private val searchTextField = TextField()

    data class SearchResult(val suite: Suite, val line: String, val lineNumber: Int, val lineAnchor: Int, val anchor: Int, var isNameUnique: Boolean = true) {
        val file: File = when (suite) {
            is FileSuite -> suite.file
            is FolderSuite -> suite.initFile
            else -> throw RuntimeException("Unexpected suite '${suite.javaClass}'.")
        }

        val name: String
            get() = (if (isNameUnique) "" else file.parentFile.name + File.separator) + file.name
    }

    init {
        val previewVBox = VBox(5.0).apply {
            padding = Insets(2.0)
        }

        searchTextField.apply {
            promptText = "Search text.."

            val pause = PauseTransition(Duration.millis(250.0))
            textProperty().addListener { _, _, newValue ->
                pause.setOnFinished {
                    searchResultsView.items.clear()
                    if (newValue.isEmpty()) {
                        previewVBox.children.clear()
                    } else {
                        fullTextSearch(newValue)
                    }
                }
                pause.playFromStart()
            }

            setOnKeyPressed { event: KeyEvent ->
                if (event.code in listOf(KeyCode.UP, KeyCode.DOWN)) {
                    searchResultsView.fireEvent(event)
                    event.consume()
                } else if (event.code == KeyCode.ENTER) {
                    navigateToSelectedSuite()
                }
            }
        }

        val searchAdResultVBox = VBox(5.0, searchTextField, searchResultsView).apply {
            padding = Insets(2.0)
        }

        searchResultsView.apply {
            setCellFactory {
                object : ListCell<SearchResult>() {
                    override fun updateItem(result: SearchResult?, empty: Boolean) {
                        super.updateItem(result, empty)
                        graphic = null
                        result?.apply {
                            val root = HBox(10.0)
                            val searchPhraseLength = searchTextField.text.length

                            val lineFlow = TextFlow()
                            lineFlow.children.add(Text(line.substring(0, lineAnchor)))
                            val highlight = TextFlow(Text(line.substring(lineAnchor, lineAnchor + searchPhraseLength)))
                            highlight.style = "-fx-background-color: yellow"
                            lineFlow.children.add(highlight)
                            lineFlow.children.add(Text(line.substring(lineAnchor + searchPhraseLength, line.length)))

                            root.children.add(lineFlow)
                            // I'll add another Region here to expand, pushing the buttons to the right
                            val region = Region()
                            HBox.setHgrow(region, Priority.ALWAYS)
                            root.children.add(region)

                            val fileInfo = Text("$name  $lineNumber")
                            fileInfo.fill = Color.GRAY
                            root.children.add(fileInfo)

                            graphic = root
                        }
                    }
                }
            }
            selectionModel.selectedItemProperty().addListener { _, _, searchResult ->
                if (searchResult != null) {
                    val codeAreaPane = SnowCodeAreaProvider.getPreviewCodeArea(searchResult.suite)
                    if (previewVBox.children.isEmpty() || previewVBox.children[1] != codeAreaPane) {
                        previewVBox.children.clear()
                        previewVBox.children.add(Text(searchResult.file.absolutePath))
                        previewVBox.children.add(codeAreaPane)
                        codeAreaPane.content.isEditable = false
                    }
                    codeAreaPane.content.selectRange(searchResult.anchor, searchResult.anchor + searchTextField.text.length)
                    codeAreaPane.content.requestFollowCaret()
                }
            }
            setOnMouseClicked { event ->
                if (event.clickCount == 2) {
                    navigateToSelectedSuite()
                }
                searchTextField.requestFocus()
            }
        }

        val fulltextSearchPane = SplitPane(searchAdResultVBox, previewVBox)
        fulltextSearchPane.orientation = Orientation.VERTICAL
        fulltextSearchPane.setDividerPosition(0, 0.3)

        stage.apply {
            scene = Scene(fulltextSearchPane, 820.0, 1000.0)
            title = "Find in Files"

            addEventFilter(KeyEvent.KEY_PRESSED) { event ->
                if (event.code == KeyCode.ESCAPE) {
                    stage.close()
                }
            }
        }
    }

    private fun navigateToSelectedSuite() {
        val searchResult = searchResultsView.selectionModel.selectedItem
        MainForm.INSTANCE.apply {
            keepTabSelection = true
            selectProgrammatically(searchResult.suite)
            tabs.selectionModel.select(tabTextEdit)
        }
        SnowCodeAreaProvider.getTextEditCodeArea(searchResult.suite).content.apply {
            clearStyle(0, text.length)
            selectRange(searchResult.anchor, searchResult.anchor + searchTextField.text.length)
            requestFollowCaret()
        }
        stage.close()
    }

    /**
     * VERY naive and simple full-text search.
     */
    private fun fullTextSearch(searchPhrase: String) {
        val results: MutableList<SearchResult> = LinkedList()
        MainForm.INSTANCE.rootElement.childrenRecursively.forEach { highElement ->
            if (highElement is Suite) {
                if (highElement.contents?.contains(searchPhrase, ignoreCase = true) == true) {
                    var globalIndex = 0
                    highElement.contents.lines().forEachIndexed { lineIndex, line ->
                        var index = 0
                        while (true) {
                            val anchor = line.indexOf(searchPhrase, ignoreCase = true, startIndex = index)
                            if (anchor < 0) break
                            results.add(SearchResult(highElement, line, lineIndex + 1, anchor, globalIndex + anchor))
                            index += anchor + searchPhrase.length
                        }
                        globalIndex += line.length + 1
                    }
                }
            }
        }

        results.groupBy { it.file.name }
            .forEach { (_, group) ->
                if (group.size > 1 && group.groupBy { it.file }.size > 1) {
                    group.forEach { it.isNameUnique = false }
                }
            }

        for (result in results) {
            searchResultsView.items.add(result)
            searchResultsView.selectionModel.select(0)
        }
    }

    fun setSearchPhrase(searchPhrase: String) {
        searchTextField.text = searchPhrase
    }

    fun show() {
        stage.show()
        searchTextField.requestFocus()
    }
}


package cz.hudecekpetr.snowride.ui

import cz.hudecekpetr.snowride.filesystem.LastChangeKind
import cz.hudecekpetr.snowride.tree.highelements.HighElement
import cz.hudecekpetr.snowride.tree.highelements.Suite
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.LineNumberFactory
import java.util.concurrent.ConcurrentHashMap


object SnowCodeAreaProvider {
    private val nonEditableCodeArea = SnowCodeArea(null)
    val nonEditableCodeAreaPane = VirtualizedScrollPane(nonEditableCodeArea)
    var codeArea: SnowCodeArea = nonEditableCodeArea

    private val textEditCodeAreaMap: MutableMap<HighElement, VirtualizedScrollPane<SnowCodeArea>> = ConcurrentHashMap()
    private val previewCodeAreaMap: MutableMap<HighElement, VirtualizedScrollPane<SnowCodeArea>> = ConcurrentHashMap()

    init {
        nonEditableCodeArea.paragraphGraphicFactory = LineNumberFactory.get(nonEditableCodeArea)
        VBox.setVgrow(nonEditableCodeAreaPane, Priority.ALWAYS)
        nonEditableCodeArea.isEditable = false
    }

    fun getTextEditCodeArea(suite: Suite): VirtualizedScrollPane<SnowCodeArea> {
        val textEditCodeAreaPane = textEditCodeAreaMap.getOrPut(suite) { createCodeArea(suite) }
        textEditCodeAreaPane.content.reload()
        codeArea = textEditCodeAreaPane.content
        return textEditCodeAreaPane
    }

    fun getPreviewCodeArea(suite: Suite): VirtualizedScrollPane<SnowCodeArea> {
        val previewCodeAreaPane = previewCodeAreaMap.getOrPut(suite) { createCodeArea(suite) }
        previewCodeAreaPane.content.reload()
        return previewCodeAreaPane
    }

    private fun createCodeArea(suite: Suite): VirtualizedScrollPane<SnowCodeArea> {
        val codeArea = SnowCodeArea(suite)
        codeArea.textProperty().addListener { _, _, newValue ->
            if (!MainForm.INSTANCE.switchingTextEditContents && !codeArea.reloading) {
                val element = codeArea.highElement
                if (element is Suite) {
                    element.areTextChangesUnapplied = true
                    element.contents = element.newlineStyle.convertToStyle(newValue)
                    MainForm.INSTANCE.changeOccurredTo(element, LastChangeKind.TEXT_CHANGED)
                }
            }
        }
        val pane = VirtualizedScrollPane(codeArea)
        codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)
        VBox.setVgrow(pane, Priority.ALWAYS)
        return pane
    }

    fun clear() {
        textEditCodeAreaMap.clear()
        previewCodeAreaMap.clear()
    }

    fun replaceHighElement(new: Suite, old: HighElement) {
        textEditCodeAreaMap.replaceHighElement(old, new)
        previewCodeAreaMap.replaceHighElement(old, new)
    }

    private fun MutableMap<HighElement, VirtualizedScrollPane<SnowCodeArea>>.replaceHighElement(old: HighElement, new: Suite) {
        textEditCodeAreaMap[old]?.let { scrollPane ->
            scrollPane.content.highElement = new
            remove(old)
            put(new, scrollPane)
        }
    }
}
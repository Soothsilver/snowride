package cz.hudecekpetr.snowride.ui

import cz.hudecekpetr.snowride.tree.LogicalLine
import cz.hudecekpetr.snowride.ui.SnowTableArrowSelectionHelper.SelectionAnchorDirection.HORIZONTAL
import cz.hudecekpetr.snowride.ui.SnowTableArrowSelectionHelper.SelectionAnchorDirection.VERTICAL
import javafx.scene.control.TablePosition
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent

class SnowTableArrowSelectionHelper {

    private lateinit var selectionAnchor: TablePosition<Any, Any>
    private var horizontalDirection: KeyCode? = null
    private var verticalDirection: KeyCode? = null
    private var manualSelectionInProgress = false

    var minColumn: Int = 0
    var maxColumn: Int = 0
    var minRow: Int = 0
    var maxRow: Int = 0

    private enum class SelectionAnchorDirection {
        HORIZONTAL, VERTICAL
    }

    fun selectionModelChange(selectionModel: TableView.TableViewSelectionModel<LogicalLine>) {
        if (manualSelectionInProgress) {
            return
        }
        // reset selection anchor
        if (selectionModel.selectedCells.size == 1) {
            selectionAnchor = selectionModel.selectedCells[0]
            horizontalDirection = null
            verticalDirection = null
        }
    }

    fun onKeyPressed(keyEvent: KeyEvent, selectionModel: TableView.TableViewSelectionModel<LogicalLine>) {
        if (keyEvent.isShiftDown && keyEvent.code in listOf(LEFT, RIGHT, UP, DOWN)) {

            val direction = if (keyEvent.code in listOf(LEFT, RIGHT)) HORIZONTAL else VERTICAL

            // reset selection anchor
            if (selectionModel.selectedCells.size == 1) {
                selectionAnchor = selectionModel.selectedCells[0]
                horizontalDirection = if (direction == HORIZONTAL) keyEvent.code else null
                verticalDirection = if (direction == VERTICAL) keyEvent.code else null
            } else {
                if (horizontalDirection == null || verticalDirection == null) {
                    recalculateSelectionIndexes(selectionModel)
                    if (verticalDirection == null && minRow != maxRow) {
                        verticalDirection = if (selectionAnchor.row == minRow) DOWN else UP
                    }
                    if (horizontalDirection == null && minColumn != maxColumn) {
                        horizontalDirection = if (selectionAnchor.column == minColumn) RIGHT else LEFT
                    }
                }
            }
            // decide on the direction in which the selection is growing
            horizontalDirection = if (horizontalDirection == null && direction == HORIZONTAL) keyEvent.code else horizontalDirection
            verticalDirection = if (verticalDirection == null && direction == VERTICAL) keyEvent.code else verticalDirection


            recalculateSelectionIndexes(selectionModel)

            // decide what to do now
            if (direction == HORIZONTAL) {
                if (horizontalDirection == keyEvent.code) {
                    if (keyEvent.code == RIGHT) maxColumn++ else minColumn--
                } else {
                    if (keyEvent.code == RIGHT) minColumn++ else maxColumn--
                }
            } else {
                if (verticalDirection == keyEvent.code) {
                    if (keyEvent.code == UP) minRow-- else maxRow++
                } else {
                    if (keyEvent.code == UP) maxRow-- else minRow++
                }
            }

            // change horizontal/vertical direction when necessary
            if (minColumn > maxColumn) {
                minColumn = maxColumn.also { maxColumn = minColumn }
                horizontalDirection = keyEvent.code
            }
            if (minRow > maxRow) {
                minRow = maxRow.also { maxRow = minRow }
                verticalDirection = keyEvent.code
            }

            // clear selection and select necessary cells
            manualSelectionInProgress = true
            selectionModel.clearSelection()
            for (col in minColumn..maxColumn) {
                for (row in minRow..maxRow) {
                    if (col < selectionModel.tableView.columns.size) {
                        selectionModel.select(row, selectionModel.tableView.columns[col])
                    }
                }
            }
            manualSelectionInProgress = false

            keyEvent.consume()
        }
    }

    private fun recalculateSelectionIndexes(selectionModel: TableView.TableViewSelectionModel<LogicalLine>) {
        // get min/max row/column indexes of currently selected cells
        minColumn = selectionAnchor.column
        maxColumn = selectionAnchor.column
        minRow = selectionAnchor.row
        maxRow = selectionAnchor.row
        selectionModel.selectedCells.forEach {
            minColumn = if (it.column < minColumn) it.column else minColumn
            maxColumn = if (it.column > maxColumn) it.column else maxColumn
            minRow = if (it.row < minRow) it.row else minRow
            maxRow = if (it.row > maxRow) it.row else maxRow
        }
    }
}
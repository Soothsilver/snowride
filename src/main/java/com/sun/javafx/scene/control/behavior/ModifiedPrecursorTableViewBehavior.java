package com.sun.javafx.scene.control.behavior;

import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;

/**
 * Copied from JDK under GPL2 and modified. We have it in the original package as opposed to cz.hudecekpetr.snowride
 * because it uses some package-private stuff.
 *
 * The point of modifications in here is to allow Shift+arrow keys to select a region of cells as though you were
 * in Excel. By default, the TableView uses a weird kind of snake-like multiselect which we don't want. We want
 * a blocky select that people are used to.
 */
public class ModifiedPrecursorTableViewBehavior<T> extends PrecursorTableViewBehavior<T> {
    /**************************************************************************
     *                                                                        *
     * Constructors                                                           *
     *                                                                        *
     ************************************************************************
     * @param control*/
    public ModifiedPrecursorTableViewBehavior(TableView<T> control) {
        super(control);
    }

    @Override
    protected void updateCellVerticalSelection(int delta, Runnable defaultAction) {
        TableSelectionModel sm = getSelectionModel();
        if (sm == null || sm.getSelectionMode() == SelectionMode.SINGLE) return;

        TableFocusModel fm = getFocusModel();
        if (fm == null) return;

        final TablePositionBase focusedCell = getFocusedCell();
        final int focusedCellRow = focusedCell.getRow();

        if (isShiftDown && sm.isSelected(focusedCellRow + delta, focusedCell.getTableColumn())) {
            int newFocusOwner = focusedCellRow + delta;

            // work out if we're backtracking
            boolean backtracking = false;
            if (selectionHistory.size() >= 2) {
                TablePositionBase<TableColumn<T, ?>> secondToLastSelectedCell = selectionHistory.get(1);
                backtracking = secondToLastSelectedCell.getRow() == newFocusOwner &&
                        secondToLastSelectedCell.getColumn() == focusedCell.getColumn();
            }

            // if the selection path has deviated from the anchor row / column, then we need to see if we're moving
            // backwards to the previous selection or not (as it determines what cell row we clear out)
            int cellRowToClear = selectionPathDeviated ?
                    (backtracking ? focusedCellRow : newFocusOwner) :
                    focusedCellRow;

            sm.clearSelection(cellRowToClear, focusedCell.getTableColumn());
            fm.focus(newFocusOwner, focusedCell.getTableColumn());
        } else if (isShiftDown && getAnchor() != null   ) { // i don't care about deviation
            int newRow = fm.getFocusedIndex() + delta;

            // we don't let the newRow go outside the bounds of the data
            newRow = Math.max(Math.min(getItemCount() - 1, newRow), 0);

            int start = Math.min(getAnchor().getRow(), newRow);
            int end = Math.max(getAnchor().getRow(), newRow);

            if (sm.getSelectedIndices().size() > 1) {
                clearSelectionOutsideRange(start, end, focusedCell.getTableColumn());
            }

            for (int _row = start; _row <= end; _row++) {
                if (sm.isSelected(_row, focusedCell.getTableColumn())) {
                    continue;
                }
                // Our modification here: select entire block
                int anchorX = getAnchor().getColumn();
                int ourX = focusedCell.getColumn();
                int fordelta = anchorX <= ourX ? 1 : -1;
                for (int i = anchorX;  i != ourX; i += fordelta) {
                    sm.select(_row, getColumn(i));
                }
                sm.select(_row, focusedCell.getTableColumn());
            }
            fm.focus(newRow, focusedCell.getTableColumn());
        } else {
            final int focusIndex = fm.getFocusedIndex();
            if (! sm.isSelected(focusIndex, focusedCell.getTableColumn())) {
                sm.select(focusIndex, focusedCell.getTableColumn());
            }
            defaultAction.run();
        }
    }

    @Override
    protected void updateCellHorizontalSelection(int delta, Runnable defaultAction) {
        TableSelectionModel sm = getSelectionModel();
        if (sm == null || sm.getSelectionMode() == SelectionMode.SINGLE) return;

        TableFocusModel fm = getFocusModel();
        if (fm == null) return;

        final TablePositionBase focusedCell = getFocusedCell();
        if (focusedCell == null || focusedCell.getTableColumn() == null) return;

        boolean atEnd = false;
        TableColumnBase adjacentColumn = getColumn(focusedCell.getTableColumn(), delta);
        if (adjacentColumn == null) {
            // if adjacentColumn is null, we use the focusedCell column, as we are
            // most probably at the very beginning or end of the row
            adjacentColumn = focusedCell.getTableColumn();
            atEnd = true;
        }

        final int focusedCellRow = focusedCell.getRow();

        if (isShiftDown && sm.isSelected(focusedCellRow, adjacentColumn)) {
            if (atEnd) {
                return;
            }

            // work out if we're backtracking
            boolean backtracking = false;
            ObservableList<? extends TablePositionBase> selectedCells = getSelectedCells();
            if (selectedCells.size() >= 2) {
                TablePositionBase<TableColumn<T, ?>> secondToLastSelectedCell = selectedCells.get(selectedCells.size() - 2);
                backtracking = secondToLastSelectedCell.getRow() == focusedCellRow &&
                        secondToLastSelectedCell.getTableColumn().equals(adjacentColumn);
            }

            // if the selection path has deviated from the anchor row / column, then we need to see if we're moving
            // backwards to the previous selection or not (as it determines what cell column we clear out)
            TableColumnBase<?,?> cellColumnToClear = selectionPathDeviated ?
                    (backtracking ? focusedCell.getTableColumn() : adjacentColumn) :
                    focusedCell.getTableColumn();

            sm.clearSelection(focusedCellRow, cellColumnToClear);
            fm.focus(focusedCellRow, adjacentColumn);
        } else if (isShiftDown && getAnchor() != null) {
            final int anchorColumn = getAnchor().getColumn();

            // we don't let the newColumn go outside the bounds of the data
            int newColumn = getVisibleLeafIndex(focusedCell.getTableColumn()) + delta;
            newColumn = Math.max(Math.min(getVisibleLeafColumns().size() - 1, newColumn), 0);

            int start = Math.min(anchorColumn, newColumn);
            int end = Math.max(anchorColumn, newColumn);




            for (int _col = start; _col <= end; _col++) {

                // Our modification here: select entire block
                int anchorY = getAnchor().getRow();
                int ourY = focusedCell.getRow();
                int fordelta = anchorY <= ourY ? 1 : -1;
                for (int i = anchorY;  i != ourY; i += fordelta) {
                    sm.select(i, getColumn(_col));
                }


                sm.select(focusedCell.getRow(), getColumn(_col));
            }
            fm.focus(focusedCell.getRow(), getColumn(newColumn));
        } else {
            defaultAction.run();
        }
    }
}

package cz.hudecekpetr.snowride.lexer;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LogicalLine {
    public String preTrivia = "";
    public List<Cell> cells = new ArrayList<>();
    private List<SimpleObjectProperty<Cell>> wrappers = new ArrayList<>();
    public HighElement belongsToHighElement;
    public PositionInListProperty lineNumber;

    public static LogicalLine fromEmptyLine(String text) {
        LogicalLine line = new LogicalLine();
        text = Extensions.removeFinalNewlineIfAny(text);
        if (text.trim().startsWith("#")) {
            // TODO not perfect, probably better dealt with at grammar/lexer level:
            if (text.startsWith("  ") || text.startsWith("\t") || text.startsWith(" \t")) {
                line.cells.add(new Cell("", text.substring(0, text.indexOf('#')), line));
            }
            line.cells.add(new Cell(text.trim(), "", line));
        }
        else {
            line.cells.add(new Cell("", text, line));
        }
        return line;
    }

    public LogicalLine prepend(String cellspace, String cell) {
        cells.add(0, new Cell("", cellspace, this));
        cells.add(1, new Cell(cell, this.preTrivia, this));
        this.preTrivia = "";
        return this;
    }
    public LogicalLine prepend(String cell) {
        cells.add(0, new Cell(cell, this.preTrivia, this));
        this.preTrivia = "";
        return this;
    }

    public void serializeInto(StringBuilder sb) {
        int lastNonVirtualCell = getLastNonVirtualCell();

        for (int i = 0; i < lastNonVirtualCell + 1; i++) {
            Cell cell = cells.get(i);
            sb.append(cell.contents);
            if (i != lastNonVirtualCell || !StringUtils.isBlank(cell.postTrivia)) {
                sb.append(cell.postTrivia);
            }
        }
        sb.append("\n");
    }

    private int getLastNonVirtualCell() {
        int lastNonVirtualCell = -1;
        for (int i = cells.size() - 1; i >= 0; i--) {
            if (!cells.get(i).virtual && (!StringUtils.isBlank(cells.get(i).postTrivia) || !StringUtils.isBlank(cells.get(i).contents))) {
                lastNonVirtualCell = i;
                break;
            }
        }
        return lastNonVirtualCell;
    }


    public SimpleObjectProperty<Cell> getCellAsStringProperty(int cellIndex, MainForm mainForm) {
        while (cells.size() <= cellIndex) {
            if (cells.size() > 0) {
                cells.get(cells.size() - 1).postTrivia = "    ";
            }
            Cell cell = new Cell("", "    ", this);
            cell.virtual = true;
            cells.add(cell);
        }
        while (cells.size() > wrappers.size()) {
            int index = wrappers.size();
            SimpleObjectProperty<Cell> wrapper = new SimpleObjectProperty<>();
            wrapper.addListener(new ChangeListener<Cell>() {
                @Override
                public void changed(ObservableValue<? extends Cell> observable, Cell oldValue, Cell newValue) {
                    String previousValue = cells.get(index).contents;
                    cells.set(index, newValue);
                    if (belongsToHighElement != null && !previousValue.equals(newValue.contents)) {
                        belongsToHighElement.markAsStructurallyChanged(mainForm);
                    }
                }
            });
            wrappers.add(wrapper);
        }
        wrappers.get(cellIndex).set(cells.get(cellIndex));
        return wrappers.get(cellIndex);
    }

    public boolean isFullyVirtual() {

        int lastNonVirtualCell = getLastNonVirtualCell();
        if (lastNonVirtualCell == -1) {
            // It's a virtual row.
            return true;
        } else {
            return false;
        }

    }
}

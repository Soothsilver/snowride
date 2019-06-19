package cz.hudecekpetr.snowride.lexer;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.fx.grid.SnowTableKind;
import cz.hudecekpetr.snowride.semantics.CellSemantics;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.SectionKind;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LogicalLine {
    public String preTrivia = "";
    public List<Cell> cells = new ArrayList<>();
    public PositionInListProperty lineNumber;
    public SnowTableKind belongsWhere;
    private HighElement belongsToHighElement;
    private List<SimpleObjectProperty<Cell>> wrappers = new ArrayList<>();

    public static LogicalLine fromEmptyLine(String text) {
        LogicalLine line = new LogicalLine();
        text = Extensions.removeFinalNewlineIfAny(text);
        if (text.trim().startsWith("#")) {
            // TODO not perfect, probably better dealt with at grammar/lexer level:
            if (text.startsWith("  ") || text.startsWith("\t") || text.startsWith(" \t")) {
                line.cells.add(new Cell("", text.substring(0, text.indexOf('#')), line));
            }
            line.cells.add(new Cell(text.trim(), "", line));
        } else {
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
                    recalcStyles();


                    if (getBelongsToHighElement() != null && !previousValue.equals(newValue.contents)) {
                        getBelongsToHighElement().markAsStructurallyChanged(mainForm);
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


    public void recalcStyles() {
        recalculateSemantics();
        for (Cell cell : cells) {
            cell.updateStyle();
        }
    }

    public void shiftTrueCellsRight(MainForm mainForm) {
        int cellcount = cells.size();
        for (int i = cellcount - 1; i >= 1; i--) {
            getCellAsStringProperty(i + 1, mainForm).set(cells.get(i).copy());
        }
    }

    public void shiftTrueCellsLeft(MainForm mainForm) {
        int cellcount = cells.size();
        for (int i = 2; i <= cellcount; i++) {
            if (i == cellcount) {
                getCellAsStringProperty(i - 1, mainForm).set(new Cell("", "    ", this));
            } else {
                getCellAsStringProperty(i - 1, mainForm).set(cells.get(i).copy());
            }
        }
    }

    public void recalculateSemantics() {

        boolean thereHasBeenNoGuaranteedKeywordCellYet = true;
        boolean isInScenario = getBelongsToHighElement() instanceof Scenario;
        boolean skipFirst = isInScenario;
        int indexOfThisAsArgument = 0; // value before first keyword is not relevant
        boolean everythingIsAComment = false;
        SnowTableKind kind = isInScenario ? SnowTableKind.SCENARIO : SnowTableKind.SETTINGS;
        IKnownKeyword currentKeyword = null;
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            CellSemantics cellSemantics = new CellSemantics(i);
            cell.setSemantics(cellSemantics);
            indexOfThisAsArgument++;
            if (isInScenario && ((Scenario) getBelongsToHighElement()).semanticsIsTemplateTestCase) {
                thereHasBeenNoGuaranteedKeywordCellYet = false;
            }
            if (everythingIsAComment) {
                cellSemantics.isComment = true;
                continue;
            }
            if (skipFirst) {
                // Cell number "0" is always empty in scenarios.
                skipFirst = false;
                continue;
            }
            if (cell.contents.startsWith("#")) {
                everythingIsAComment = true;
                cellSemantics.isComment = true;
                continue;
            }

            cellSemantics.argumentStatus = Cell.ArgumentStatus.UNKNOWN;
            if (currentKeyword != null) {
                int maxMandatory = currentKeyword.getNumberOfMandatoryArguments();
                int maxOptional = currentKeyword.getNumberOfOptionalArguments() + maxMandatory;
                if (indexOfThisAsArgument >= 0) {
                    if (indexOfThisAsArgument < maxMandatory) {
                        cellSemantics.argumentStatus = Cell.ArgumentStatus.MANDATORY;
                    } else if (indexOfThisAsArgument < maxOptional) {
                        cellSemantics.argumentStatus = Cell.ArgumentStatus.VARARG;
                    } else {
                        cellSemantics.argumentStatus = Cell.ArgumentStatus.FORBIDDEN;
                    }
                }
            }

            boolean isCertainlyNotAKeyword = isInScenario && (cell.contents.startsWith("${") || cell.contents.startsWith("@{") || cell.contents.startsWith("&{") || cell.contents.trim().equals("\\"));
            boolean canKeywordBeHere = thereHasBeenNoGuaranteedKeywordCellYet || (currentKeyword != null && currentKeyword.getArgumentIndexOfKeywordArgument() == indexOfThisAsArgument);
            if (canKeywordBeHere) {
                if (isCertainlyNotAKeyword) {
                    // Don't prevent further cells from being a keyword.
                } else {
                    cellSemantics.isKeyword = true;
                    thereHasBeenNoGuaranteedKeywordCellYet = false;
                }
                // This is the keyword.
                cellSemantics.permissibleKeywords = getBelongsToHighElement().asSuite().getKeywordsPermissibleInSuite();
                cellSemantics.permissibleKeywordsByInvariantName = getBelongsToHighElement().asSuite().getKeywordsPermissibleInSuiteByInvariantName();
                Collection<IKnownKeyword> homonyms = cellSemantics.permissibleKeywordsByInvariantName.get(Extensions.toInvariant(cell.contents));
                for (IKnownKeyword homonym : homonyms) {
                    if (homonym.isLegalInContext(cellSemantics.cellIndex, kind)) {
                        cellSemantics.thisHereKeyword = homonym;
                    }
                }
                currentKeyword = cellSemantics.thisHereKeyword;
                indexOfThisAsArgument = -1;
            }


        }

    }

    public HighElement getBelongsToHighElement() {
        return belongsToHighElement;
    }

    public void setBelongsToHighElement(HighElement belongsToHighElement) {
        this.belongsToHighElement = belongsToHighElement;
    }

    public void reformat(SectionKind sectionKind) {
        for (int i = cells.size() - 1; i >= 0; i--) {
            Cell cell = cells.get(i);
            if (StringUtils.isBlank(cell.contents) && StringUtils.isBlank(cell.postTrivia)) {
                if (wrappers.size() == cells.size()) {
                    wrappers.remove(i);
                }
                cells.remove(i);
                continue;
            } else {
                break;
            }
        }
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            if (StringUtils.isBlank(cell.postTrivia)) {
                if (i == cells.size() - 1) {
                    cell.postTrivia = "";
                } else {
                    if (i == 0 && sectionKind == SectionKind.SETTINGS) {
                        cell.postTrivia = StringUtils.repeat(' ', 18 - cell.contents.length());
                    } else {
                        cell.postTrivia = "    ";
                    }
                }
            }
        }
    }
}

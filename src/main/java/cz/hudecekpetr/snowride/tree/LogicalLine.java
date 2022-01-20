package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.errors.ErrorKind;
import cz.hudecekpetr.snowride.errors.SnowrideError;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import javafx.collections.ObservableList;
import org.robotframework.jaxb.BodyItemStatusValue;
import org.robotframework.jaxb.For;
import org.robotframework.jaxb.ForIteration;
import org.robotframework.jaxb.Keyword;
import cz.hudecekpetr.snowride.semantics.CellSemantics;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.QualifiedKeyword;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.sections.SectionKind;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.ui.grid.SnowTableKind;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Severity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static cz.hudecekpetr.snowride.Extensions.toInvariant;

import static cz.hudecekpetr.snowride.semantics.RobotFrameworkVariableUtils.getVariableName;
import static cz.hudecekpetr.snowride.semantics.RobotFrameworkVariableUtils.isVariable;

/**
 * Represents a line in Robot Framework code if it's part of a settings table, variables table or a scenario. One logical
 * line can be on multiple consecutive physical lines if they're joined by the ellipsis (...).  A logical line consists
 * of cells.
 */
public class LogicalLine {
    public String preTrivia = "";
    /**
     * Cells of this line. The first cell (cell 0) starts at column 0. If there's white space at column 0, then
     * cell 0 is empty and the line actually begins with the cell zero's post-trivia.
     */
    public List<Cell> cells = new ArrayList<>();
    public Cell lineNumberCell;
    public PositionInListProperty lineNumber;
    public SnowTableKind belongsWhere;
    private HighElement belongsToHighElement;
    private List<SimpleObjectProperty<Cell>> wrappers = new ArrayList<>();

    // output.xml related fields
    public BodyItemStatusValue status;
    public boolean doesNotMatch;
    public Keyword keyword;
    public List<String> keywordArguments;
    public List<ForIteration> forIterations;
    public For forLoop;

    public static LogicalLine createEmptyLine(SnowTableKind snowTableKind, HighElement highElement, ObservableList<LogicalLine> list) {
        LogicalLine newLine = new LogicalLine();
        newLine.setBelongsToHighElement(highElement);
        newLine.lineNumber = new PositionInListProperty<>(newLine, list);
        newLine.belongsWhere = snowTableKind;
        newLine.recalcStyles();
        return newLine;
    }

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
            wrapper.addListener((observable, oldValue, newValue) -> {
                String previousValue = cells.get(index).contents;
                cells.set(index, newValue);
                recalcStyles();


                if (getBelongsToHighElement() != null && !previousValue.equals(newValue.contents)) {
                    getBelongsToHighElement().markAsStructurallyChanged(mainForm);
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
        int cellCount = cells.size();
        for (int i = cellCount - 1; i >= 1; i--) {
            getCellAsStringProperty(i + 1, mainForm).set(cells.get(i).copy());
        }
    }

    public void shiftTrueCellsLeft(MainForm mainForm) {
        int cellCount = cells.size();
        for (int i = 2; i <= cellCount; i++) {
            if (i == cellCount) {
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
        boolean ignoreEverythingFromNowOn = false;
        IKnownKeyword currentKeyword = null;
        boolean isTemplate = isInScenario && ((Scenario) getBelongsToHighElement()).semanticsIsTemplateTestCase;
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            CellSemantics cellSemantics = new CellSemantics(i);
            cell.setSemantics(cellSemantics);
            cellSemantics.variablesList = getBelongsToHighElement().getVariablesList();
            indexOfThisAsArgument++;
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
            if (cell.contents.equals("IF") || cell.contents.equals("ELSE") || cell.contents.equals("ELSE IF")) {
                ignoreEverythingFromNowOn = true;
            }
            if (currentKeyword != null && !ignoreEverythingFromNowOn) {
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

            boolean isVariable = isVariable(cell.contents);
            boolean isCertainlyNotAKeyword = isInScenario && (isVariable || cell.contents.trim().equals("\\"));
            boolean canKeywordBeHere = thereHasBeenNoGuaranteedKeywordCellYet || (currentKeyword != null && currentKeyword.getArgumentIndexOfKeywordArgument() == indexOfThisAsArgument);
            if (canKeywordBeHere) {

                // This is the keyword.
                cellSemantics.permissibleKeywords = getBelongsToHighElement().asSuite().getKeywordsPermissibleInSuite();
                cellSemantics.permissibleKeywordsByInvariantName = getBelongsToHighElement().asSuite().getKeywordsPermissibleInSuiteByInvariantName();
                Collection<IKnownKeyword> homonyms = cellSemantics.permissibleKeywordsByInvariantName.get(toInvariant(cell.contents));
                if (homonyms != null) {
                    for (IKnownKeyword homonym : homonyms) {
                        if (homonym.isLegalInContext(cellSemantics.cellIndex, kind)) {
                            cellSemantics.thisHereKeyword = homonym;
                        }
                    }
                }
                if (cellSemantics.thisHereKeyword == null) {
                    determineThisHereKeywordWithAdvancedProcedures(cellSemantics, kind, cell.contents);
                }
                if (cellSemantics.thisHereKeyword == null) {
                    determineViaGherkin(cellSemantics, kind, cell.contents);
                }
                currentKeyword = cellSemantics.thisHereKeyword;
                indexOfThisAsArgument = -1;
                if (isTemplate) {
                    if (currentKeyword != null && currentKeyword.isTestCaseOption()) {
                        isTemplate = false; // ok, now highlight as normal
                    } else {
                        // prevent highlight
                        isCertainlyNotAKeyword = true;
                    }
                }
                if (isCertainlyNotAKeyword) {
                    if (isVariable) {
                        getBelongsToHighElement().variables.add(getVariableName(cell.contents));
                        cellSemantics.isVariable = true;
                    }
                    // Don't prevent further cells from being a keyword.
                } else {
                    cellSemantics.isKeyword = true;
                    thereHasBeenNoGuaranteedKeywordCellYet = false;
                }
            }


        }

    }

    private void determineViaGherkin(CellSemantics cellSemantics, SnowTableKind kind, String cellContents) {
        int firstSpace = cellContents.indexOf(' ');
        if (firstSpace != -1) {
            String prefix = cellContents.substring(0, firstSpace);
            if (prefix.equalsIgnoreCase("Given") ||
                    prefix.equalsIgnoreCase("When") ||
                    prefix.equalsIgnoreCase("Then") ||
                    prefix.equalsIgnoreCase("And") ||
                    prefix.equalsIgnoreCase("But")) {
                String afterPrefix = cellContents.substring(firstSpace + 1);
                Collection<IKnownKeyword> homonyms = cellSemantics.permissibleKeywordsByInvariantName.get(toInvariant(afterPrefix));
                for (IKnownKeyword homonym : homonyms) {
                    if (homonym.isLegalInContext(cellSemantics.cellIndex, kind)) {
                        cellSemantics.thisHereKeyword = homonym;
                    }
                }
                if (cellSemantics.thisHereKeyword == null) {
                    determineThisHereKeywordWithAdvancedProcedures(cellSemantics, kind, afterPrefix);
                }
            }
        }
    }

    private void determineThisHereKeywordWithAdvancedProcedures(CellSemantics cellSemantics, SnowTableKind kind, String cellContents) {
        QualifiedKeyword qualifiedKeyword = QualifiedKeyword.fromDottedString(cellContents);
        if (qualifiedKeyword.getSource() != null) {
            Collection<IKnownKeyword> homonyms = cellSemantics.permissibleKeywordsByInvariantName.get(toInvariant(qualifiedKeyword.getKeyword()));
            if (homonyms != null) {
                for (IKnownKeyword homonym : homonyms) {
                    if (homonym.isLegalInContext(cellSemantics.cellIndex, kind)) {
                        String sourceName = homonym.getSourceName();
                        if (!StringUtils.isEmpty(sourceName) && toInvariant(sourceName).equals(toInvariant(qualifiedKeyword.getSource()))) {
                            cellSemantics.thisHereKeyword = homonym;
                            break;
                        }
                    }
                }
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

    public void addLineErrorsToOwner() {
        for (Cell cell : cells) {
            if (cell.getSemantics().isKeyword && cell.getSemantics().thisHereKeyword == null && !StringUtils.isBlank(cell.contents)) {
                belongsToHighElement.selfErrors.add(new SnowrideError(belongsToHighElement, ErrorKind.BAD_KEYWORD, Severity.WARNING, "'" + cell.contents + "' is not a known keyword."));
            }
            if (cell.getSemantics().argumentStatus == Cell.ArgumentStatus.FORBIDDEN && !cell.getSemantics().isComment && !StringUtils.isBlank(cell.contents)) {
                belongsToHighElement.selfErrors.add(new SnowrideError(belongsToHighElement, ErrorKind.TOO_MANY_ARGUMENTS, Severity.WARNING, "Too many arguments on line " + this.lineNumber.intValue() + "."));
            }
            if (cell.getSemantics().argumentStatus == Cell.ArgumentStatus.MANDATORY && StringUtils.isBlank(cell.contents)) {
                belongsToHighElement.selfErrors.add(new SnowrideError(belongsToHighElement, ErrorKind.MISSING_ARGUMENT, Severity.WARNING, "Not enough arguments on line " + this.lineNumber.intValue() + "."));
            }
        }
    }

    public boolean startsWith(String... anotherStrings) {
        List<String> lineArgs = cells.subList(1, cells.size()).stream()
//                .map(cell -> toInvariant(cell.contents).replaceAll("\\s*=\\s*$", ""))
                .map(cell -> toInvariant(cell.contents))
                .filter(StringUtils::isNotBlank)
                .filter(s -> !s.startsWith("#"))  // filter out comments
                .collect(Collectors.toList());
        return Arrays.stream(anotherStrings).anyMatch(s -> lineArgs.get(0).equalsIgnoreCase(s));
    }

    public boolean matchesKeyword(Keyword keyword) {
        // remove empty and comment cells
        List<String> cellsToMatch = cells.subList(1, cells.size()).stream()
                .map(cell -> cell.contents)
                .filter(StringUtils::isNotBlank)
                .filter(s -> !s.startsWith("#"))  // filter out comments
                .collect(Collectors.toList());

        // quick check
        if (cellsToMatch.size() != keyword.getVariables().size() + keyword.getArguments().size() + 1) {
            return false;
        }

        List<String> varsToMatch = cellsToMatch.subList(0, keyword.getVariables().size()).stream()
                .map(contents -> toInvariant(contents).replaceAll("\\s*=\\s*$", ""))
                .collect(Collectors.toList());
        if (!varsToMatch.equals(keyword.getVariables().stream().map(contents -> toInvariant(contents).replaceAll("\\s*=\\s*$", "")).collect(Collectors.toList()))) {
            return false;
        }

        int index = keyword.getVariables().size() == 0 ? 0 : 1;
        if (!toInvariant(cellsToMatch.get(index)).equals(toInvariant(keyword.getName()))) {
            return false;
        }

        if (keyword.getArguments().isEmpty() || cellsToMatch.subList(index + 1, cellsToMatch.size()).equals(keyword.getArguments())) {
            return true;
        }

        return false;
    }

    public void clearOutputFields() {
        status = null;
        doesNotMatch = false;
        keyword = null;
        keywordArguments = null;
        forIterations = null;
        forLoop = null;
    }
}

package cz.hudecekpetr.snowride.lexer;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.IHasQuickDocumentation;
import cz.hudecekpetr.snowride.fx.grid.SnowTableKind;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.TestCaseSettingOption;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Cell implements IHasQuickDocumentation {
    // Permanent fields:
    public final String contents;
    public String postTrivia;
    public LogicalLine partOfLine;
    // Fields via analysis:
    public boolean virtual;
    public boolean triggerDocumentationNext;
    public boolean isLineNumberCell;
    private boolean isComment;
    private boolean isKeyword;
    private int cellIndex;
    private List<IKnownKeyword> permissibleKeywords;
    private Map<String, IKnownKeyword> permissibleKeywordsByInvariantName;
    private SimpleStringProperty styleProperty = new SimpleStringProperty(null);

    public Cell(String contents, String postTrivia, LogicalLine partOfLine) {
        this.contents = contents;
        this.postTrivia = postTrivia;
        this.partOfLine = partOfLine;
    }

    public Cell copy() {
        Cell theCopy = new Cell(contents, postTrivia, partOfLine);
        theCopy.virtual = this.virtual;
        return theCopy;
    }

    @Override
    public String toString() {
        return contents;
    }

    public void updateStyle(int cellIndex) {
        String style = "-fx-padding: 0; -fx-background-insets: 0.0; ";
        if (isLineNumberCell) {
            style += "-fx-font-weight: bold; -fx-background-color: lavender; -fx-text-alignment: right; -fx-alignment: center; ";
        } else {
            if (partOfLine.belongsWhere.isScenario()) {
                style += getStyle();
            } else {
                if (cellIndex == 0) {
                    style += "-fx-font-weight: bold; ";
                    if (partOfLine.belongsWhere == SnowTableKind.SETTINGS) {
                        style += "-fx-text-fill: darkmagenta; ";
                    } else {
                        style += "-fx-text-fill: green; ";
                    }
                }
            }
        }
        styleProperty.set(style);
    }
    private String getStyle() {
        updateSemanticsStatus();
        String style = "";
        if (cellIndex == 1 && (contents.startsWith("[") && contents.endsWith("]"))) {
            style += "-fx-text-fill: darkmagenta; ";
            style += "-fx-font-weight: bold; ";
        } else if (cellIndex == 1 && contents.equals(": FOR") || contents.equals(":FOR") || contents.equals("FOR")) {
            style += "-fx-text-fill: darkmagenta; ";
            style += "-fx-font-weight: bold; ";
        } else if (cellIndex == 1 && contents.equals("\\")) {
            style += "-fx-font-style: italic;  -fx-background-color: darkgray; ";
        } else if (isComment) {
            style += "-fx-text-fill: brown; ";
        } else if (isKeyword) {
            style += "-fx-font-weight: bold; ";
            IKnownKeyword knownKeyword = permissibleKeywordsByInvariantName.get(Extensions.toInvariant(this.contents));
            if (knownKeyword != null) {
                if (knownKeyword.getScenarioIfPossible() != null) {
                    style += "-fx-text-fill: blue; ";
                } else {
                    style += "-fx-text-fill: dodgerblue; ";
                }
            }
        } else if (contents.contains("${") || contents.contains("@{") || contents.contains("&{")) {
            style += "-fx-text-fill: green; ";
        }
        return style;
    }

    private void updateSemanticsStatus() {
        isComment = false;
        cellIndex = partOfLine.cells.indexOf(this);
        boolean pastTheKeyword = false;
        isKeyword = false;
        boolean skipFirst = true;
        for (Cell cell : partOfLine.cells) {
            if (skipFirst) {
                skipFirst = false;
                continue;
            }
            isKeyword = false;
            if (cell.contents.startsWith("#")) {
                isComment = true;
                break;
            }
            if (cell.contents.startsWith("${") || cell.contents.startsWith("@{") || cell.contents.startsWith("&{")) {
                // Is the return value.
            } else if (!pastTheKeyword) {
                // This is the keyword.
                isKeyword = true;
                permissibleKeywords = partOfLine.belongsToHighElement.asSuite().getKeywordsPermissibleInSuite();
                permissibleKeywordsByInvariantName = partOfLine.belongsToHighElement.asSuite().getKeywordsPermissibleInSuiteByInvariantName();
                pastTheKeyword = true;
            }
            if (cell == this) {
                // The rest doesn't matter.
                break;
            }
        }
    }

    public Stream<? extends IAutocompleteOption> getCompletionOptions(SnowTableKind snowTableKind) {
        if (snowTableKind.isScenario()) {
            updateSemanticsStatus();
            Stream<IAutocompleteOption> options = Stream.empty();
            if (cellIndex == 1) {
                options = Stream.concat(options, TestCaseSettingOption.testCaseTableOptions.stream());
            }
            if (isKeyword) {
                options = Stream.concat(options, permissibleKeywords.stream());
            }
            return options;
        } else {
            cellIndex = partOfLine.cells.indexOf(this);
            if (cellIndex == 0 && snowTableKind == SnowTableKind.SETTINGS) {
                return TestCaseSettingOption.settingsTableOptions.stream();
            }
            return Stream.empty();
        }
    }

    public IKnownKeyword getKeywordInThisCell() {
        updateSemanticsStatus();
        if (isKeyword) {
            IKnownKeyword first = permissibleKeywordsByInvariantName.get(Extensions.toInvariant(this.contents));
            return first;
        }
        return null;
    }

    public boolean hasDocumentation() {
        return this.isKeyword;
    }

    @Override
    public Image getAutocompleteIcon() {
        if (isKeyword) {
            IKnownKeyword kw = getKeywordInThisCell();
            if (kw != null) {
                return kw.getAutocompleteIcon();
            }
        }
        return null;
    }

    @Override
    public String getQuickDocumentationCaption() {
        if (isKeyword) {
            IKnownKeyword kw = getKeywordInThisCell();
            if (kw != null) {
                return kw.getQuickDocumentationCaption();
            }
        }
        return null;
    }

    @Override
    public String getFullDocumentation() {
        if (isKeyword) {
            IKnownKeyword kw = getKeywordInThisCell();
            if (kw != null) {
                return kw.getFullDocumentation();
            }
        }
        return null;
    }

    @Override
    public String getItalicsSubheading() {
        if (isKeyword) {
            IKnownKeyword kw = getKeywordInThisCell();
            if (kw != null) {
                return kw.getItalicsSubheading();
            }
        }
        return null;
    }

    public SimpleStringProperty getStyleProperty() {
        return styleProperty;
    }
}

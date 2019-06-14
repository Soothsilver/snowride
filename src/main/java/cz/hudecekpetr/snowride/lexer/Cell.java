package cz.hudecekpetr.snowride.lexer;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.IHasQuickDocumentation;
import cz.hudecekpetr.snowride.fx.grid.SnowTableKind;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.TestCaseSettingOption;
import cz.hudecekpetr.snowride.semantics.resources.ImportedResource;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Cell implements IHasQuickDocumentation {
    // Permanent fields:
    public final String contents;
    public String postTrivia;
    public LogicalLine partOfLine;

    // Fields via Settings table analysis:
    public Suite leadsToSuite;

    // Fields via analysis:
    public boolean virtual;
    public boolean triggerDocumentationNext;
    public boolean isLineNumberCell;
    private boolean isComment;
    private boolean isKeyword;
    private ArgumentStatus argumentStatus;
    private int cellIndex;
    public IKnownKeyword keywordOfThisLine; // TODO to be reimplemented at the line level
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
        leadsToSuite = null;
        String style = "";//-fx-padding: 0; -fx-background-insets: 0.0; ";
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
                if (cellIndex == 1 && partOfLine.belongsWhere == SnowTableKind.SETTINGS) {
                    if (partOfLine.belongsToHighElement instanceof Suite) {
                        Suite asSuite = (Suite) partOfLine.belongsToHighElement;
                        Optional<ImportedResource> resource = asSuite.getImportedResources().stream().filter(ir -> ir.getName().equals(contents)).findFirst();
                        if (resource.isPresent()) {
                            if (resource.get().isSuccessfullyImported()) {
                                if (resource.get().getImportsSuite() != null) {
                                    style += "-fx-text-fill: blue; -fx-underline: true; -fx-font-weight: bold; ";
                                    leadsToSuite = resource.get().getImportsSuite();
                                } else {
                                    style += "-fx-text-fill: dodgerblue; ";
                                }
                            } else {
                                style += "-fx-text-fill: red; ";
                            }
                        }
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
            style += "-fx-font-style: italic; -fx-background-color: darkgray; ";
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
        style += "-fx-border-color: transparent #EDEDED #EDEDED transparent; -fx-border-width: 1px; ";
        switch (argumentStatus) {
            case FORBIDDEN:
                if (!isComment && !StringUtils.isBlank(contents)) {
                    style += "-fx-background-color: #ff7291; ";
                } else {
                    style += "-fx-background-color: #c0c0c0; ";
                }
                break;
            case VARARG:
                style += "-fx-background-color: #F5F5F5; ";
                break;
            case MANDATORY:
                style += "-fx-background-color: white; ";
                if (StringUtils.isBlank(contents)) {
                    style += "-fx-background-color: #ffcf32; ";
                }
                break;
        }
        return style;
    }

    public void updateSemanticsStatus() {
        keywordOfThisLine = null;
        isComment = false;
        cellIndex = partOfLine.cells.indexOf(this);
        boolean pastTheKeyword = false;
        isKeyword = false;
        boolean skipFirst = true;
        int indexOfThisAsArgument = -2;
        if ((partOfLine.belongsToHighElement instanceof Scenario) && (((Scenario) partOfLine.belongsToHighElement)).semanticsIsTemplateTestCase) {
            pastTheKeyword = true;
            indexOfThisAsArgument = -1;
        }
        for (Cell cell : partOfLine.cells) {
            if (indexOfThisAsArgument >= -1) {
                indexOfThisAsArgument++;
            }
            if (skipFirst) {
                skipFirst = false;
                continue;
            }
            isKeyword = false;
            if (cell.contents.startsWith("#")) {
                isComment = true;
                break;
            }
            if (cell.contents.startsWith("${") || cell.contents.startsWith("@{") || cell.contents.startsWith("&{") || cell.contents.trim().equals("\\")) {
                // TODO the last thing should only trigger a non-keyword if there is a nonempty cell afterwards still on the same row
                // Is the return value, or an indent due to the FOR loop.
            } else if (!pastTheKeyword) {
                // This is the keyword.
                isKeyword = true;
                permissibleKeywords = partOfLine.belongsToHighElement.asSuite().getKeywordsPermissibleInSuite();
                permissibleKeywordsByInvariantName = partOfLine.belongsToHighElement.asSuite().getKeywordsPermissibleInSuiteByInvariantName();
                keywordOfThisLine = permissibleKeywordsByInvariantName.get(Extensions.toInvariant(cell.contents));
                pastTheKeyword = true;
                indexOfThisAsArgument = -1;
            }
            if (cell == this) {
                // The rest doesn't matter.
                break;
            }
        }
        argumentStatus = ArgumentStatus.UNKNOWN;
        if (keywordOfThisLine != null) {
            int maxMandatory = keywordOfThisLine.getNumberOfMandatoryArguments();
            int maxOptional = keywordOfThisLine.getNumberOfOptionalArguments() + maxMandatory;
            if (indexOfThisAsArgument >= 0) {
                if (indexOfThisAsArgument < maxMandatory) {
                    argumentStatus = ArgumentStatus.MANDATORY;
                } else if (indexOfThisAsArgument < maxOptional) {
                    argumentStatus = ArgumentStatus.VARARG;
                } else {
                    argumentStatus = ArgumentStatus.FORBIDDEN;
                }
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

    private enum ArgumentStatus {
        MANDATORY,
        VARARG,
        FORBIDDEN,
        UNKNOWN
    }
}

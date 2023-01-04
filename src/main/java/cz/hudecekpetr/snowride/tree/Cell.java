package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.Underlining;
import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.ui.grid.IceCell;
import org.robotframework.jaxb.BodyItemStatusValue;
import cz.hudecekpetr.snowride.semantics.*;
import cz.hudecekpetr.snowride.semantics.codecompletion.GherkinEnhancedOption;
import cz.hudecekpetr.snowride.semantics.codecompletion.LibraryAutocompleteOption;
import cz.hudecekpetr.snowride.semantics.codecompletion.QualifiedCompletionOption;
import cz.hudecekpetr.snowride.semantics.resources.ImportedResource;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.grid.SnowTableKind;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import org.reactfx.collection.LiveArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cz.hudecekpetr.snowride.semantics.RobotFrameworkVariableUtils.*;
import static cz.hudecekpetr.snowride.ui.grid.YellowHighlight.lastPositionSelectText;

public class Cell implements IHasQuickDocumentation {
    

    private static final String CELL_STYLE_SETTINGS = "settingsCell";
    private static final String CELL_STYLE_NON_SETTINGS = "nonSettingsCell";
    private static final String CELL_STYLE_SUITE_IMPORT = "suiteImportCell";
    private static final String CELL_STYLE_OTHER_IMPORT = "otherImportCell";
    private static final String CELL_STYLE_FAILED_IMPORT = "failedImportCell";

    private static final String CELL_STYLE_RESERVED_KEYWORD = "reservedKeywordCell";
    private static final String CELL_STYLE_INDENTATION = "indentationCell";
    private static final String CELL_STYLE_COMMENT = "commentCell";
    private static final String CELL_STYLE_KEYWORD = "keywordCell";
    private static final String CELL_STYLE_SCENARIO_LINK = "scenarioLinkCell";
    private static final String CELL_STYLE_SCENARIO_LINK_ACTIVE = "scenarioLinkActiveCell";
    private static final String CELL_STYLE_KEYWORD_NO_LINK = "keywordNoLinkCell";

    private static final String CELL_STYLE_CONDITIONAL_KEYWORD = "conditionalKeywordCell";
    private static final String CELL_STYLE_VARIABLE = "variableCell";
    private static final String CELL_STYLE_VARIABLE_UNKNOWN = "variableUnknownOriginCell";
    private static final String CELL_STYLE_BORDERED = "borderedCell";
    private static final String CELL_STYLE_HIGHLIGHTED = "highlightedCell";
    private static final String CELL_STYLE_FORBIDDEN_ARGUMENT = "forbiddenArgumentCell";
    private static final String CELL_STYLE_BLANK_ARGUMENT = "blankArgumentCell";
    private static final String CELL_STYLE_VARIABLE_ARGUMENT = "variableArgumentCell";
    private static final String CELL_STYLE_MANDATORY_ARGUMENT = "mandatoryArgumentCell";
    private static final String CELL_STYLE_MANDATORY_ARGUMENT_MISSING = "missingMandatoryArgumentCell";

    public static List<String> interchangableCellStyles = Arrays.asList(CELL_STYLE_SETTINGS, CELL_STYLE_NON_SETTINGS,
        CELL_STYLE_SUITE_IMPORT, CELL_STYLE_OTHER_IMPORT, CELL_STYLE_FAILED_IMPORT, CELL_STYLE_RESERVED_KEYWORD,
        CELL_STYLE_INDENTATION, CELL_STYLE_COMMENT, CELL_STYLE_KEYWORD, CELL_STYLE_SCENARIO_LINK,
        CELL_STYLE_SCENARIO_LINK_ACTIVE, CELL_STYLE_KEYWORD_NO_LINK, CELL_STYLE_CONDITIONAL_KEYWORD,
        CELL_STYLE_VARIABLE, CELL_STYLE_VARIABLE_UNKNOWN, CELL_STYLE_BORDERED, CELL_STYLE_HIGHLIGHTED,
        CELL_STYLE_FORBIDDEN_ARGUMENT, CELL_STYLE_BLANK_ARGUMENT,
        CELL_STYLE_VARIABLE_ARGUMENT, CELL_STYLE_MANDATORY_ARGUMENT, CELL_STYLE_MANDATORY_ARGUMENT_MISSING);

    private static final String LINE_NUMBER_CELL_STYLE = "-fx-padding: 0; -fx-background-insets: 0.0; -fx-font-weight: bold;  -fx-alignment: center;";
    private static final String LINE_NUMBER_CELL_STYLE_DEFAULT = LINE_NUMBER_CELL_STYLE
            + "-fx-background-color: lavender";

    private static final List<String> CONDITIONAL_KEYWORDS = new ArrayList<>(Arrays.asList(": FOR", ":FOR", "FOR", "END", "IF", "ELSE IF", "ELSE"));

    // Permanent fields:
    public final String contents;
    public String postTrivia;
    public LogicalLine partOfLine;

    // Fields via Settings table analysis:
    public Suite leadsToSuite;

    // Fields via analysis:
    public boolean virtual;
    public boolean triggerDocumentationNext;
    public boolean triggerMessagesNext;
    public boolean isLineNumberCell;
    public BodyItemStatusValue status;
    
    private final ListProperty<String> cssStyleClassesProperty = new SimpleListProperty<>(  new LiveArrayList<>());
    
    public IceCell iceCell;
    private CellSemantics semantics;
    private ChangeListener<List<String>> recentChangeListener;

    public Cell(String contents, String postTrivia, LogicalLine partOfLine, boolean isLineNumberCell) {
        this.contents = contents;
        this.postTrivia = postTrivia;
        this.partOfLine = partOfLine;
        this.isLineNumberCell = isLineNumberCell;
    }

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

    public void updateLineNumberCellStyle() {
        if (iceCell != null) {
            iceCell.updateLineNumberCellStyle(this);
        }
    }

    public void updateStyle() {
        if (isLineNumberCell) {
            return;
        }
        leadsToSuite = null;
        List<String> styleClasses = new ArrayList<>();
        if (semantics.cellIndex == 0) {
            if (partOfLine.belongsWhere == SnowTableKind.SETTINGS) {
                styleClasses.add(CELL_STYLE_SETTINGS);
            } else {
                styleClasses.add(CELL_STYLE_NON_SETTINGS);
            }
        }
        if (semantics.cellIndex == 1 && partOfLine.belongsWhere == SnowTableKind.SETTINGS) {
            if (partOfLine.getBelongsToHighElement() instanceof Suite) {
                Suite asSuite = (Suite) partOfLine.getBelongsToHighElement();
                Optional<ImportedResource> resource = asSuite.getImportedResources().stream().filter(ir -> ir.getName().equals(contents)).findFirst();
                if (resource.isPresent()) {
                    if (resource.get().isSuccessfullyImported()) {
                        if (resource.get().getImportsSuite() != null) {
                            styleClasses.add(CELL_STYLE_SUITE_IMPORT);
                            leadsToSuite = resource.get().getImportsSuite();
                        } else {
                            styleClasses.add(CELL_STYLE_OTHER_IMPORT);
                        }
                    } else {
                        styleClasses.add(CELL_STYLE_FAILED_IMPORT);
                    }
                }
            }
        }
        if (semantics.cellIndex == 1 && (contents.startsWith("[") && contents.endsWith("]"))) {
            styleClasses.add(CELL_STYLE_RESERVED_KEYWORD);
        } else if (semantics.cellIndex == 1 && contents.equals("\\")) {
            styleClasses.add(CELL_STYLE_INDENTATION);
        } else if (semantics.isComment) {
            styleClasses.add(CELL_STYLE_COMMENT);
        } else if (semantics.isKeyword) {
            styleClasses.add(CELL_STYLE_KEYWORD);
            IKnownKeyword knownKeyword = semantics.thisHereKeyword;
            if (knownKeyword != null) {
                if (knownKeyword.getScenarioIfPossible() != null) {
                    styleClasses.add(CELL_STYLE_SCENARIO_LINK);
                    if (Underlining.getActiveCell() == this && Underlining.ctrlDown) {
                        styleClasses.add(CELL_STYLE_SCENARIO_LINK_ACTIVE);
                    }
                } else {
                    styleClasses.add(CELL_STYLE_KEYWORD_NO_LINK);
                }
            }
        } else if (semantics.cellIndex == 1 && CONDITIONAL_KEYWORDS.stream().anyMatch(contents::matches)) {
            styleClasses.add(CELL_STYLE_CONDITIONAL_KEYWORD);
        } else if (semantics.isVariable) {
            styleClasses.add(CELL_STYLE_VARIABLE);
        } else if (containsAnyVariable(contents)) {
            if (containsVariable(contents, partOfLine.getBelongsToHighElement().variables)) {
                styleClasses.add(CELL_STYLE_VARIABLE);
            } else {
                styleClasses.add(CELL_STYLE_VARIABLE_UNKNOWN);
            }
        }
        styleClasses.add(CELL_STYLE_BORDERED);
        if (!StringUtils.isBlank(contents) && Settings.getInstance().cbHighlightSameCells &&
                (contents.equals(lastPositionSelectText)
                        || (isVariable(lastPositionSelectText) && containsVariable(contents, getVariableName(lastPositionSelectText)))
                        || (containsVariable(lastPositionSelectText) && containsVariable(contents, getVariableName(lastPositionSelectText)))
                )
        ) {
            styleClasses.add(CELL_STYLE_HIGHLIGHTED);
        } else {
            switch (semantics.argumentStatus) {
                case FORBIDDEN:
                    if (!semantics.isComment && !StringUtils.isBlank(contents)) {
                        styleClasses.add(CELL_STYLE_FORBIDDEN_ARGUMENT);
                    } else {
                        styleClasses.add(CELL_STYLE_BLANK_ARGUMENT);
                    }
                    break;
                case VARARG:
                    styleClasses.add(CELL_STYLE_VARIABLE_ARGUMENT);
                    break;
                case MANDATORY:
                    styleClasses.add(CELL_STYLE_MANDATORY_ARGUMENT);
                    if (StringUtils.isBlank(contents)) {
                        styleClasses.add(CELL_STYLE_MANDATORY_ARGUMENT_MISSING);
                    }
                    break;
            }
        }
        cssStyleClassesProperty.clear();
        cssStyleClassesProperty.addAll(styleClasses);
    }

    public Stream<? extends IAutocompleteOption> getCompletionOptions(SnowTableKind snowTableKind, QualifiedKeyword whatWrittenSoFar) {
        partOfLine.recalculateSemantics();
        Stream<IAutocompleteOption> options = Stream.empty();
        if (semantics.isKeyword) {
            options = Stream.concat(options,
                    semantics.permissibleKeywords.stream().filter(kw -> kw.isLegalInContext(semantics.cellIndex, snowTableKind)));
            if (whatWrittenSoFar.getSource() == null) {
                options = Stream.concat(options,
                        semantics.permissibleKeywords.stream().filter(kw -> !kw.getSourceName().equals("")).filter(kw -> kw.isLegalInContext(semantics.cellIndex, snowTableKind))
                                .map(kw -> new LibraryAutocompleteOption(kw.getSourceName())).distinct());
            } else {
                options = Stream.concat(options,
                        semantics.permissibleKeywords.stream().filter(kw -> !kw.getSourceName().equals("")).filter(kw -> kw.isLegalInContext(semantics.cellIndex, snowTableKind))
                                .filter(kw -> Extensions.toInvariant(kw.getSourceName()).equals(Extensions.toInvariant(whatWrittenSoFar.getSource())))
                                .map(QualifiedCompletionOption::new));
            }
        }
        String gherkinPrefix = GherkinKeywords.getPrefixWithSpaceIfAny(whatWrittenSoFar.getKeyword());
        if (gherkinPrefix != null) {
            List<IAutocompleteOption> asList = options.collect(Collectors.toList());
            options = Stream.concat(asList.stream().map(co -> new GherkinEnhancedOption(gherkinPrefix, co)), asList.stream());
        }
        options = Stream.concat(options, GherkinKeywords.all.stream());
        if (Settings.getInstance().cbAutocompleteVariables) {
            if (semantics.cellIndex >= 1 && semantics.variablesList != null && snowTableKind != SnowTableKind.VARIABLES) {
                options = Stream.concat(options, semantics.variablesList.stream());
            }
        }
        return options;
    }

    public IKnownKeyword getKeywordInThisCell() {
        partOfLine.recalculateSemantics();
        return semantics.thisHereKeyword;
    }

    public boolean hasDocumentation() {
        return this.semantics.isKeyword;
    }

    @Override
    public Image getAutocompleteIcon() {
        if (semantics.isKeyword) {
            IKnownKeyword kw = getKeywordInThisCell();
            if (kw != null) {
                return kw.getAutocompleteIcon();
            }
        }
        return null;
    }

    @Override
    public String getQuickDocumentationCaption() {
        if (semantics.isKeyword) {
            IKnownKeyword kw = getKeywordInThisCell();
            if (kw != null) {
                return kw.getQuickDocumentationCaption();
            }
        }
        return null;
    }

    @Override
    public String getFullDocumentation() {
        if (semantics.isKeyword) {
            IKnownKeyword kw = getKeywordInThisCell();
            if (kw != null) {
                return kw.getFullDocumentation();
            }
        }
        return null;
    }

    @Override
    public String getItalicsSubheading() {
        if (semantics.isKeyword) {
            IKnownKeyword kw = getKeywordInThisCell();
            if (kw != null) {
                return kw.getItalicsSubheading();
            }
        }
        return null;
    }

    public ListProperty<String> getCssStyleClassesProperty() {
        return cssStyleClassesProperty;
    }

    public CellSemantics getSemantics() {
        return this.semantics;
    }

    public void setSemantics(CellSemantics semantics) {
        this.semantics = semantics;
    }

    public enum ArgumentStatus {
        MANDATORY,
        VARARG,
        FORBIDDEN,
        UNKNOWN
    }

    public void setRecentChangeListener(ChangeListener<List<String>> changeListener) {
        this.recentChangeListener = changeListener;
    }

    public ChangeListener<List<String>> getRecentChangeListener() {
        return recentChangeListener;
    }
}

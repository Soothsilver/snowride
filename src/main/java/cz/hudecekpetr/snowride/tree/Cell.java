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
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cz.hudecekpetr.snowride.semantics.RobotFrameworkVariableUtils.*;
import static cz.hudecekpetr.snowride.ui.grid.YellowHighlight.lastPositionSelectText;

public class Cell implements IHasQuickDocumentation {
    private static final String LINE_NUMBER_CELL_STYLE = "-fx-padding: 0; -fx-background-insets: 0.0; -fx-font-weight: bold;  -fx-alignment: center;";
    private static final String LINE_NUMBER_CELL_STYLE_DEFAULT = LINE_NUMBER_CELL_STYLE + "-fx-background-color: lavender";

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
    private final SimpleStringProperty styleProperty = new SimpleStringProperty(null);
    public IceCell iceCell;
    private CellSemantics semantics;

    public Cell(String contents, String postTrivia, LogicalLine partOfLine, boolean isLineNumberCell) {
        this.contents = contents;
        this.postTrivia = postTrivia;
        this.partOfLine = partOfLine;
        this.isLineNumberCell = isLineNumberCell;
        if (isLineNumberCell) {
            styleProperty.set(LINE_NUMBER_CELL_STYLE_DEFAULT);
        }
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
        String style = "";
        if (semantics.cellIndex == 0) {
            style += "-fx-font-weight: bold; ";
            if (partOfLine.belongsWhere == SnowTableKind.SETTINGS) {
                style += "-fx-text-fill: darkmagenta; ";
            } else {
                style += "-fx-text-fill: green; ";
            }
        }
        if (semantics.cellIndex == 1 && partOfLine.belongsWhere == SnowTableKind.SETTINGS) {
            if (partOfLine.getBelongsToHighElement() instanceof Suite) {
                Suite asSuite = (Suite) partOfLine.getBelongsToHighElement();
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
        if (semantics.cellIndex == 1 && (contents.startsWith("[") && contents.endsWith("]"))) {
            style += "-fx-text-fill: darkmagenta; ";
            style += "-fx-font-weight: bold; ";
        } else if (semantics.cellIndex == 1 && contents.equals("\\")) {
            style += "-fx-font-style: italic; -fx-background-color: darkgray; ";
        } else if (semantics.isComment) {
            style += "-fx-text-fill: brown; ";
        } else if (semantics.isKeyword) {
            style += "-fx-font-weight: bold; ";
            IKnownKeyword knownKeyword = semantics.thisHereKeyword;
            if (knownKeyword != null) {
                if (knownKeyword.getScenarioIfPossible() != null) {
                    style += "-fx-text-fill: blue; ";
                    if (Underlining.getActiveCell() == this && Underlining.ctrlDown) {
                        style += "-fx-underline: true; ";
                    }
                } else {
                    style += "-fx-text-fill: dodgerblue; ";
                }
            }
        } else if (semantics.cellIndex == 1 && CONDITIONAL_KEYWORDS.stream().anyMatch(contents::matches)) {
            style += "-fx-text-fill: darkmagenta; ";
            style += "-fx-font-weight: bold; ";
        } else if (semantics.isVariable) {
            style += "-fx-text-fill: green; ";
        } else if (containsAnyVariable(contents)) {
            if (containsVariable(contents, partOfLine.getBelongsToHighElement().variables)) {
                style += "-fx-text-fill: green; ";
            } else {
                style += "-fx-text-fill: #ff861a; ";
            }
        }
        style += "-fx-border-color: transparent #EDEDED #EDEDED transparent; -fx-border-width: 1px; ";
        if (!StringUtils.isBlank(contents) && Settings.getInstance().cbHighlightSameCells &&
                (contents.equals(lastPositionSelectText)
                        || (isVariable(lastPositionSelectText) && containsVariable(contents, getVariableName(lastPositionSelectText)))
                        || (containsVariable(lastPositionSelectText) && containsVariable(contents, getVariableName(lastPositionSelectText)))
                )
        ) {
            style += "-fx-background-color: #FFFF77; ";
        } else {
            switch (semantics.argumentStatus) {
                case FORBIDDEN:
                    if (!semantics.isComment && !StringUtils.isBlank(contents)) {
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
        }
        styleProperty.set(style);
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

    public SimpleStringProperty getStyleProperty() {
        return styleProperty;
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
}

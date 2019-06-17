package cz.hudecekpetr.snowride.lexer;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.IHasQuickDocumentation;
import cz.hudecekpetr.snowride.fx.Underlining;
import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.grid.SnowTableKind;
import cz.hudecekpetr.snowride.semantics.CellSemantics;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.resources.ImportedResource;
import cz.hudecekpetr.snowride.tree.Suite;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;

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
    private SimpleStringProperty styleProperty = new SimpleStringProperty(null);
    private CellSemantics semantics;

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
        String style = "";
        if (isLineNumberCell) {
            style += "-fx-font-weight: bold; -fx-background-color: lavender; -fx-text-alignment: right; -fx-alignment: center; ";
        } else {
            style += getStyle();
        }
        styleProperty.set(style);
    }

    private String getStyle() {
        partOfLine.recalculateSemantics();
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
        if (semantics.cellIndex == 1 && (contents.startsWith("[") && contents.endsWith("]"))) {
            style += "-fx-text-fill: darkmagenta; ";
            style += "-fx-font-weight: bold; ";
        } else if (semantics.cellIndex == 1 && contents.equals(": FOR") || contents.equals(":FOR") || contents.equals("FOR")) {
            style += "-fx-text-fill: darkmagenta; ";
            style += "-fx-font-weight: bold; ";
        } else if (semantics.cellIndex == 1 && contents.equals("\\")) {
            style += "-fx-font-style: italic; -fx-background-color: darkgray; ";
        } else if (semantics.isComment) {
            style += "-fx-text-fill: brown; ";
        } else if (semantics.isKeyword) {
            style += "-fx-font-weight: bold; ";
            IKnownKeyword knownKeyword = semantics.permissibleKeywordsByInvariantName.get(Extensions.toInvariant(this.contents));
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
        } else if (contents.contains("${") || contents.contains("@{") || contents.contains("&{")) {
            style += "-fx-text-fill: green; ";
        }
        style += "-fx-border-color: transparent #EDEDED #EDEDED transparent; -fx-border-width: 1px; ";
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
        return style;
    }


    public Stream<? extends IAutocompleteOption> getCompletionOptions(SnowTableKind snowTableKind) {
        partOfLine.recalculateSemantics();
        Stream<IAutocompleteOption> options = Stream.empty();
        if (semantics.isKeyword) {
            options = Stream.concat(options, semantics.permissibleKeywords.stream().filter(kw -> kw.isLegalInContext(semantics.cellIndex, snowTableKind)));
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

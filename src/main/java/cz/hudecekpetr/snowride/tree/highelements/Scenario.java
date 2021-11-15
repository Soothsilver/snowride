package cz.hudecekpetr.snowride.tree.highelements;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.fx.SnowAlert;
import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.runner.TestResult;
import cz.hudecekpetr.snowride.semantics.codecompletion.VariableCompletionOption;
import cz.hudecekpetr.snowride.semantics.findusages.FindUsages;
import cz.hudecekpetr.snowride.semantics.findusages.Usage;
import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.Tag;
import cz.hudecekpetr.snowride.tree.TagKind;
import cz.hudecekpetr.snowride.tree.sections.SectionKind;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

public class Scenario extends HighElement {

    private final ObservableList<LogicalLine> lines;
    public TestResult lastTestResult = TestResult.NOT_YET_RUN;
    public HashSet<Tag> actualTags = new HashSet<>();
    public boolean semanticsIsTemplateTestCase;
    private Cell nameCell;
    private boolean isTestCase;
    private List<String> semanticsArguments = new ArrayList<>();
    private List<IAutocompleteOption> thisScenarioVariables = new ArrayList<IAutocompleteOption>();

    public Scenario(Cell nameCell, boolean isTestCase, List<LogicalLine> lines) {
        super(nameCell.contents, null, new ArrayList<>());

        this.nameCell = nameCell;
        this.setTestCase(isTestCase);
        this.lines = FXCollections.observableList(lines);
        for (int i = 0; i < lines.size(); i++) {
            this.lines.get(i).lineNumber = new PositionInListProperty<>(this.lines.get(i), this.lines);
            this.lines.get(i).setBelongsToHighElement(this);
        }
    }

    public List<String> getSemanticsArguments() {
        return semanticsArguments;
    }

    public ObservableList<LogicalLine> getLines() {
        return lines;
    }

    @Override
    protected boolean isResourceOnly() {
        return !isTestCase;
    }

    @Override
    public void saveAll() {
        // Saved as part of the file suite.
    }


    @Override
    public void deleteSelf(MainForm mainForm) {
        this.parent.dissociateSelfFromChild(this);
        this.parent.markAsStructurallyChanged(mainForm);
    }

    @Override
    public void renameSelfTo(String newName, MainForm mainForm) {
        if (!this.isTestCase()) {
            List<Usage> allUsages = FindUsages.findUsagesInternal(null, this, MainForm.INSTANCE.getRootElement());
            if (allUsages.size() > 0) {
                ButtonType yes = new ButtonType("Rename");
                ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert alert = new SnowAlert(Alert.AlertType.CONFIRMATION, "Renaming this keyword will also alter " + allUsages.size() + " usages of the keyword. It is possible that Snowride missed some usages, if they're used as arguments to other keywords. Proceed with renaming anyway?",
                        yes,
                        cancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(Images.snowflake);
                Optional<ButtonType> result = alert.showAndWait();
                if (!result.isPresent() || result.get() != yes) {
                    return;
                }
                for (Usage usage : allUsages) {
                    usage.getUsageLine().getCellAsStringProperty(usage.getUsageCell(), MainForm.INSTANCE)
                            .set(new Cell(newName, usage.getUsageLine().cells.get(usage.getUsageCell()).postTrivia, usage.getUsageLine()));
                }
            }
        }
        this.nameCell = new Cell(newName, nameCell.postTrivia, null);
        this.shortNameProperty.set(newName);
        this.refreshToString();
        this.markAsStructurallyChanged(mainForm);
    }

    @Override
    public void applyText() {
        this.parent.applyText();
    }

    @Override
    public void markAsStructurallyChanged(MainForm mainForm) {
        mainForm.changeOccurredTo(this, LastChangeKind.STRUCTURE_CHANGED);
        this.parent.markAsStructurallyChanged(mainForm);
    }

    @Override
    public void optimizeStructure() {
        Extensions.optimizeLines(lines);
    }

    @Override
    protected void ancestorRenamed(File oldFile, File newFile) {
        // don't care, this is for files
    }

    @Override
    public Suite asSuite() {
        return this.parent;
    }

    @Override
    protected String getTagsDocumentation() {
        if (actualTags.size() > 0) {
            return "*Tags: *" + StringUtils.join(actualTags.stream().map(Tag::prettyPrint).iterator(), ", ");
        } else {
            return null;
        }
    }

    @Override
    public void analyzeCodeInSelf() {
        this.selfErrors.removeIf(error -> error.type.getValue().isLineError());
        this.lines.forEach(LogicalLine::recalculateSemantics);
        this.lines.forEach(LogicalLine::addLineErrorsToOwner);
    }

    public void serializeInto(StringBuilder sb) {
        sb.append(nameCell.contents);
        sb.append(nameCell.postTrivia);
        sb.append("\n");
        lines.forEach(ll -> ll.serializeInto(sb));
    }

    public boolean isTestCase() {
        return isTestCase;
    }

    public void setTestCase(boolean testCase) {
        isTestCase = testCase;
        if (isTestCase) {
            this.imageView.setImage(Images.testIcon);
            this.checkbox.setVisible(true);
        } else {
            this.imageView.setImage(Images.keywordIcon);
            this.checkbox.setVisible(false);
        }
    }

    @Override
    public Image getAutocompleteIcon() {
        return this.isTestCase ? Images.testIcon : Images.keywordIcon;
    }

    @Override
    public String getAutocompleteText() {
        return (this.isTestCase ? "[test] " : "[keyword] ") + toString();
    }

    @Override
    public String getItalicsSubheading() {
        return isTestCase ? "Test case" : "Keyword";
    }

    public void markTestStatus(TestResult lastTestResult) {
        this.lastTestResult = lastTestResult;
        switch (lastTestResult) {
            case NOT_YET_RUN:
                this.imageView.setImage(Images.testIcon);
                break;
            case PASSED:
                this.imageView.setImage(Images.yes);
                break;
            case FAILED:
                this.imageView.setImage(Images.no);
                break;
            case SKIP:
                this.imageView.setImage(Images.skip);
                break;
        }
    }

    @Override
    public String getQuickDocumentationCaption() {
        return toString();
    }

    public void analyzeSemantics() {
        // TODO template-ness can also be inherited from parent suites
        semanticsIsTemplateTestCase = false;
        ArrayList<String> argCells = new ArrayList<>();
        semanticsDocumentation = "";
        thisScenarioVariables.clear();
        Set<String> variableCells = new HashSet<>();
        for (LogicalLine line : getLines()) {
            if (line.cells.size() >= 3) {
                if (line.cells.get(1).contents.equalsIgnoreCase("[Documentation]")) {
                    List<String> docCells = new ArrayList<>();
                    for (int i = 2; i < line.cells.size(); i++) {
                        docCells.add(line.cells.get(i).contents);
                    }
                    semanticsDocumentationLine = line;
                    semanticsDocumentation = String.join("\n", docCells);
                } else if (line.cells.get(1).contents.equalsIgnoreCase("[Arguments]")) {
                    for (int i = 2; i < line.cells.size(); i++) {
                        if (!StringUtils.isBlank(line.cells.get(i).contents)) {
                            argCells.add(line.cells.get(i).contents);
                        }
                    }

                } else if (line.cells.get(1).contents.equalsIgnoreCase("[Template]")) {
                    semanticsIsTemplateTestCase = true;
                }
            }
            for (Cell cell : line.cells) {
                considerAddingAsVariableDefinition(variableCells, cell.contents);
            }
        }
        for (String variableCell : variableCells) {
            thisScenarioVariables.add(new VariableCompletionOption("${" + variableCell + "}", "A variable used in this test case or keyword."));
            thisScenarioVariables.add(new VariableCompletionOption("@{" + variableCell + "}", "A variable used in this test case or keyword, as a list."));
            thisScenarioVariables.add(new VariableCompletionOption("&{" + variableCell + "}", "A variable used in this test case or keyword, as a dictionary."));
        }
        thisScenarioVariables.addAll(parent.getVariablesList());
        this.semanticsArguments = argCells;
        if (argCells.size() > 0) {
            this.semanticsDocumentation = "*Args:* " + String.join(", ", argCells) + "\n" + (semanticsDocumentation != null ? semanticsDocumentation : "");
        }
    }

    private void considerAddingAsVariableDefinition(Set<String> variableCells, String contents) {
        if (contents.length() >= 3) {
            int initialBrace = contents.indexOf('{');
            int finalBrace = contents.indexOf('}');
            if (finalBrace != -1 && initialBrace == 1) {
                String trueName = contents.substring(2, finalBrace);
                variableCells.add(trueName);
            }
        }
    }

    @Override
    public void updateTagsForSelfAndChildren() {
        if (parent.childTestsAreTemplates) {
            semanticsIsTemplateTestCase = true;
        }
        actualTags = new HashSet<>(parent.forceTagsCumulative);
        boolean foundTags = false;
        for (LogicalLine line : getLines()) {
            if (line.cells.size() >= 2) {
                if (line.cells.get(1).contents.equalsIgnoreCase("[tags]")) {
                    for (int i = 2; i < line.cells.size(); i++) {
                        if (!StringUtils.isBlank(line.cells.get(i).contents)) {
                            actualTags.add(new Tag(line.cells.get(i).contents, TagKind.STANDARD, this));
                        }
                    }
                    foundTags = true;
                }
            }
        }
        if (!foundTags) {
            actualTags.addAll(parent.defaultTags);
        }
    }

    public LogicalLine findLineWithTags() {
        for (LogicalLine line : getLines()) {
            if (line.cells.size() >= 2) {
                if (line.cells.get(1).contents.equalsIgnoreCase("[tags]")) {
                    return line;
                }
            }
        }
        return null;
    }

    public void reformat() {
        optimizeStructure();
        for (LogicalLine line : lines) {
            line.reformat(SectionKind.TEST_CASES);
        }
    }

    @Override
    public List<? extends IAutocompleteOption> getVariablesList() {
        return thisScenarioVariables;
    }
}

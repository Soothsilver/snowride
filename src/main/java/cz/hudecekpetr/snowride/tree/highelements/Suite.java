package cz.hudecekpetr.snowride.tree.highelements;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import cz.hudecekpetr.snowride.errors.ErrorKind;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.NewlineStyle;
import cz.hudecekpetr.snowride.errors.SnowrideError;
import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.Setting;
import cz.hudecekpetr.snowride.semantics.UserKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.VariableCompletionOption;
import cz.hudecekpetr.snowride.semantics.externallibraries.ExternalLibrary;
import cz.hudecekpetr.snowride.semantics.resources.ImportedResource;
import cz.hudecekpetr.snowride.semantics.resources.ImportedResourceKind;
import cz.hudecekpetr.snowride.semantics.resources.KeywordSource;
import cz.hudecekpetr.snowride.semantics.resources.LibraryKeywordSource;
import cz.hudecekpetr.snowride.semantics.resources.ResourceFileKeywordSource;
import cz.hudecekpetr.snowride.semantics.resources.TestCaseSettingOptionLibrarySource;
import cz.hudecekpetr.snowride.tree.sections.IScenarioSection;
import cz.hudecekpetr.snowride.tree.sections.KeyValuePairSection;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.RobotFile;
import cz.hudecekpetr.snowride.tree.sections.RobotSection;
import cz.hudecekpetr.snowride.tree.sections.SectionKind;
import cz.hudecekpetr.snowride.tree.Tag;
import cz.hudecekpetr.snowride.tree.TagKind;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.controlsfx.validation.Severity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Suite extends HighElement implements ISuite {
    /**
     * The name of this file or folder on disk, including the prefix that sets the execution order, and without changing
     * cases of letters or replacing underscores with spaces. But, this already doesn't contain the .robot extension.
     */
    protected String shortNameAsOnDisk;
    public RobotFile fileParsed;
    public long importedResourcesLastRecursedDuringIteration = 0;
    public Set<Tag> forceTagsCumulative = new HashSet<>();
    public Set<Tag> defaultTags = new HashSet<>();
    private List<ImportedResource> importedResources = new ArrayList<>();
    private Set<KeywordSource> importedResourcesRecursively = new HashSet<>();
    private List<IKnownKeyword> importedKeywordsRecursively = new ArrayList<>();
    private List<VariableCompletionOption> importedVariablesRecursively = new ArrayList<>();
    private Multimap<String, IKnownKeyword> importedKeywordsRecursivelyByInvariantName =
            MultimapBuilder.hashKeys().arrayListValues().build();
    public boolean childTestsAreTemplates = false;
    /**
     * What to use as line separators. By default, we use LF only, unless the file as loaded has CRLF.
     */
    public NewlineStyle newlineStyle = NewlineStyle.LF;

    public Suite(String shortName, String contents, List<HighElement> children) {
        super(Extensions.toPrettyName(shortName), contents, children);
        shortNameAsOnDisk = shortName;
        this.children.addListener((ListChangeListener.Change<? extends HighElement> change) -> childrenChanged());
        if (contents != null && contents.indexOf('\r') != -1) {
            // If you load it with \r, it's Windows-style line endings.
            newlineStyle = NewlineStyle.CRLF;
        }
    }

    @Override
    public String getShortNameAsOnDisk() {
        return shortNameAsOnDisk;
    }

    private void childrenChanged() {
        this.imageView.setImage(getAutocompleteIcon());
    }

    public List<ImportedResource> getImportedResources() {
        return importedResources;
    }

    public String serialize() {
        return fileParsed.serialize(newlineStyle);
    }

    private void reparseResources() {
        this.importedResources.clear();
        this.selfErrors.removeIf(snowrideError -> snowrideError.type.getValue() == ErrorKind.IMPORT_ERROR);
        if (fileParsed != null) {
            for (RobotSection section : fileParsed.sections) {
                if (section.header.sectionKind == SectionKind.SETTINGS) {
                    KeyValuePairSection settingsSection = (KeyValuePairSection) section;
                    settingsSection.createSettings().forEach(setting -> {
                        if (setting.firstValue != null) {
                            if (setting.key.equalsIgnoreCase("Library")) {
                                importedResources.add(new ImportedResource(setting.firstValue, ImportedResourceKind.LIBRARY));
                            } else if (setting.key.equalsIgnoreCase("Resource")) {
                                importedResources.add(new ImportedResource(setting.firstValue, ImportedResourceKind.RESOURCE));
                            }
                        }
                    });
                }
            }
        }
    }

    private void recalculateResources() {
        importedResourcesRecursively.clear();
        importedKeywordsRecursively.clear();
        importedVariablesRecursively.clear();
        importedResourcesRecursively.add(new TestCaseSettingOptionLibrarySource());
        importedResourcesRecursively.add(new LibraryKeywordSource(ExternalLibrary.builtIn));
        importedResourcesRecursively.add(new ResourceFileKeywordSource(this));
        importedResources.forEach(ir -> ir.gatherSelfInto(importedResourcesRecursively, this, ImportedResource.incrementAndGetIterationCount()));
        importedResourcesRecursively.stream().flatMap(KeywordSource::getAllKeywords).forEachOrdered(kk -> importedKeywordsRecursively.add(kk));
        importedResources.forEach(ir -> {
            if (!ir.isSuccessfullyImported()) {
                String text = ir.getName() + " is not a known resource or library.";
                selfErrors.add(new SnowrideError(this, ErrorKind.IMPORT_ERROR, Severity.WARNING, text));
            }
        });
        importedKeywordsRecursively.sort(Comparator.comparingInt(IKnownKeyword::getCompletionPriority));
        importedKeywordsRecursivelyByInvariantName.clear();
        importedKeywordsRecursively.forEach(keyword -> importedKeywordsRecursivelyByInvariantName.put(keyword.getInvariantName(), keyword));
        importedResourcesRecursively.stream().flatMap(KeywordSource::getAllVariables).forEachOrdered(vco -> importedVariablesRecursively.add(vco));
        // Built-ins:
        importedVariablesRecursively.add(new VariableCompletionOption("${EMPTY}", "Built-in variable that's an empty string."));
        importedVariablesRecursively.add(new VariableCompletionOption("@{EMPTY}", "Built-in variable that's an empty list."));
        importedVariablesRecursively.add(new VariableCompletionOption("&{EMPTY}", "Built-in variable that's an empty dictionary."));
        importedVariablesRecursively.add(new VariableCompletionOption("${SPACE}", "Built-in variable that's a single space."));
    }

    public Stream<IKnownKeyword> getSelfKeywords() {
        return this.children.stream().filter(he -> (he instanceof Scenario) && !((Scenario) he).isTestCase())
                .map(he -> {
                    Scenario s = (Scenario) he;
                    return UserKeyword.fromScenario(s);
                });
    }

    public List<IKnownKeyword> getKeywordsPermissibleInSuite() {
        return importedKeywordsRecursively;
    }

    public Multimap<String, IKnownKeyword> getKeywordsPermissibleInSuiteByInvariantName() {
        return importedKeywordsRecursivelyByInvariantName;
    }

    public void reparse() {
        if (contents != null) {
            RobotFile parsed = GateParser.parse(contents, this);
            fileParsed = parsed;
            selfErrors.removeIf(snowrideError -> snowrideError.type.getValue() == ErrorKind.PARSE_ERROR);
            for (Exception exception : parsed.errors) {
                this.selfErrors.add(new SnowrideError(this, ErrorKind.PARSE_ERROR, Severity.ERROR, ExceptionUtils.getMessage(exception)));
            }
            this.reparseResources();
            addOrUpdateTreeNodes(parsed.getHighElements(), treeNode, children);

            children.removeIf(he -> he instanceof Scenario);
            children.addAll(parsed.getHighElements());
            for (HighElement child : children) {
                if (child instanceof Scenario) {
                    child.parent = this;
                }
            }
        }
    }

    public void analyzeSemantics() {
        if (this.fileParsed != null) {
            this.fileParsed.analyzeSemantics(this);
        }
        this.updateTagsForSelfAndChildren();
    }

    @Override
    public void applyText() {
        // Apply
        if (this.areTextChangesUnapplied) {
            reparse();
            analyzeSemantics();
            this.areTextChangesUnapplied = false;
        }
    }

    public void reparseAndRecalculateResources() {
        this.reparseResources();
        this.recalculateResources();
    }

    @Override
    public void updateTagsForSelfAndChildren() {
        if (this.parent != null) {
            this.forceTagsCumulative = new HashSet<>(parent.forceTagsCumulative);
            this.childTestsAreTemplates = parent.childTestsAreTemplates;
        } else {
            this.forceTagsCumulative.clear();
            this.childTestsAreTemplates = false;
        }
        this.defaultTags.clear();
        if (this.fileParsed != null) {
            List<Setting> settings = this.fileParsed.findOrCreateSettingsSection().createSettings();
            for (Setting setting : settings) {
                if (setting.key.toLowerCase().equals("force tags")) {
                    for (String val : setting.values) {
                        if (!StringUtils.isBlank(val)) {
                            forceTagsCumulative.add(new Tag(val, TagKind.FORCED, this));
                        }
                    }
                }
                if (setting.key.toLowerCase().equals("default tags")) {
                    for (String val : setting.values) {
                        if (!StringUtils.isBlank(val)) {
                            defaultTags.add(new Tag(val, TagKind.DEFAULT, this));
                        }
                    }
                }
                if (setting.key.toLowerCase().equals("test template")) {
                    childTestsAreTemplates = true;
                }
            }
        }
        for (HighElement child : this.children) {
            child.updateTagsForSelfAndChildren();
        }
    }


    @Override
    public final void optimizeStructure() {
        if (fileParsed != null) {
            for (RobotSection section : fileParsed.sections) {
                section.optimizeStructure(this.getUndoStack());
            }
        }
    }

    @Override
    public final Suite asSuite() {
        return this;
    }


    public void replaceChildWithAnotherChild(HighElement oldElement, Suite newSuite) {
        int indexOld = children.indexOf(oldElement);
        int indexTreeOld = treeNode.getChildren().indexOf(oldElement.treeNode);

        children.remove(oldElement);
        newSuite.parent = this;
        children.add(indexOld, newSuite);

        TreeItem<HighElement> oldTreeNode = treeNode.getChildren().get(indexTreeOld);
        addOrUpdateTreeNodes(newSuite.children, oldTreeNode, oldTreeNode.getValue().children);
        MainForm.INSTANCE.reloadCurrentThing();
        oldTreeNode.setValue(newSuite);
        newSuite.treeNode = oldTreeNode;
    }

    private void addOrUpdateTreeNodes(List<HighElement> newHighElements, TreeItem<HighElement> treeNode, ObservableList<HighElement> children) {
        if (newHighElements.stream().anyMatch(highElement -> !(highElement instanceof Scenario))) {
            throw new RuntimeException("Only scenarios were expected!");
        }

        // TreeNode - delete removed nodes
        List<HighElement> removedNodes = children.stream()
                .filter(highElement -> highElement instanceof Scenario)
                .filter(currentElement -> newHighElements.stream().noneMatch(newElement -> currentElement.getInvariantName().equals(newElement.getInvariantName())))
                .collect(Collectors.toList());
        treeNode.getChildren().removeIf(ti -> removedNodes.contains(ti.getValue()));

        // TreeNode - add or update existing nodes
        for (int i = 0; i < newHighElements.size(); i++) {
            HighElement newElement = newHighElements.get(i);
            if (i >= treeNode.getChildren().size() || !newElement.getInvariantName().equals(treeNode.getChildren().get(i).getValue().getInvariantName())) {
                treeNode.getChildren().add(i, newElement.treeNode);
            } else {
                TreeItem<HighElement> currentNode = treeNode.getChildren().get(i);
                newElement.updateGraphics(currentNode.getGraphic(), currentNode.getValue());
                newElement.outputElement = currentNode.getValue().outputElement;
                currentNode.setValue(newElement);
                newElement.treeNode = currentNode;
            }
        }
    }

    public boolean excludedFromQualifiedName() {
        return this.parent instanceof ExternalResourcesElement;
    }

    @Override
    public void dissociateSelfFromChild(HighElement child) {
        super.dissociateSelfFromChild(child);
        if (child instanceof Scenario) {
            if (fileParsed != null) {
                for (RobotSection section : fileParsed.sections) {
                    section.removeChildIfAble((Scenario) child);
                }
            }
        }
    }

    @Override
    protected boolean isResourceOnly() {
        for (HighElement child : children) {
            if (!child.isResourceOnly()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected String getTagsDocumentation() {
        String t = "";
        if (forceTagsCumulative.size() > 0) {
            t += "*Force tags:* " + StringUtils.join(forceTagsCumulative.stream().map(tg -> tg.prettyPrintFromSuite(this)).iterator(), ", ");
        }
        if (defaultTags.size() > 0) {
            if (!StringUtils.isEmpty(t)) {
                t += "\n";
            }
            t += "*Default tags:* " + StringUtils.join(defaultTags.stream().map(Tag::getTag).iterator(), ", ");
        }
        if (StringUtils.isEmpty(t)) {
            return null;
        } else {
            return t;
        }
    }

    @Override
    public void analyzeCodeInSelf() {
        reparseAndRecalculateResources();
        if (fileParsed != null) {
            fileParsed.findOrCreateSettingsSection().pairs.forEach(LogicalLine::recalculateSemantics);
        }
    }

    /**
     * Returns the "Force tags" line in this suite's Settings table. If there is no such line, returns null.
     */
    public LogicalLine findLineWithTags() {
        for (LogicalLine pair : fileParsed.findOrCreateSettingsSection().pairs) {
            if (pair.getCellAsStringProperty(0, MainForm.INSTANCE).getValue().contents.equalsIgnoreCase("Force tags")) {
                return pair;
            }
        }
        return null;
    }

    public void displaceChildScenario(Scenario child, int indexDifference) {
        boolean isTestCase = child.isTestCase();
        IScenarioSection section = isTestCase ? this.fileParsed.findOrCreateTestCasesSection() : this.fileParsed.findOrCreateKeywordsSection();
        List<Scenario> scenarios = section.getScenarios();
        int sectionIndex = scenarios.indexOf(child);
        int childrenIndex = children.indexOf(child);
        int treeIndex = treeNode.getChildren().indexOf(child.treeNode);
        if (sectionIndex == -1) {
            throw new IllegalArgumentException("The scenario '" + child + "' is not part of suite '" + this + "'.");
        }
        if (indexDifference == -1 && sectionIndex == 0) {
            // We're the top stuff.
            return;
        }
        if (indexDifference == 1 && sectionIndex == scenarios.size() - 1) {
            // We're the bottom stuff.
            return;
        }
        int insertionIndexDiff = (indexDifference == 1 ? 2 : -1);
        int deletionIndexDiff = (indexDifference == 1 ? 0 : 1);
        displaceInList(scenarios, sectionIndex + insertionIndexDiff, sectionIndex + deletionIndexDiff, child);
        displaceInList(children, childrenIndex + insertionIndexDiff, childrenIndex + deletionIndexDiff, child);
        displaceInList(treeNode.getChildren(), treeIndex + insertionIndexDiff, treeIndex + deletionIndexDiff, child.treeNode);
        markAsStructurallyChanged(MainForm.INSTANCE);
    }

    private <T> void displaceInList(List<T> list, int insertionIndex, int deletionIndex, T item) {
        if (insertionIndex < 0) {
            // do nothing
            return;
        }
        if (insertionIndex > list.size()) {
            // do nothing
            return;
        }
        list.add(insertionIndex, item);
        list.remove(deletionIndex);
    }

    @Override
    public List<? extends IAutocompleteOption> getVariablesList() {
        List<IAutocompleteOption> i = new ArrayList<>();
        return importedVariablesRecursively;
    }

    public Stream<VariableCompletionOption> getSelfVariables() {
        // Variables table:
        List<VariableCompletionOption> vco = new ArrayList<>();
        if (fileParsed != null) {
            fileParsed.findOrCreateVariablesSection().pairs.forEach(pair -> {
                if (pair.cells.size() > 0) {
                    String name = pair.cells.get(0).contents;
                    if (name.length() >= 3) {
                        int finalBrace = name.indexOf('}');
                        if (finalBrace != -1) {
                            String trueName = name.substring(2, finalBrace);
                            String desc = "A user-defined variable, originally defined as " + name + ".";
                            vco.add(new VariableCompletionOption("${" + trueName + "}", desc));
                            vco.add(new VariableCompletionOption("@{" + trueName + "}", desc));
                            vco.add(new VariableCompletionOption("&{" + trueName + "}", desc));
                        }
                    }
                }
            });
            return vco.stream();
        } else {
            return Stream.empty();
        }
    }
}

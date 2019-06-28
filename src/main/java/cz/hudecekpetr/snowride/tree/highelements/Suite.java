package cz.hudecekpetr.snowride.tree.highelements;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import cz.hudecekpetr.snowride.errors.ErrorKind;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.NewlineStyle;
import cz.hudecekpetr.snowride.errors.SnowrideError;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.Setting;
import cz.hudecekpetr.snowride.semantics.UserKeyword;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.controlsfx.validation.Severity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private Multimap<String, IKnownKeyword> importedKeywordsRecursivelyByInvariantName =
            MultimapBuilder.hashKeys().arrayListValues().build();
    public boolean childTestsAreTemplates = false;
    /**
     * What to use as line separators. By default, we use LF only, unless the file as loaded has CRLF.
     */
    private NewlineStyle newlineStyle = NewlineStyle.LF;
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
            this.children.removeIf(he -> he instanceof Scenario);
            this.treeNode.getChildren().removeIf(ti -> ti.getValue() instanceof Scenario);
            RobotFile parsed = GateParser.parse(contents, this);
            this.fileParsed = parsed;
            this.selfErrors.removeIf(snowrideError -> snowrideError.type.getValue() == ErrorKind.PARSE_ERROR);
            for (Exception exception : parsed.errors) {
                this.selfErrors.add(new SnowrideError(this, ErrorKind.PARSE_ERROR, Severity.ERROR, ExceptionUtils.getMessage(exception)));
            }
            this.reparseResources();
            this.addChildren(parsed.getHighElements());
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
                section.optimizeStructure();
            }
        }
    }

    @Override
    public final Suite asSuite() {
        return this;
    }


    public void replaceChildWithAnotherChild(HighElement oldElement, Suite newElement) {
        int indexOld = children.indexOf(oldElement);
        int indexTreeOld = treeNode.getChildren().indexOf(oldElement.treeNode);
        dissociateSelfFromChild(oldElement);
        newElement.parent = this;
        children.add(indexOld, newElement);
        treeNode.getChildren().add(indexTreeOld, newElement.treeNode);
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
                t+="\n";
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
}

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
    public RobotFile fileParsed;
    public long importedResourcesLastRecursedDuringIteration = 0;
    public Set<Tag> forceTagsCumulative = new HashSet<>();
    public Set<Tag> defaultTags = new HashSet<>();
    private List<ImportedResource> importedResources = new ArrayList<>();
    private Set<KeywordSource> importedResourcesRecursively = new HashSet<>();
    private List<IKnownKeyword> importedKeywordsRecursively = new ArrayList<>();
    private Multimap<String, IKnownKeyword> importedKeywordsRecursivelyByInvariantName =
            MultimapBuilder.hashKeys().arrayListValues().build();
    /**
     * What to use as line separators. By default, we use LF only, unless the file as loaded has CRLF.
     */
    private NewlineStyle newlineStyle = NewlineStyle.LF;
    public Suite(String shortName, String contents, List<HighElement> children) {
        super(Extensions.toPrettyName(shortName), contents, children);
        this.children.addListener((ListChangeListener)this::childrenChanged);
        if (contents != null && contents.indexOf('\r') != -1) {
            // If you load it with \r, it's Windows-style line endings.
            newlineStyle = NewlineStyle.CRLF;
        }
    }

    private void childrenChanged(ListChangeListener.Change change) {
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
        } else {
            this.forceTagsCumulative.clear();
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

    public LogicalLine findLineWithTags() {
        for (LogicalLine pair : fileParsed.findOrCreateSettingsSection().pairs) {
            if (pair.getCellAsStringProperty(0, MainForm.INSTANCE).getValue().contents.equalsIgnoreCase("Force tags")) {
                return pair;
            }
        }
        return null;
    }
}

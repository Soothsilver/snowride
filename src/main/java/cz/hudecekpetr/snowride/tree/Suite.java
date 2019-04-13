package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.ErrorKind;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.NewlineStyle;
import cz.hudecekpetr.snowride.SnowrideError;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.semantics.*;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import cz.hudecekpetr.snowride.semantics.resources.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.controlsfx.validation.Severity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public abstract class Suite extends HighElement {
    public RobotFile fileParsed;

    public Suite(String shortName, String contents, List<HighElement> children) {
        super(Extensions.toPrettyName(shortName), contents, children);
        if (contents != null && contents.indexOf('\r') != -1) {
            // If you load it with \r, it's Windows-style line endings.
            newlineStyle = NewlineStyle.CRLF;
        }
    }

    private List<ImportedResource> importedResources = new ArrayList<>();
    private Set<KeywordSource> importedResourcesRecursively = new HashSet<>();
    private List<IKnownKeyword> importedKeywordsRecursively = new ArrayList<>();
    /**
     * What to use as line separators. By default, we use LF only, unless the file as loaded has CRLF.
     */
    private NewlineStyle newlineStyle = NewlineStyle.LF;
    public long importedResourcesLastRecursedDuringIteration = 0;
    public Set<Tag> forceTagsCumulative = new HashSet<>();
    public Set<Tag> defaultTags = new HashSet<>();

    public List<ImportedResource> getImportedResources() {
        return importedResources;
    }
    public String serialize() {
        return fileParsed.serialize(newlineStyle);
    }
    public void reparseResources() {
        this.importedResources.clear();
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
        importedResourcesRecursively.add(new LibraryKeywordSource(ExternalLibrary.builtIn));
        importedResourcesRecursively.add(new ResourceFileKeywordSource(this));
        importedResources.forEach(ir -> ir.gatherSelfInto(importedResourcesRecursively, this, ImportedResource.incrementAndGetIterationCount()));
        importedResourcesRecursively.stream().flatMap(KeywordSource::getAllKeywords).forEachOrdered(kk -> {
            importedKeywordsRecursively.add(kk);
        });
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

    public void reparse() {
        if (contents != null) {
            this.children.removeIf(he -> he instanceof Scenario);
            this.treeNode.getChildren().removeIf(ti -> ti.getValue() instanceof Scenario);
            RobotFile parsed = GateParser.parse(contents);
            this.fileParsed = parsed;
            this.selfErrors.removeIf(snowrideError -> snowrideError.type.getValue() == ErrorKind.PARSE_ERROR);
            for (Exception exception : parsed.errors) {
                this.selfErrors.add(new SnowrideError(this, ErrorKind.PARSE_ERROR, Severity.ERROR, ExceptionUtils.getMessage(exception)));
            }
            this.reparseResources();
            this.selfErrors.removeIf(snowrideError -> snowrideError.type.getValue() == ErrorKind.IMPORT_ERROR);
            this.addChildren(parsed.getHighElements());
        }
    }

    public void analyzeSemantics() {
        if (this.fileParsed != null) {
            this.fileParsed.analyzeSemantics(this);
        }
        this.updateTagsForSelfAndChildren();
        this.recheckSerialization();
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

    private void recheckSerialization() {
        this.selfErrors.removeIf(error -> error.type.getValue() == ErrorKind.SERIALIZATION_ERROR);
        if (this.unsavedChanges == LastChangeKind.TEXT_CHANGED && this.fileParsed != null && this.fileParsed.errors.size() == 0) {
            String afterSerialization = this.fileParsed.serialize(newlineStyle).replace("\r", "");
            String beforeSerialization = Extensions.removeFinalNewlineIfAny(this.contents.replace("\r", ""));
            if (!afterSerialization.equals(beforeSerialization)) {
                selfErrors.add(new SnowrideError(this, ErrorKind.SERIALIZATION_ERROR, Severity.WARNING, "File might not serialize in the same way you've written it down."));
            }
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
                        forceTagsCumulative.add(new Tag(val));
                    }
                }
                if (setting.key.toLowerCase().equals("default tags")) {
                    for (String val : setting.values) {
                        defaultTags.add(new Tag(val));
                    }
                }
            }
        }
        for (HighElement child : this.children) {
            child.updateTagsForSelfAndChildren();
        }
    }


    @Override
    protected final void optimizeStructure() {
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
}

package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.ErrorKind;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.SnowrideError;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.semantics.*;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import cz.hudecekpetr.snowride.semantics.resources.ImportedResource;
import cz.hudecekpetr.snowride.semantics.resources.ImportedResourceKind;
import cz.hudecekpetr.snowride.semantics.resources.KeywordSource;
import cz.hudecekpetr.snowride.semantics.resources.LibraryKeywordSource;
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
    }

    private List<ImportedResource> importedResources = new ArrayList<>();
    private Set<KeywordSource> importedResourcesRecursively = new HashSet<>();
    public long importedResourcesLastRecursedDuringIteration = 0;

    public List<ImportedResource> getImportedResources() {
        return importedResources;
    }

    public void reparseResources() {
        this.importedResources.clear();
        if (fileParsed != null) {
            for (RobotSection section : fileParsed.sections) {
                if (section.header.sectionKind == SectionKind.SETTINGS) {
                    KeyValuePairSection settingsSection = (KeyValuePairSection) section;
                    settingsSection.createSettings().forEach(setting -> {
                        if (setting.key.equalsIgnoreCase("Library")) {
                            importedResources.add(new ImportedResource(setting.firstValue, ImportedResourceKind.LIBRARY));
                        } else if (setting.key.equalsIgnoreCase("Resource")) {
                            importedResources.add(new ImportedResource(setting.firstValue, ImportedResourceKind.RESOURCE));
                        }
                    });
                }
            }
        }
    }

    private void recalculateResources() {
        importedResourcesRecursively.clear();
        importedResourcesRecursively.add(new LibraryKeywordSource(ExternalLibrary.builtIn));
        importedResources.forEach(ir -> ir.gatherSelfInto(importedResourcesRecursively, this, ImportedResource.incrementAndGetIterationCount()));
    }

    public Stream<IKnownKeyword> getSelfKeywords() {
        return this.children.stream().filter(he -> (he instanceof Scenario) && !((Scenario) he).isTestCase())
                .map(he -> {
                    Scenario s = (Scenario) he;
                    return UserKeyword.fromScenario(s);
                });
    }

    public Stream<IKnownKeyword> getKeywordsPermissibleInSuite() {
        return Stream.concat(getSelfKeywords(), getImportedKeywords());
    }

    private Stream<IKnownKeyword> getImportedKeywords() {
        return importedResourcesRecursively.stream().flatMap(KeywordSource::getAllKeywords);
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

    private void validateImportedResources() {
        this.selfErrors.removeIf(err -> err.type.getValue() == ErrorKind.IMPORT_ERROR);
        for (ImportedResource importedResource : importedResources) {
            try {
                importedResource.getImportedKeywords(this);
            } catch (ImportException importError) {
                this.selfErrors.add(new SnowrideError(this, ErrorKind.IMPORT_ERROR, Severity.ERROR, importError.getMessage()));
            }
        }
    }

    public void analyzeSemantics() {
        if (this.fileParsed != null) {
            this.fileParsed.analyzeSemantics(this);
        }
        this.validateImportedResources();
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
            String afterSerialization = this.fileParsed.serialize().replace("\r", "");
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
}

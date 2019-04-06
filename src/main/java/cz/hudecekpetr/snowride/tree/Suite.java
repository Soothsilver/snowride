package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.ErrorKind;
import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.SnowrideError;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.parser.GateParser;
import cz.hudecekpetr.snowride.semantics.*;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class Suite extends HighElement {
    public RobotFile fileParsed;

    public Suite(String shortName, String contents, List<HighElement> children) {
        super(Extensions.toPrettyName(shortName), contents, children);
    }

    public List<ImportedResource> importedResources = new ArrayList<>();


    protected void reparseResources(RobotFile robotFile) {
        this.importedResources.clear();
        for (RobotSection section : robotFile.sections) {
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

    public Stream<IKnownKeyword> getSelfKeywords() {
        return this.children.stream().filter(he -> (he instanceof Scenario) && !((Scenario) he).isTestCase())
                .map(he -> {
                    Scenario s = (Scenario) he;
                    return UserKeyword.fromScenario(s);
                });
    }

    public Stream<IKnownKeyword> getKeywordsPermissibleInSuite() {
        return Stream.concat(Stream.concat(getSelfKeywords(), getImportedKeywords()), ExternalLibrary.builtIn.keywords.stream());
    }

    private Stream<IKnownKeyword> getImportedKeywords() {
        return importedResources.stream().flatMap(ir -> {
            try {
                return ir.getImportedKeywords(this);
            } catch (ImportException importException) {
                return Stream.empty();
            }
        });
    }

    public void reparse() {
        if (contents != null) {
            this.children.removeIf(he -> he instanceof Scenario);
            this.treeNode.getChildren().removeIf(ti -> ti.getValue() instanceof Scenario);
            RobotFile parsed = GateParser.parse(contents);
            this.fileParsed = parsed;
            this.selfErrors.removeIf(snowrideError -> snowrideError.type.getValue() == ErrorKind.PARSE_ERROR);
            for (Exception exception : parsed.errors) {
                this.selfErrors.add(new SnowrideError(this, ErrorKind.PARSE_ERROR, Severity.ERROR, exception.getMessage()));
            }
            this.reparseResources(parsed);
            this.selfErrors.removeIf(snowrideError -> snowrideError.type.getValue() == ErrorKind.IMPORT_ERROR);
            this.validateImportedResources();
            this.addChildren(parsed.getHighElements());
            this.analyzeSemantics();
            this.recheckSerialization();
        }
    }

    private void validateImportedResources() {
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
    }

    @Override
    public void applyAndValidateText() {
        // Apply
        if (this.areTextChangesUnapplied) {
            reparse();
            this.areTextChangesUnapplied = false;
        }

        // Validate
        if (fileParsed != null && fileParsed.errors.size() > 0) {
            throw new RuntimeException("There are parse errors.");
        }
    }

    protected void recheckSerialization() {
        this.selfErrors.removeIf(error -> error.type.getValue() == ErrorKind.SERIALIZATION_ERROR);
        if (this.unsavedChanges == LastChangeKind.TEXT_CHANGED && this.fileParsed != null && this.fileParsed.errors.size() == 0) {
            String afterSerialization = this.fileParsed.serialize().replace("\r", "");
            String beforeSerialization = Extensions.removeFinalNewlineIfAny(this.contents.replace("\r", ""));
            if (!afterSerialization.equals(beforeSerialization)) {
                selfErrors.add(new SnowrideError(this, ErrorKind.SERIALIZATION_ERROR, Severity.WARNING, "File might not serialize in the same way you've written it down."));
            }
        }
    }

    @Override
    public String getFullDocumentation() {
        return "*Qualified name:* " + this.getQualifiedName() +
                (!StringUtils.isBlank(this.semanticsDocumentation) ? ("\n" + "*Documentation:* " + this.semanticsDocumentation) : "");
    }
}

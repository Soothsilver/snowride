package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.ImportedResource;
import cz.hudecekpetr.snowride.semantics.ImportedResourceKind;
import cz.hudecekpetr.snowride.semantics.UserKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class Suite extends HighElement {
    public Suite(String shortName, String contents, List<HighElement> children) {
        super(shortName, contents, children);
    }

    public List<ImportedResource> importedResources = new ArrayList<>();


    protected void reparseResources(RobotFile robotFile) {
        this.importedResources.clear();
        for (RobotSection section : robotFile.sections) {
            if (section.header.sectionKind == SectionKind.SETTINGS) {
                KeyValuePairSection settingsSection = (KeyValuePairSection)section;
                settingsSection.createSettings().forEach(setting -> {
                    if (setting.key.equalsIgnoreCase("Library")) {
                        importedResources.add(new ImportedResource(setting.firstValue, ImportedResourceKind.LIBRARY));
                    }
                    else if (setting.key.equalsIgnoreCase("Resource")) {
                        importedResources.add(new ImportedResource(setting.firstValue, ImportedResourceKind.RESOURCE));
                    }
                });
            }
        }
    }

    public Stream<IKnownKeyword> getSelfKeywords() {
        return this.children.stream().filter(he -> (he instanceof Scenario) && !((Scenario)he).isTestCase())
                .map(he -> {
                    Scenario s = (Scenario)he;
                    return UserKeyword.fromScenario(s);
                });
    }

    public Stream<IKnownKeyword> getKeywordsPermissibleInSuite() {
        return Stream.concat(Stream.concat(getSelfKeywords(), getImportedKeywords()), ExternalLibrary.builtIn.keywords.stream());
    }

    private Stream<IKnownKeyword> getImportedKeywords() {
        return importedResources.stream().flatMap(ir -> ir.getImportedKeywords());
    }

}

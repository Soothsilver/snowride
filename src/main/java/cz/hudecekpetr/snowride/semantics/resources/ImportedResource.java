package cz.hudecekpetr.snowride.semantics.resources;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.semantics.externallibraries.ExternalLibrary;
import cz.hudecekpetr.snowride.semantics.externallibraries.ReloadExternalLibraries;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.highelements.ExternalResourcesElement;
import cz.hudecekpetr.snowride.tree.highelements.FileSuite;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;

public class ImportedResource {
    private static long indexOfIteration = 0;
    private final String name;
    private final ImportedResourceKind kind;
    private boolean successfullyImported = false;
    private Suite importsSuite = null;

    public ImportedResource(String name, ImportedResourceKind kind) {
        this.name = name;
        this.kind = kind;
    }

    public static long incrementAndGetIterationCount() {
        return ++indexOfIteration;
    }

    public String getName() {
        return name;
    }

    public ImportedResourceKind getKind() {
        return kind;
    }

    public boolean isSuccessfullyImported() {
        return successfullyImported;
    }

    public Suite getImportsSuite() {
        return importsSuite;
    }

    public void gatherSelfInto(Set<KeywordSource> gatherIntoThis, Suite owningSuite, long iterationCounter) {
        owningSuite.importedResourcesLastRecursedDuringIteration = iterationCounter;
        switch (kind) {
            case LIBRARY:
                importsSuite = null;
                if (ExternalLibrary.otherPackedInLibraries.containsKey(name)) {
                    gatherIntoThis.add(new LibraryKeywordSource(ExternalLibrary.otherPackedInLibraries.get(name)));
                    successfullyImported = true;
                } else if (ExternalLibrary.knownExternalLibraries.containsKey(name)) {
                    gatherIntoThis.add(new LibraryKeywordSource(ExternalLibrary.knownExternalLibraries.get(name)));
                    successfullyImported = true;
                } else {
                    // Unknown library. Maybe it's in the system pythonpath
                    ReloadExternalLibraries.considerLoadingFromSystemPythonpath(name);
                    // Ignore it here. We'll put in the error list and such later.
                    successfullyImported = false;
                }
                break;
            case RESOURCE:
                Path path = Paths.get(name);
                HighElement suite = owningSuite;
                if (suite instanceof FileSuite) {
                    suite = suite.parent;
                }
                boolean isFirstSection = true;
                outerFor:
                for (Path section : path) {
                    String sectionString = Extensions.toInvariant(section.toString());
                    if (sectionString.equals(".")) {
                        isFirstSection = false;
                        continue;
                    } else if (sectionString.equals("..")) {
                        suite = suite.parent;
                        isFirstSection = false;
                        if (suite == null) {
                            // We're getting out of the tree.
                            // Eventually this should be supported.
                            importsSuite = null;
                            successfullyImported = false;
                            return;
                        }
                        continue;
                    } else {
                        List<HighElement> children = new ArrayList<>(suite.children);
                        if (isFirstSection) {
                            ExternalResourcesElement resourceElement = MainForm.INSTANCE.getExternalResourcesElement();
                            List<HighElement> resourceChildren = resourceElement.children;
                            children.addAll(resourceChildren);

                            // also traverse through the 2nd level of resource location
                            resourceChildren.forEach(resourceChild -> children.addAll(resourceChild.children));
                        }
                        for (HighElement child : children) {
                            MatchStatus matchStatus = getMatchStatus(child.getShortName(), sectionString);
                            if (matchStatus == MatchStatus.MATCHES_AND_IS_DIRECTORY) {
                                suite = child;
                                isFirstSection = false;
                                continue outerFor;
                            } else if (matchStatus == MatchStatus.MATCHES_AND_IS_FILE) {
                                suite = child;
                                break outerFor;
                            }
                        }
                    }
                    importsSuite = null;
                    successfullyImported = false;
                    return;
                }
                if (suite instanceof Suite) {
                    Suite asSuite = (Suite) suite;
                    gatherIntoThis.add(new ResourceFileKeywordSource(asSuite));
                    successfullyImported = true;
                    importsSuite = asSuite;
                    if (asSuite.importedResourcesLastRecursedDuringIteration == iterationCounter) {
                        // Already been there.
                    } else {
                        for (ImportedResource ir : asSuite.getImportedResources()) {
                            ir.gatherSelfInto(gatherIntoThis, asSuite, iterationCounter);
                        }
                    }
                } else {

                    importsSuite = null;
                    successfullyImported = false;
                }
                break;
            default:
                // not yet supported
                throw new RuntimeException("You can only import resources or libraries with Snowride. This error cannot happen.");
        }
    }

    private MatchStatus getMatchStatus(String shortName, String sectionString) {
        if (Extensions.toInvariant(shortName).equals(sectionString)) {
            return MatchStatus.MATCHES_AND_IS_DIRECTORY;
        }
        if (Extensions.toInvariant(shortName + ".robot").equals(sectionString)) {
            return MatchStatus.MATCHES_AND_IS_FILE;
        }
        if (Extensions.toInvariant(shortName + ".txt").equals(sectionString) && Settings.getInstance().cbAlsoImportTxtFiles) {
            return MatchStatus.MATCHES_AND_IS_FILE;
        }
        return MatchStatus.NO_MATCH;
    }

    private enum MatchStatus {
        MATCHES_AND_IS_FILE,
        MATCHES_AND_IS_DIRECTORY,
        NO_MATCH
    }
}

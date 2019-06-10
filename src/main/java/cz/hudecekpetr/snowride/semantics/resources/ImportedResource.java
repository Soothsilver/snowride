package cz.hudecekpetr.snowride.semantics.resources;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.semantics.ImportException;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

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
                    // Unknown library
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
                            // we're getting out of the tree
                            importsSuite = null;
                            successfullyImported = false;
                            return;
                            // Eventually should be supported....
                            //throw new ImportException("Snowride cannot import resource files from outside the root folder.");
                        }
                        continue;
                    } else {
                        for (HighElement child : suite.children) {
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

                        if (isFirstSection) {
                            for (HighElement importedResourceFolder : MainForm.INSTANCE.getExternalResourcesElement().children) {
                                for (HighElement child : importedResourceFolder.children) {
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
                        }
                    }
                    importsSuite = null;
                    successfullyImported = false;
                    return;
                    // throw new ImportException("Path '" + path + "' doesn't lead to a resource file because '" + section.toString() + "' is not a suite.");
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
// TODO report these errors somehow
                    //                    throw new ImportException("Path '" + path + "' somehow leads to something that is not a suite.");
                }
                break;
            default:
                // not yet supported
                throw new ImportException("You can only import resources or libraries with Snowride. This error cannot happen.");
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

    enum MatchStatus {
        MATCHES_AND_IS_FILE,
        MATCHES_AND_IS_DIRECTORY,
        NO_MATCH
    }
}

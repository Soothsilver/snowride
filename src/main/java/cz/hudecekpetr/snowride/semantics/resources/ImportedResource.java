package cz.hudecekpetr.snowride.semantics.resources;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.ImportException;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Suite;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

public class ImportedResource {
    private final String name;
    private final ImportedResourceKind kind;

    private static long indexOfIteration = 0;

    public ImportedResource(String name, ImportedResourceKind kind) {
        this.name = name;
        this.kind = kind;
    }

    public static long incrementAndGetIterationCount() {
        return ++indexOfIteration;
    }

    public void gatherSelfInto(Set<KeywordSource> gatherIntoThis, Suite owningSuite, long iterationCounter) {
        owningSuite.importedResourcesLastRecursedDuringIteration = iterationCounter;
        switch (kind) {
            case LIBRARY:
                if (ExternalLibrary.otherPackedInLibraries.containsKey(name)) {
                    gatherIntoThis.add(new LibraryKeywordSource(ExternalLibrary.otherPackedInLibraries.get(name)));
                } else {
                    // Unknown library
                    // Ignore it here. We'll put in the error list and such later.
                }
                break;
            case RESOURCE:
                Path path = Paths.get(name);
                HighElement suite = owningSuite;
                if (suite instanceof FileSuite) {
                    suite = suite.parent;
                }
                outerFor: for (Path section : path) {
                    String sectionString = Extensions.toInvariant(section.toString());
                    if (sectionString.equals(".")) {
                        continue;
                    }
                    else if (sectionString.equals("..")) {
                        suite = suite.parent;
                        if (suite == null) {
                            // we're getting out of the tree
                            throw new ImportException("Snowride cannot import resource files from outside the root folder.");
                        }
                        continue;
                    }
                    else {
                        for (HighElement child : suite.children) {
                            if (Extensions.toInvariant(child.getShortName()).equals(sectionString)) {
                                suite = child;
                                continue outerFor;
                            }
                            if (Extensions.toInvariant(child.getShortName() + ".robot").equals(sectionString)) {
                                suite = child;
                                break outerFor;
                            }
                        }
                    }
                    // TODO add it to import errors
                    return;
                    // throw new ImportException("Path '" + path + "' doesn't lead to a resource file because '" + section.toString() + "' is not a suite.");
                }
                if (suite instanceof Suite) {
                    Suite asSuite = (Suite)suite;
                    gatherIntoThis.add(new ResourceFileKeywordSource(asSuite));
                    if (asSuite.importedResourcesLastRecursedDuringIteration == iterationCounter) {
                        // Already been there.
                    } else {
                        for (ImportedResource ir : asSuite.getImportedResources()) {
                            ir.gatherSelfInto(gatherIntoThis, asSuite, iterationCounter);
                        }
                    }
                } else {
                    throw new ImportException("Path '" + path + "' somehow leads to something that is not a suite.");
                }
                break;
            default:
                // not yet supported
                throw new ImportException("You can only import resources or libraries with Snowride. This error cannot happen.");
        }
    }
}

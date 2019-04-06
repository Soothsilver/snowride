package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Suite;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ImportedResource {
    private final String name;
    private final ImportedResourceKind kind;

    public ImportedResource(String name, ImportedResourceKind kind) {
        this.name = name;
        this.kind = kind;
    }

    public Stream<? extends IKnownKeyword> getImportedKeywords(Suite owningSuite) {
        switch (kind) {
            case LIBRARY:
                if (ExternalLibrary.otherPackedInLibraries.containsKey(name)) {
                    return ExternalLibrary.otherPackedInLibraries.get(name).keywords.stream();
                } else {
                    // unknown library
                    throw new ImportException("Library '" + name + "' is not built-in or on the additional path.");
                }
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
                    }
                    else {
                        for (HighElement child : suite.children) {
                            if (Extensions.toInvariant(child.shortName).equals(sectionString)) {
                                suite = child;
                                continue outerFor;
                            }
                            if (Extensions.toInvariant(child.shortName + ".robot").equals(sectionString)) {
                                suite = child;
                                break outerFor;
                            }
                        }
                    }
                    throw new ImportException("Path '" + path + "' doesn't lead to a resource file because '" + section.toString() + "' is not a suite.");
                }
                if (suite instanceof Suite) {
                    Suite asSuite = (Suite)suite;
                    return asSuite.getKeywordsPermissibleInSuite();
                } else {
                    throw new ImportException("Path '" + path + "' somehow leads to something that is not a suite.");
                }
            default:
                // not yet supported
                throw new ImportException("You can only import resources or libraries with Snowride. This error cannot happen.");
        }
    }
}

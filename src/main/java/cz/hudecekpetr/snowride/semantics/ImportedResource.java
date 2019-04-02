package cz.hudecekpetr.snowride.semantics;

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
                    return Stream.empty();
                }
            case RESOURCE:
                Path path = Paths.get(name);
                HighElement suite = owningSuite;
                for (Path section : path) {
                    String s = section.toString();
                    if (s.equals(".")) {
                        continue;
                    }
                    else if (s.equals("..")) {
                        suite = suite.parent;
                        if (suite == null) {
                            // we're getting out of the tree
                            return Stream.empty();
                        }
                    }
                    else {
                        for (HighElement child : suite.children) {
                            if (child.shortName.equals(section)) {
                                // TODO canonicalizing names
                                suite = child;
                                continue;
                            }
                        }
                    }
                    return Stream.empty();
                }
                if (suite instanceof Suite) {
                    Suite asSuite = (Suite)suite;
                    return asSuite.getSelfKeywords();
                } else {
                    return Stream.empty();
                }
            default:
                // not yet supported
                return Stream.empty();
        }
    }
}

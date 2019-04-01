package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;

import java.util.stream.Stream;

public class ImportedResource {
    private final String name;
    private final ImportedResourceKind kind;

    public ImportedResource(String name, ImportedResourceKind kind) {

        this.name = name;
        this.kind = kind;
    }

    public Stream<? extends IKnownKeyword> getImportedKeywords() {
        switch (kind) {
            case LIBRARY:
                if (ExternalLibrary.otherPackedInLibraries.containsKey(name)) {
                    return ExternalLibrary.otherPackedInLibraries.get(name).keywords.stream();
                } else {
                    // unknown library
                    return Stream.empty();
                }
            case RESOURCE:
            default:
                // not yet supported
                return Stream.empty();
        }
    }
}

package cz.hudecekpetr.snowride.semantics.resources;

import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.VariableCompletionOption;
import cz.hudecekpetr.snowride.semantics.externallibraries.ExternalLibrary;

import java.util.stream.Stream;

public class LibraryKeywordSource extends KeywordSource {
    private ExternalLibrary externalLibrary;

    public LibraryKeywordSource(ExternalLibrary externalLibrary) {
        this.externalLibrary = externalLibrary;
    }

    @Override
    public int hashCode() {
        return externalLibrary.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryKeywordSource that = (LibraryKeywordSource) o;
        return externalLibrary.equals(that.externalLibrary);
    }

    @Override
    public Stream<? extends IKnownKeyword> getAllKeywords() {
        return externalLibrary.keywords.stream();
    }

    @Override
    public Stream<VariableCompletionOption> getAllVariables() {
        return Stream.empty();
    }
}

package cz.hudecekpetr.snowride.semantics.resources;

import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.VariableCompletionOption;
import cz.hudecekpetr.snowride.tree.highelements.Suite;

import java.util.Objects;
import java.util.stream.Stream;

public class ResourceFileKeywordSource extends KeywordSource {
    private Suite suite;

    public ResourceFileKeywordSource(Suite suite) {
        this.suite = suite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceFileKeywordSource that = (ResourceFileKeywordSource) o;
        return suite.equals(that.suite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suite);
    }

    @Override
    public Stream<? extends IKnownKeyword> getAllKeywords() {
        return suite.getSelfKeywords();
    }

    @Override
    public Stream<VariableCompletionOption> getAllVariables() {
        return suite.getSelfVariables();
    }
}

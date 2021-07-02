package cz.hudecekpetr.snowride.semantics.resources;

import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.VariableCompletionOption;

import java.util.stream.Stream;

public abstract class KeywordSource {
    public abstract Stream<? extends IKnownKeyword> getAllKeywords();

    public abstract Stream<VariableCompletionOption> getAllVariables();
}

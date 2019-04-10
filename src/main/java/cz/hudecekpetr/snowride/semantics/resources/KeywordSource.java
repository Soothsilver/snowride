package cz.hudecekpetr.snowride.semantics.resources;

import cz.hudecekpetr.snowride.semantics.IKnownKeyword;

import java.util.stream.Stream;

public abstract class KeywordSource {
    public abstract Stream<? extends IKnownKeyword> getAllKeywords();
}

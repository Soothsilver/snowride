package cz.hudecekpetr.snowride.semantics.resources;

import cz.hudecekpetr.snowride.semantics.IKnownKeyword;
import cz.hudecekpetr.snowride.semantics.codecompletion.TestCaseSettingOption;

import java.util.stream.Stream;

public class TestCaseSettingOptionLibrarySource extends KeywordSource {
    @Override
    public Stream<? extends IKnownKeyword> getAllKeywords() {
        return TestCaseSettingOption.allOptions.stream();
    }
}

package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.semantics.Setting;

import java.util.ArrayList;
import java.util.List;

public class RobotFile {
    public List<RobotSection> sections = new ArrayList<>();
    public List<Exception> errors = new ArrayList<>();

    public List<HighElement> getHighElements() {
        List<HighElement> he = new ArrayList<>();
        for(RobotSection section : sections) {
            he.addAll(section.getHighElements());
        }
        return he;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for(RobotSection robotSection : sections) {
            robotSection.serializeInto(sb);
        }
        if (errors.size() > 0) {
            throw new RuntimeException("There were parse errors. Editing or saving is not possible.");
        }
        String str = Extensions.removeFinalNewlineIfAny(sb.toString());
        str = Extensions.normalizeLineEndings(str);
        return str;
    }

    public TestCasesSection findOrCreateTestCasesSection() {
        for (RobotSection section : sections) {
            if (section.header.sectionKind == SectionKind.TEST_CASES) {
                return (TestCasesSection) section;
            }
        }
        TestCasesSection newSection = new TestCasesSection(new SectionHeader(SectionKind.TEST_CASES, "*** Test Cases ***\n"), new ArrayList<>());
        sections.add(newSection);
        return newSection;
    }

    public KeywordsSection findOrCreateKeywordsSection() {
        for (RobotSection section : sections) {
            if (section.header.sectionKind == SectionKind.KEYWORDS) {
                return (KeywordsSection) section;
            }
        }
        KeywordsSection newSection = new KeywordsSection(new SectionHeader(SectionKind.KEYWORDS, "*** Keywords ***\n"), new ArrayList<>());
        sections.add(newSection);
        return newSection;
    }

    public void analyzeSemantics(Suite suite) {
        KeyValuePairSection settings = this.findSettingsSection();
        if (settings != null) {
            for (Setting s : settings.createSettings()) {
                if (s.key.equalsIgnoreCase("Documentation")) {
                    suite.semanticsDocumentation = s.firstValue; // TODO make it all values concatenated
                }
            }
        }
        // TODO for test cases
    }

    private KeyValuePairSection findSettingsSection() {
        for (RobotSection section : sections) {
            if (section.header.sectionKind == SectionKind.SETTINGS) {
                return (KeyValuePairSection) section;
            }
        }
        return null;
    }
}

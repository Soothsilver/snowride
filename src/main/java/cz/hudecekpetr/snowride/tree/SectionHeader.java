package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.lexer.LogicalLine;

public class SectionHeader {
    public LogicalLine line;
    public SectionKind sectionKind;
    public String followupEmptyLines = "";

    public SectionHeader(LogicalLine line) {
        this.line = line;
        this.sectionKind = determineSectionKind();
    }

    public SectionKind determineSectionKind() {
        String content = line.cells.get(0).contents.toLowerCase();
        if (content.startsWith("*")) {
            if (content.contains("setting")) {
                return SectionKind.SETTINGS;
            }
            else if (content.contains("variable")) {
                return SectionKind.VARIABLES;
            }
            else if (content.contains("test case")) {
                return SectionKind.TEST_CASES;
            }
            else if (content.contains("keyword")) {
                return SectionKind.KEYWORDS;
            }
            else if (content.contains("comment")) {
                return SectionKind.COMMENTS;
            }
            throw new RuntimeException("Unrecognized section name.");
        } else {
            throw new RuntimeException("This is not a section start.");
        }
    }

    public SectionKind getSectionKind() {
        return sectionKind;
    }
}

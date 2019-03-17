package cz.hudecekpetr.snowride.parser;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.RobotFile;
import cz.hudecekpetr.snowride.tree.TestDataSection;
import cz.hudecekpetr.snowride.tree.TextOnlyTestDataSection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<LogicalLine> stream;
    private int position;

    public Parser(List<LogicalLine> stream) {
        this.stream = stream;
    }

    public RobotFile fileSuite() {
        RobotFile robotFile = new RobotFile();
        while (position < stream.size()) {
            TestDataSection testDataSection = testDataSection();
            if (testDataSection == null) {
                break;
            }
            robotFile.sections.add(testDataSection);
        }
        return robotFile;
    }

    private TestDataSection testDataSection() {
        SectionHeader header = parseTestDataSectionHeader();
        switch (header.getSectionKind()) {
            case TEST_CASES:
                return testCasesSection(header);
            default:
                return textBasedSection(header);
        }
    }

    private TestDataSection testCasesSection(SectionHeader header) {
        while (position < stream.size()) {
            LogicalLine line = stream.get(position);
            if (line.isStartOfSection()) {
                // End of section
                break;
            }

            position++;
        }
    }

    private TestDataSection textBasedSection(SectionHeader header) {
        List<LogicalLine> ignoredLines = new ArrayList<>();
        while (position < stream.size()) {
            LogicalLine line = stream.get(position);
            if (line.isStartOfSection()) {
                // End of section
                break;
            }
            ignoredLines.add(line);
            position++;
        }
        return new TextOnlyTestDataSection(header, ignoredLines);
    }

    private SectionHeader parseTestDataSectionHeader() {
        SectionHeader sh = new SectionHeader(stream.get(position));
        position++;
        return sh;
    }
}

package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.lexer.LogicalLine;

import java.util.List;

public class TestCasesSection extends RobotSection {
    private final List<TestCase> testCases;

    public TestCasesSection(SectionHeader header, List<TestCase> testCases) {
        super(header);
        this.testCases = testCases;
    }
}

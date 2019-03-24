package cz.hudecekpetr.snowride.listener;

import cz.hudecekpetr.snowride.antlr.RobotBaseListener;
import cz.hudecekpetr.snowride.antlr.RobotParser;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.tree.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class AntlrListener extends RobotBaseListener implements ANTLRErrorListener {

    public List<Exception> errors = new ArrayList<>();

    @Override
    public void exitFile(RobotParser.FileContext ctx) {
        ctx.File = new RobotFile();
        for(RobotParser.SectionContext section : ctx.section()) {
            ctx.File.sections.add(section.Section);
        }
    }

    @Override
    public void exitSection(RobotParser.SectionContext ctx) {
        if (ctx.keywordsSection() != null) {
            ctx.Section = ctx.keywordsSection().Section;
        } else if (ctx.testCasesSection() != null) {
            ctx.Section = ctx.testCasesSection().Section;
        } else if (ctx.unknownSection() != null) {
            ctx.Section = ctx.unknownSection().Section;
        }
    }

    @Override
    public void exitUnknownSection(RobotParser.UnknownSectionContext ctx) {
        LogicalLine ll = new LogicalLine();
        ll.cells.add(new Cell("nondef",""));
        ctx.Section = new TextOnlyRobotSection(new SectionHeader(ll), "");
    }

    @Override
    public void exitTestCasesSection(RobotParser.TestCasesSectionContext ctx) {
        SectionHeader header = ctx.testCasesHeader().SectionHeader;
        StringBuilder text = new StringBuilder();
        if (ctx.emptyLines() != null) {
            header.followupEmptyLines = ctx.emptyLines().Trivia;
        }
        List<TestCase> tcc = new ArrayList<>();
        // TODO  do this properly
        ctx.testCase().stream().map(ctxx -> ctxx.TestCase).forEachOrdered(tcc::add);
        // TODO do this properly
        ctx.Section = new TestCasesSection(header, tcc);
    }

    @Override
    public void exitTestCasesHeader(RobotParser.TestCasesHeaderContext ctx) {
        ctx.SectionHeader = new SectionHeader(SectionKind.TEST_CASES, ctx.getText());
    }

    @Override
    public void exitKeywordsSection(RobotParser.KeywordsSectionContext ctx) {
        SectionHeader header = ctx.keywordsHeader().SectionHeader;
        StringBuilder text = new StringBuilder();
        if (ctx.emptyLines() != null) {
            header.followupEmptyLines = ctx.emptyLines().Trivia;
        }
        ctx.testCase().stream().map(ctxx -> ctxx.TestCase).forEachOrdered(tc -> {
            // TODO  do this properly
            text.append(tc.name);
        });
        ctx.Section = new TextOnlyRobotSection(header, text.toString());
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        errors.add(e);
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

    }
}

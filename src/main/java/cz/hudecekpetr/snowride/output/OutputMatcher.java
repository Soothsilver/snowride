package cz.hudecekpetr.snowride.output;

import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.robotframework.jaxb.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cz.hudecekpetr.snowride.Extensions.toInvariant;

public class OutputMatcher {

    public static void matchLines(HighElement highElement, ObservableList<LogicalLine> lines) {
        if (Settings.getInstance().disableOutputParsing) {
            return;
        }
        lines.forEach(LogicalLine::clearOutputFields);

        if (highElement instanceof Suite && highElement.outputElement != null) {
            OutputSuite outputSuite = (OutputSuite) highElement.outputElement;
            for (Keyword keyword : outputSuite.getKeywords()) {
                switch (keyword.getType()) {
                    case SETUP:
                    case SETUP_RF3:
                        findLineAndAttachKeyword("Suite Setup", lines, 0, keyword);
                        break;
                    case TEARDOWN:
                    case TEARDOWN_RF3:
                        findLineAndAttachKeyword("Suite Teardown", lines, 0, keyword);
                        break;
                    case TEST_SETUP:
                        findLineAndAttachKeyword("Test Setup", lines, 0, keyword);
                        break;
                    case TEST_TEARDOWN:
                        findLineAndAttachKeyword("Test Teardown", lines, 0, keyword);
                        break;
                }
            }
            return;
        }
        OutputElement element = highElement != null && highElement.outputElement != null ? highElement.outputElement : MainForm.INSTANCE.navigationStack.currentOutputElement();

        // Special workarounds #1 - unwrap 'Run Keyword And Expect Error'
        if (element != null && element.getName().equals("Run Keyword And Expect Error")) {
            element = element.getElements().get(0);
        }

        if (element != null && highElement.getShortName().equals(element.getName())) {
            if (element.getElements().isEmpty()) {
                setNotRunStatus(lines);
                return;
            }
            LinkedList<OutputElement> kwOrForOrIf = new LinkedList<>(element.getElements());

            // Handle Test Setup/Teardown
            List<Keyword> setupAndTeardown = kwOrForOrIf.stream()
                    .filter(outElement -> outElement instanceof Keyword && ((Keyword) outElement).isSetupOrTearDown())
                    .map(outputElement -> (Keyword) outputElement)
                    .collect(Collectors.toList());
            kwOrForOrIf.removeAll(setupAndTeardown);
            for (Keyword keyword : setupAndTeardown) {
                switch (keyword.getType()) {
                    case SETUP:
                    case SETUP_RF3:
                        findLineAndAttachKeyword("[Setup]", lines, 1, keyword);
                        break;
                    case TEARDOWN:
                    case TEARDOWN_RF3:
                        findLineAndAttachKeyword("[Teardown]", lines, 1, keyword);
                        break;
                }
            }
            if (kwOrForOrIf.isEmpty()) {
                setNotRunStatus(lines);
                return;
            }

            List<LogicalLine> logicalLines = filteredLines(lines);
            for (int i = 0; i < logicalLines.size(); i++) {
                int linesToSkip = tryToMatchLogicalLineToOutputKeyword(element, logicalLines, kwOrForOrIf, i);
                i += linesToSkip;
            }
        }
    }

    private static int tryToMatchLogicalLineToOutputKeyword(OutputElement element, List<LogicalLine> logicalLines, LinkedList<OutputElement> kwOrForOrIf, int logicalLineIndex) {
        LogicalLine line = logicalLines.get(logicalLineIndex);
        if (line.cells.get(1).contents.startsWith("[Arguments]") && element instanceof Keyword) {
            Keyword keyword = (Keyword) element;
            line.keywordArguments = keyword.getArguments();
            return 0;
        }

        if (!kwOrForOrIf.isEmpty()) {
            OutputElement outputElement = kwOrForOrIf.pop();

            // IF statement support
            if (outputElement instanceof If) {
                If ifElement = (If) outputElement;
                int ifBranchIndex = 0;
                for (IfBranch ifBranch : ifElement.getBranches()) {
                    LogicalLine ifBranchLine = logicalLines.get(logicalLineIndex + ifBranchIndex);
                    if (ifBranchLine.asLineArgs().get(0).equalsIgnoreCase(toInvariant(ifBranch.getType().value()))) {
                        ifBranchLine.status = ifBranch.getStatus().getStatus();
                        kwOrForOrIf.addAll(0, ifBranch.getKwOrForOrIf());
                        ifBranchIndex++;
                        for (OutputElement ifBranchElement : ifBranch.getKwOrForOrIf()) {
                            tryToMatchLogicalLineToOutputKeyword(element, logicalLines, kwOrForOrIf, logicalLineIndex + ifBranchIndex);
                            ifBranchIndex++;
                        }
                    }
                }
                return ifBranchIndex;
            }

            // FOR statement support
            if (outputElement instanceof For) {
                For loop = (For) outputElement;
                List<String> lineArgs = line.asLineArgs();
                if (lineArgs.get(0).equalsIgnoreCase("FOR")) {
                    List<ForIteration> iterations = loop.getElements().stream()
                            .filter(elem -> elem instanceof ForIteration)
                            .map(sc -> (ForIteration) sc)
                            .collect(Collectors.toList());

                    // Try to find failing iteration OR use the latest
                    Optional<ForIteration> failedIteration = iterations.stream().filter(elem -> elem.getStatus().getStatus() == BodyItemStatusValue.FAIL).findFirst();
                    ForIteration iteration = failedIteration.orElseGet(() -> iterations.stream().reduce((first, second) -> second).orElse(null));
                    if (iteration != null && !iteration.getKwOrForOrIf().isEmpty()) {
                        line.status = iteration.getStatus().getStatus();
                        line.forIterations = iterations;
                        kwOrForOrIf.addAll(0, iteration.getKwOrForOrIf());
                    } else {
                        // Failure in For loop declaration
                        line.status = loop.getStatus().getStatus();
                        line.forLoop = loop;
                    }

                    // Look for 'END' and match lines inside 'FOR' with last successful/failing iteration
                    int loopIndex = 1;
                    while (!logicalLines.get(logicalLineIndex + loopIndex).asLineArgs().get(0).equalsIgnoreCase("END")) {
                        if (iteration != null) {
                            tryToMatchLogicalLineToOutputKeyword(element, logicalLines, kwOrForOrIf, logicalLineIndex + loopIndex);
                        }
                        loopIndex++;
                    }
                    return  loopIndex;
                }
                return 0;
            }

            // FOR statement support (RobotFramework 3.X)
            if (outputElement instanceof Keyword && ((Keyword) outputElement).getType() == KeywordType.FOR) {
                Keyword keyword = (Keyword) outputElement;
                List<String> lineArgs = line.asLineArgs();
                if (lineArgs.get(0).equalsIgnoreCase("FOR")) {
                    List<Keyword> iterations = keyword.getKwOrForOrIf().stream().filter(o -> o instanceof Keyword).map(o -> (Keyword) o).collect(Collectors.toList());

                    // Try to find failing iteration OR use the latest
                    Optional<Keyword> failedIteration = iterations.stream().filter(elem -> elem.getStatus().getStatus() == BodyItemStatusValue.FAIL).findFirst();
                    Keyword iteration = failedIteration.orElseGet(() -> iterations.stream().reduce((first, second) -> second).orElse(null));
                    if (iteration != null && !iteration.getKwOrForOrIf().isEmpty()) {
                        line.status = iteration.getStatus().getStatus();
                        line.keyword = keyword;
                        kwOrForOrIf.addAll(0, iteration.getKwOrForOrIf());
                    } else {
                        // Failure in For loop declaration
                        line.status = keyword.getStatus().getStatus();
                        line.keyword = keyword;
                    }

                    // Look for 'END' and match lines inside 'FOR' with last successful/failing iteration
                    int loopIndex = 1;
                    while (!logicalLines.get(logicalLineIndex + loopIndex).asLineArgs().get(0).equalsIgnoreCase("END")) {
                        if (iteration != null) {
                            tryToMatchLogicalLineToOutputKeyword(element, logicalLines, kwOrForOrIf, logicalLineIndex + loopIndex);
                        }
                        loopIndex++;
                    }
                    return  loopIndex;
                }

            }

            Keyword keyword = (Keyword) outputElement;
            if (!line.matchesKeyword(keyword)) {
                line.doesNotMatch = true;
            }

            attachKeywordToLine(line, keyword);
        }
        return 0;
    }

    private static void setNotRunStatus(ObservableList<LogicalLine> lines) {
        for (LogicalLine line : filteredLines(lines)) {
            line.status = BodyItemStatusValue.NOT_RUN;
        }
    }

    private static List<LogicalLine> filteredLines(ObservableList<LogicalLine> lines) {
        return lines.stream()
                .filter(line -> line.cells.size() > 1) // lines with only line number cells
                .filter(line -> line.cells.subList(1, line.cells.size()).stream().anyMatch(cell -> {
                    String contents = cell.contents;
                    return StringUtils.isNotBlank(contents);
                })) // empty lines
                .filter(line -> !line.cells.get(1).contents.startsWith("#")) // comment lines
                .filter(line -> !line.cells.get(1).contents.matches("^\\[.*]$")) // test case setting lines starting with "["
                .collect(Collectors.toList());
    }

    private static void findLineAndAttachKeyword(String cellContent, ObservableList<LogicalLine> lines, int cellIndex, Keyword keyword) {
        lines.stream().filter(line -> line.cells.size() > cellIndex && line.cells.get(cellIndex).contents.equalsIgnoreCase(cellContent)).findFirst().ifPresent(line -> {
            attachKeywordToLine(line, keyword);
        });
    }

    private static void attachKeywordToLine(LogicalLine line, Keyword keyword) {
        line.keyword = keyword;
        line.status = keyword.getStatus().getStatus();
    }
}

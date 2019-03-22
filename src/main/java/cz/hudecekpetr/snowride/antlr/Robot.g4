grammar Robot;

@parser::header {
    import cz.hudecekpetr.snowride.tree.*;
}

file returns [RobotFile File]: section* EOF ;
section returns [RobotSection Section]: testCasesSection | keywordsSection | unknownSection;
unknownSection returns [TextOnlyRobotSection Section]: 'nondef';
// Keywords
keywordsSection returns [TextOnlyRobotSection Section] : keywordsHeader emptyLines? testCase*;
keywordsHeader returns [SectionHeader SectionHeader]: KEYWORDS_CELL restOfRow;

// Test cases
testCasesSection returns [TestCasesSection Section]: testCasesHeader emptyLines? testCase*;
testCase returns [TestCase TestCase]: testCaseName testCaseSettings testCaseSteps emptyLines?;
testCasesHeader returns [SectionHeader SectionHeader]: TEST_CASES_CELL restOfRow;
testCaseName returns [String Name]: ANY_CELL restOfRow;
testCaseSettings: testCaseSetting*;
testCaseSteps: step*;
testCaseSetting: CELLSPACE TEST_CASE_SETTING_CELL restOfRow;
step: CELLSPACE ANY_CELL restOfRow;

// General
endOfLine: LINE_SEPARATOR | EOF;
restOfRow: (CELLSPACE (ANY_CELL CELLSPACE?)* endOfLine) | endOfLine;
emptyLines returns [String Trivia]: emptyLine+;
emptyLine: ((CELLSPACE | SINGLE_SPACE)+ endOfLine) | LINE_SEPARATOR;


fragment CHARACTER: [A-Za-z0-9_${}[\]=@&\\*:];
fragment TEXT: (CHARACTER ' '?)* CHARACTER;

COMMENT: '#' [^\n]* LINE_SEPARATOR;
TEST_CASES_CELL: '*'[* ]*'Test Cases'(([* ]*'*')|);
LINE_SEPARATOR: ('\n'|'\r\n');
CELLSPACE: ('  '[ \t]*)|('\t'[ \t]*);
TEST_CASE_SETTING_CELL: '[' TEXT ']';
KEYWORDS_CELL: '*'[* ]*'Keywords'(([* ]*'*')|);
ANY_CELL: TEXT;
SINGLE_SPACE: ' ';
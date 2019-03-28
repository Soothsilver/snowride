grammar Robot;

@parser::header {
    import cz.hudecekpetr.snowride.tree.*;
    import cz.hudecekpetr.snowride.lexer.*;
}

file returns [RobotFile File]: section* EOF ;
section returns [RobotSection Section]: testCasesSection | keywordsSection | settingsSection | variablesSection | unknownSection;
unknownSection returns [TextOnlyRobotSection Section]: 'nondef';

// Configuration sections
settingsSection returns [KeyValuePairSection Section] : settingsHeader emptyLines?  keyValuePair* emptyLines?;
settingsHeader returns [SectionHeader SectionHeader]: SETTINGS_CELL restOfRow;
variablesSection returns [KeyValuePairSection Section] : variablesHeader emptyLines?  keyValuePair* emptyLines?;
variablesHeader returns [SectionHeader SectionHeader]: VARIABLES_CELL restOfRow;
keyValuePair returns [LogicalLine Line]: ANY_CELL restOfRow;

// Keywords
keywordsSection returns [KeywordsSection Section] : keywordsHeader emptyLines? testCase*;
keywordsHeader returns [SectionHeader SectionHeader]: KEYWORDS_CELL restOfRow;

// Test cases
testCasesSection returns [TestCasesSection Section]: testCasesHeader emptyLines? testCase*;
testCase returns [Scenario TestCase]: testCaseName /*testCaseSettings*/ testCaseSteps emptyLines?;
testCasesHeader returns [SectionHeader SectionHeader]: TEST_CASES_CELL restOfRow;
testCaseName returns [Cell Cell]: ANY_CELL restOfRow;
//testCaseSettings returns [Lines Lines]: testCaseSetting*;
testCaseSteps returns [Lines Lines]: emptyLines? (step emptyLines?)*;
//testCaseSetting returns [LogicalLine LogicalLine]: CELLSPACE TEST_CASE_SETTING_CELL restOfRow;
step returns [LogicalLine LogicalLine]: CELLSPACE ANY_CELL restOfRow;

// General
endOfLine: LINE_SEPARATOR | EOF;
restOfRow returns [LogicalLine Line]: (CELLSPACE (ANY_CELL CELLSPACE?)* (COMMENT | endOfLine)) | endOfLine;
emptyLines returns [String Trivia]: emptyLine+;
emptyLine: ((CELLSPACE | SINGLE_SPACE)+ (COMMENT | endOfLine)) | LINE_SEPARATOR | COMMENT;


fragment CHARACTER: [\u0001-\u0008\u000e-\u001f\u0021-\u007f\u0080-\uffff];//[^ \t\r\n];//[\u0000-\u0250];
fragment TEXT: (CHARACTER ' '?)* CHARACTER;
fragment BEFORE_SECTION_HEADER:'*'[* ]*;
fragment AFTER_SECTION_HEADER:(([* ]*'*')|);

COMMENT: '#'.*? LINE_SEPARATOR;
TEST_CASES_CELL: BEFORE_SECTION_HEADER'Test Cases'AFTER_SECTION_HEADER;
KEYWORDS_CELL: BEFORE_SECTION_HEADER'Keywords'AFTER_SECTION_HEADER;
SETTINGS_CELL: BEFORE_SECTION_HEADER'Settings'AFTER_SECTION_HEADER;
VARIABLES_CELL: BEFORE_SECTION_HEADER'Variables'AFTER_SECTION_HEADER;
LINE_SEPARATOR: ('\n'|'\r\n');
CELLSPACE: ('  '[ \t]*)  |   ('\t'[ \t]*) | (' ''\t'[ \t]*);
//TEST_CASE_SETTING_CELL: '[' TEXT ']';
ANY_CELL: TEXT;
SINGLE_SPACE: ' ';
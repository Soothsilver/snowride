grammar robot;

fragment CHARACTER: [A-Za-z0-9_${}[\]=@&\\];
fragment TEXT: (CHARACTER ' '?)* CHARACTER;

COMMENT: '#' [^\n]* LINE_SEPARATOR;
TEST_CASES_CELL: '*'[* ]*'Test Cases'(([* ]*'*')|);
LINE_SEPARATOR: ('\n'|'\r\n');
CELLSPACE: ('  '[ \t]*)|('\t'[ \t]*);
TEST_CASE_SETTING_CELL: '[' TEXT ']';
KEYWORDS_CELL: '*'[* ]*'Keywords'(([* ]*'*')|);
ANY_CELL: TEXT;
SINGLE_SPACE: ' ';

file : section* EOF;
section: test_cases_section | keywords_section | unknown_section;
unknown_section: 'nondef';
// Keywords
keywords_section: keywords_header empty_lines? test_case*;
keywords_header: KEYWORDS_CELL rest_of_row;

// Test cases
test_cases_section: test_cases_header empty_lines? test_case*;
test_case: test_case_name test_case_settings test_case_steps empty_lines?;
test_cases_header: TEST_CASES_CELL rest_of_row;
test_case_name: ANY_CELL rest_of_row;
test_case_settings: test_case_setting*;
test_case_steps: step*;
test_case_setting: CELLSPACE TEST_CASE_SETTING_CELL rest_of_row;
step: CELLSPACE ANY_CELL rest_of_row;

// General
end_of_line: LINE_SEPARATOR | EOF;
rest_of_row: (CELLSPACE (ANY_CELL CELLSPACE?)* end_of_line) | end_of_line;
empty_lines: empty_line+;
ahoj: BAMB doredan;
empty_line: (CELLSPACE | SINGLE_SPACE)* end_of_line;
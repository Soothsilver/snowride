*** Variables ***
${sub_suite_variable_01}    SUB_SUITE_01
${sub_suite_variable_02}    SUB_SUITE_02

*** Test Cases ***
Sub-Suite Test 01
    Log    ${sub_suite_variable_01}

Sub-Suite Test 02
    Log    ${sub_suite_variable_02}

Sub-Suite Test 03
    Log    ${sub_suite_variable_03}


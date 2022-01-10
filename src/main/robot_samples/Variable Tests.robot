*** Settings ***
Resource          resourceFile01.robot

*** Variables ***
${suite_variable_01}    SUITE_01
${suite_variable_02}    SUITE_02

*** Test Cases ***
Variables Test
    Log    ${suite_variable_01}
    Log    ${resource_variable_01}
    Log    ${undefined_variable}

Parent Suite variable
    Log    ${parent_suite_variable_01}

Variable highliting
    ${test}    Set Variable    CONTENT_OF_VARIABLE_TEST
    Log    ${test}
    Log    ${suite_variable_01}
    Log    Value of variable 'suite_variable_01' is ${suite_variable_01}
    Log    Value of 'test' = ${test}
    Log    ${suite_variable_02}
    Log    Value of variable 'suite_variable_02' is @{suite_variable_02}
    Log    Value of variable 'suite_variable_02' is &{suite_variable_02}
    Log    Environment variables should not be highlighted: %{suite_variable_02}
    Log    Very long line for Full-Text search testing purposes. Environment variables should not be highlighted: %{suite_variable_02}
    Log    ${test-undefinedVariable}

*** Keywords ***

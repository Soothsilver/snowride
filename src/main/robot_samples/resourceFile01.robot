*** Settings ***
Library		Collections

*** Variables ***
${resource_variable_01}    RESOURCE_01
${resource_variable_02}    RESOURCE_02

*** Keywords ***
Return sum of numbers
    [Arguments]	${a}	${b}
    ${result}    Evaluate    ${a}+${b}
    [Return]    ${result}
    [Documentation]    Returns the sum of two numbers.    If A is 4 and B is 8, returns 12.

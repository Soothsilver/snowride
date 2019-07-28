*** Settings ***
Library           String
Library           Collections
Library           Collections
Library           Collections
Library           Collections
Library           Telnet
Library           Telnet
Library           Collections
Library           Telnet
Resource		rootfile.robot
Library           String

*** Variables ***
${ahoy}    Beta

*** Test Cases ***
Summation Test
    [Documentation]    bfgdxhbfdg❤️fsdfdsf
    Log    ahoj❤
    ${result}=    Return sum of numbers    2    4
    Should Be Equal As Integers    6    ${result}
    Log List    ahoj    beta
    ${a}    Create List    delta    gamma
    Log    This is CRLF.
    ${a}    Get Length    ${the list}
    Log    unlog
    Log    @{ahoy}
    Log Many    ${the list}
    Collections.Log List    ${the list}

*** Keywords ***
Return sum of numbers
    [Arguments]    ${a}    ${b}
    ${result}    Evaluate    ${a}+${b}
    [Return]    ${result}
    [Documentation]    Returns the sum of two numbers.    If A is 4 and B is 8, returns 12.


*** Settings ***

*** Variables ***
${MAX_TRIES}=    ${50}
${NUMBER_TO_PASS_ON}=    7

*** Test Cases ***
If (IF) Test
    ${random}=    Set Variable    5
    IF    ${random} == 5
    Log    IS EQUAL
    Pass Execution    "${random} == 5
    ELSE IF    ${random} > 5
    Log    IS GREATER
    Log To Console    Too high.
    ELSE
    Log    IS LESS
    Log To Console    Too low.
    END
    Log    Test Finished...

If (ELSE IF) Test
    ${random}=    Set Variable    6
    IF    ${random} == 5
    Log    IS EQUAL
    Pass Execution    "${random} == 5
    ELSE IF    ${random} > 5
    Log    IS GREATER
    Log To Console    Too high.
    ELSE
    Log    IS LESS
    Log To Console    Too low.
    END
    Log    Test Finished...

If (ELSE) Test
    ${random}=    Set Variable    4
    IF    ${random} == 5
    Log    IS EQUAL
    Pass Execution    "${random} == 5
    ELSE IF    ${random} > 5
    Log    IS GREATER
    Log To Console    Too high.
    ELSE
    Log    IS LESS
    Log To Console    Too low.
    END
    Log    Test Finished...

For (error in FOR statement) Test
    FOR    ${Idx}    IN RANGE    0    $5
    Log    ${Idx2}
    END
    Log    More logging...
    Log    END OF TEST

For (error inside FOR loop) Test
    FOR    ${Idx}    IN RANGE    0    5
    Log    ${Idx2}
    END
    Log    More logging...
    Log    END OF TEST

For (success) Test
    FOR    ${Idx}    IN RANGE    0    5
    Log    ${Idx}
    END
    Log    More logging...
    Log    END OF TEST

For-If Test
    FOR    ${i}    IN RANGE    ${MAX_TRIES}
        ${random}=    Evaluate    random.randint(0, 10)
        IF    ${random} == ${NUMBER_TO_PASS_ON}
        Pass Execution    "${random} == ${NUMBER_TO_PASS_ON}"
        ELSE IF    ${random} > ${NUMBER_TO_PASS_ON}
        Log To Console    Too high.
        ELSE
        Log To Console    Too low.
        END
    END

*** Keywords ***

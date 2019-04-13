*** Settings
Resource 	../rootfile.robot




*** Keywords ***
Raise Dead
    [Documentation]    This is documentation.
    Log    I am now raising you from the dead.
    Log    Pay X diamonds?
    Log    Aborted    WARN
    Raise Dead
    ${delta}    Return sum of numbers    2    4

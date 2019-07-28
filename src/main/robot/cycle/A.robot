*** Settings ***
Resource	B.robot
Library		Collections
Library		Collections
Library			Collections

*** Test Cases ***
afdgsg
    Run Keyword If    True    Log    Do Nothing
    Log    ${name}


*** Settings ***
Resource    WaterResources.robot

*** Test Cases ***
Sea test
    ${sea level}    Set Variable    10
    Log    The test begins.
    Run Keyword If    ${sea level} > 0    Fail    You must be in the sea first.
    Make waves
    Put the ship on the sea    big ship
    Log To Console
    sea


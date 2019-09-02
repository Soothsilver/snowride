*** Keywords ***
Root Ahoy
    Log    Ahoy    WARN
    Log    ${realm}

*** Settings ***
Library    Collections
Resource    infolder/ressurection.robot
Test Timeout    5 minutes
Test Setup    And Log List    ${yourname}

*** Variables ***
${yourname}    Soothsilver
${realm}    Seven Mounting Heavens of Celestia


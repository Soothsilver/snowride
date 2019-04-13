*** Keywords ***
Root Ahoy
    Log    Ahoy    WARN
    Log    Another Ahoy

*** Settings ***
Library    Collections
Library    String
Test Timeout    5 minutes
Test Setup    Log    Ahoy
Test Teardown    Log    End

*** Variables ***
${yourname}    Soothsilver
${realm}    Seven Mounting Heavens of Celestia

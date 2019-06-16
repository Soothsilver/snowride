*** Keywords ***
Root Ahoy
    Log    Ahoy    WARN
    Log    Another Ahoy

*** Settings ***
Library    Collections
Resource    infolder/ressurection.robot
Test Timeout    5 minutes
Test Setup    Log    Ahoy

*** Variables ***
${yourname}    Soothsilver
${realm}    Seven Mounting Heavens of Celestia


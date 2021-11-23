*** Settings ***
Resource          resourceFile01.robot

*** Test Cases ***
Call keyword Test
    Log    Calling keyword
    ${result}    Return sum of numbers    1    4
    Log    ${result}

Call keyword (wrong argument) Test
    Log    Calling keyword
    ${result}    Return sum of numbers    1    4s
    Log    ${result}
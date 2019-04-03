*** Test Cases ***
Summation Test
	[Documentation]		bfgdxhbfdg❤️fsdfdsf
	Log	ahoj❤
    ${result}=    Return sum of numbers    2    4
    Should Be Equal As Integers    6    ${result}
*** Keywords ***
Return sum of numbers
	[Arguments]	${a}	${b}
    ${result}    Evaluate    ${a}+${b}
    [Return]    ${result}
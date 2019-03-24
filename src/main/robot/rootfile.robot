*** Test Cases ***

Horror Case
    : FOR    ${i}     IN RANGE       1        10000
    \       Log     ahoj
    Kill This     5
    Kill This    really kill it
	Log Many	I	really		mean     	  	 it
*** Keywords ***
Kill This
	[Arguments]		${five}
	Log	${five}
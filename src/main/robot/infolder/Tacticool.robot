*** Settings ****
Documentation    Tacticool is cool, really!!! I say.
Library     String
Resource      ressurection.robot

*** Test Cases ***
Abcdefghijklmnopqrstuvwxyz
    [Documentation]    Documentation has *come*.
	[Tags]	Simplest    Norwegian
	Lol
    Log Many    arigatou    horro
	Log Many	a	b	c	d
        Comment    Nokonomo    "Ima Sugu"
    : FOR    ${A}    IN RANGE    1    10
	\    No Operation
	Lol    # this is a comment    and this obviously as well    and this     now I change it    and again
    Log To Console    ahoj
    Log To Console    ahoj
    Fail
    Lol    Cre
    ${anone}    ${hora}=    ${matakuzu}=    Create List    sono
    ${sore}    Convert To Uppercase    ${hora}
    Raise Dead

Another Test Case
	Log	with valid points!
    Sleep    5s
	Log    ahoj
    Lol

And yet another
	Log	Now save
	Superlog
    Lol

*** Keywords
Lol
    [Documentation]    ahoj
	Log 	This is actually a real keyword.
    Convert To Uppercase

Superlog
	Log	One
	Log   Two

Maybe likey?

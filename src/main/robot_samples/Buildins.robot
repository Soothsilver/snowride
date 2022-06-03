*** Settings ***

*** Variables ***
${MAX_TRIES}=    ${50}
${NUMBER_TO_PASS_ON}=    7

*** Test Cases ***
Convert To String
    Convert To String    1234    11

Create Dictionary
    &{dict} =	Create Dictionary	key=value	foo=bar			# key=value syntax
    Should Be True	${dict} == {'key': 'value', 'foo': 'bar'}
    &{dict2} =	Create Dictionary	key	value	foo	bar	# separate key and value
    Should Be Equal	${dict}	${dict2}
    &{dict} =	Create Dictionary	${1}=${2}	&{dict}	foo=new		# using variables
    Should Be True	${dict} == {1: 2, 'key': 'value', 'foo': 'new'}
    Should Be Equal	${dict.key}	value				# dot-access

Create List
    @{list} =	Create List	a	b	c
    ${scalar} =	Create List	a	b	c
    ${ints} =	Create List	${1}	${2}	${3}


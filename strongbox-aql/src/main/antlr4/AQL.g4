grammar AQL;

@header {
    package org.carlspring.strongbox.aql.grammar;
}

query
:
    queryExp
;

queryExp
:
    tokenExp
;

tokenExp
:
    tokenExpLeft ':' tokenExpRight
;

tokenExpRight
:
    
;

tokenExpLeft
:
    
;

WHITESPACE
:
    ' ' -> skip
;
grammar AQL;

@header {
    package org.carlspring.strongbox.aql.grammar;
}

query
:
    queryExp+
;

queryExp
:
    tokenPrefix? ROUND_BRACKET_LEFT vNesteedQueryExp = queryExp
    ROUND_BRACKET_RIGHT
    | vQueryExpLeft = queryExp logicalOp? vQueryExpRight = queryExp
    | tokenPrefix? tokenExp
;

tokenExp
:
    tokenKey COLON tokenValue
;

tokenPrefix
:
    PLUS
    | MINUS
;

tokenKey
:
    tokenKeyword
    | IDENTIFIER
;

tokenValue
:
    STRING
    | IDENTIFIER
;

tokenKeyword
:
    STORAGE
    | REPOSITORY
    | LAYOUT
    | VERSION
    | TAG
    | FROM
    | TO
    | AGE
    | ASC
    | DESC
;

logicalOp
:
    and
    | or
;

and
:
    AND
    | AMP
    | DOUBLE_AMP
;

or
:
    OR
    | PIPE
    | DOUBLE_PIPE
;

STORAGE
:
    'storage'
;

REPOSITORY
:
    'repository'
;

LAYOUT
:
    'layout'
;

VERSION
:
    'version'
;

TAG
:
    'tag'
;

FROM
:
    'from'
;

TO
:
    'to'
;

AGE
:
    'age'
;

ASC
:
    'asc'
;

DESC
:
    'desc'
;

AMP
:
    '&'
;

DOUBLE_AMP
:
    '&&'
;

AND
:
    'and'
    | 'AND'
;

PIPE
:
    '|'
;

DOUBLE_PIPE
:
    '||'
;

OR
:
    'or'
    | 'OR'
;

PLUS
:
    '+'
;

MINUS
:
    '-'
;

COLON
:
    ':'
;

ROUND_BRACKET_LEFT
:
    '('
;

ROUND_BRACKET_RIGHT
:
    ')'
;

IDENTIFIER
:
    [a-zA-Z0-9]+
;

STRING
:
    '\'' ~[\\'\r\n]* '\''
;

WHITESPACE
:
    [ ] -> skip
;
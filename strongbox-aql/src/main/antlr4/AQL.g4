grammar AQL;

@header {
    package org.carlspring.strongbox.aql.grammar;
}

query
:
    queryExp+ orderExp? pageExp? EOF
;

pageExp
:
    PAGE_SKIP COLON NUMBER
;

orderExp
:
    orderDirection COLON orderValue
;

orderValue
:
    STORAGE
    | REPOSITORY
    | LAYOUT
    | VERSION
    | TAG
    | AGE
    | IDENTIFIER
;

orderDirection
:
    ASC
    | DESC
;

queryExp
:
    tokenPrefix? ROUND_BRACKET_LEFT vNestedQueryExp = queryExp
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
    | NEGATION
    | PLUS NEGATION
;

tokenKey
:
    tokenKeyword
    | IDENTIFIER
;

tokenValue
:
    IDENTIFIER
    | VALUE
    | STRING
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

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

STORAGE
:
    S T O R A G E
;

REPOSITORY
:
    R E P O S I T O R Y
;

LAYOUT
:
    L A Y O U T
;

VERSION
:
    V E R S I O N
;

TAG
:
    T A G
;

FROM
:
    F R O M
;

TO
:
    T O
;

AGE
:
    A G E
;

ASC
:
    A S C
;

DESC
:
    D E S C
;

PAGE_SKIP
:
    S K I P
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
    A N D
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
    O R
;

PLUS
:
    '+'
;

NEGATION
:
    '!'
    | '~'
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

NUMBER
:
    [0-9]+
;

IDENTIFIER
:
    [a-zA-Z] [-_a-zA-Z0-9]+
;

VALUE
:
    [-_a-zA-Z0-9*.]+
;

STRING
:
    [\\"\\'] ~[\\"\\'\r\n]* [\\"\\']
;

WHITESPACE
:
    [ ] -> skip
;
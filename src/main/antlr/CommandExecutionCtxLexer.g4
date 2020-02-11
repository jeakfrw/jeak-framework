lexer grammar CommandExecutionCtxLexer;

fragment IDENTIFIER : [a-zA-Z0-9]+;
fragment DASH : '-' ;
fragment EQ : '=';
fragment SAFECODEPOINT
   : ~ ["\\\u0000-\u001F]
   ;
fragment SAFECODEPOINT_PARAM
   : ~ ["\\\u0000-\u001F -];
fragment ESC: '\\';

WHITESPACE: [ \t\n\r]+;

ARGUMENT_HEAD_EQ: DASH DASH? IDENTIFIER EQ;
ARGUMENT_OPTION : DASH DASH? IDENTIFIER;
PARAMETER: SAFECODEPOINT_PARAM+ | SAFECODEPOINT_PARAM+ ('-' SAFECODEPOINT_PARAM+)+;
QUOTED_PARAMETER: '"' (ESC '"' | SAFECODEPOINT)* '"';
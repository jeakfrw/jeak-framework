parser grammar CommandExecutionCtxParser;

options {
    tokenVocab = CommandExecutionCtxLexer;
}

commandExecution
    : parameter? (WHITESPACE parameter)*
    | argument? (WHITESPACE argument)*
    ;

parameter
    : PARAMETER
    | QUOTED_PARAMETER
    ;

argument
    : ARGUMENT_HEAD_EQ parameter
    | ARGUMENT_OPTION
    ;

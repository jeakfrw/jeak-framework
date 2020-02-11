package de.fearnixx.jeak.service.command;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;
import java.util.function.Consumer;

public class SyntaxErrorListener extends BaseErrorListener {

    private Consumer<String> errorMessageConsumer;

    public SyntaxErrorListener(Consumer<String> errorMessageConsumer) {
        this.errorMessageConsumer = errorMessageConsumer;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        errorMessageConsumer.accept(msg);
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        errorMessageConsumer.accept(String.format("Ambiguity error at index %o - %o", startIndex, stopIndex));
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        errorMessageConsumer.accept(String.format("Full context error at index %o - %o", startIndex, stopIndex));
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        errorMessageConsumer.accept(String.format("Context sensitivity error at index %o - %o", startIndex, stopIndex));
    }
}

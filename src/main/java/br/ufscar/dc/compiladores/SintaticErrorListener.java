package br.ufscar.dc.compiladores;

import java.util.BitSet;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class SintaticErrorListener implements ANTLRErrorListener {
    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
            BitSet ambigAlts, ATNConfigSet configs) {
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
            BitSet conflictingAlts, ATNConfigSet configs) {
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
            ATNConfigSet configs) {
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        Token t = (Token) offendingSymbol;

        if (t.getText().equals("<EOF>")) {
            var symbol = new StringBuffer(t.getText());
            symbol.delete(t.getText().length() - 1, t.getText().length());
            symbol.delete(0, 1);
            throw new ParseCancellationException(
                    "Linha " + line + ": erro sintatico proximo a " + symbol.toString() + "\n");
        } else {
            throw new ParseCancellationException("Linha " + line + ": erro sintatico proximo a " + t.getText() + "\n");
        }
    }
}

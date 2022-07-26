package br.ufscar.dc.compiladores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class LexerErrorListener extends BaseErrorListener {
    
    public static final LexerErrorListener INSTANCE = new LexerErrorListener();
    
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
            int line, int charPositionLine, String msg, RecognitionException e)
            throws ParseCancellationException{
        String[] tokens = msg.split("\'");
        
        if(tokens[1].charAt(0) == '{' && tokens[1].charAt(tokens[1].length()-1) != '}'){
            throw new ParseCancellationException("Linha " + line + ":" + " comentario nao fechado" + "\n");
        }
        if(tokens[1].charAt(0) == '"' && tokens[1].charAt(tokens[1].length()-1) != '"'){
            throw new ParseCancellationException("Linha " + line + ":" + " cadeia literal nao fechada" + "\n");
        }
        throw new ParseCancellationException("Linha " + line + ":" + " " + tokens[1] + " - simbolo nao identificado" + "\n");
    }
}

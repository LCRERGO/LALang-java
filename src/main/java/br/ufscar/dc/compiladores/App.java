package br.ufscar.dc.compiladores;

import java.io.FileWriter;
import java.io.IOException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class App {

    public static void main(String args[]) throws IOException, ParseCancellationException {
        if (args.length < 2) {
            System.err.printf("Usage: %s <input> <output> %n", args[0]);
            System.exit(1);
        }

        var cs = CharStreams.fromFileName(args[0]);
        var lex = new LALexer(cs);

        var myWriter = new FileWriter(args[1]);
        try {
            lex.removeErrorListeners();
            lex.addErrorListener(LexerErrorListener.INSTANCE);
            
            var tokens = new CommonTokenStream(lex);
            var parser = new LAParser(tokens);
            
            var mcel = new SintaticErrorListener();
            parser.addErrorListener(mcel);
            
            var arvore = parser.programa();
            var sem = new LASemantic();
            sem.visitPrograma(arvore);
            
            if(!LASemanticUtils.semanticErrors.isEmpty()){
                for(var s: LASemanticUtils.semanticErrors){
                    myWriter.write(s);
                }
                myWriter.write("Fim da compilacao\n");
            }
        } catch (ParseCancellationException e) {
            myWriter.write(e.getMessage());
            myWriter.write("Fim da compilacao\n");
        }
        
        myWriter.close();
    }

}

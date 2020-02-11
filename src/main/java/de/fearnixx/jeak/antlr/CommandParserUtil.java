package de.fearnixx.jeak.antlr;

import de.fearnixx.jeak.service.command.CommandCtxVisitor;
import de.fearnixx.jeak.service.command.CommandInfo;
import de.fearnixx.jeak.service.command.SyntaxErrorListener;
import de.mlessmann.confort.lang.RuntimeParseException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.slf4j.Logger;

public abstract class CommandParserUtil {

    private CommandParserUtil() {
    }

    public static CommandInfo parseCommandLine(String arguments, Logger logger) {
        CodePointCharStream charStream = CharStreams.fromString(arguments);
        var lexer = new CommandExecutionCtxLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new CommandExecutionCtxParser(tokenStream);


        // Use 2-stage parsing for expression performance
        // https://github.com/antlr/antlr4/blob/master/doc/faq/general.md#why-is-my-expression-parser-slow
        try {
            // STAGE 1
            var treeVisitor = new CommandCtxVisitor();
            var errorListener = new SyntaxErrorListener(treeVisitor.getInfo().getErrorMessages()::add);

            logger.debug("Trying to run STAGE 1 parsing. (SSL prediction)");
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            var grammarContext = parser.commandExecution();
            treeVisitor.visitCommandExecution(grammarContext);
            return treeVisitor.getInfo();
        } catch (Exception ex) {
            // STAGE 2
            var treeVisitor = new CommandCtxVisitor();
            var errorListener = new SyntaxErrorListener(treeVisitor.getInfo().getErrorMessages()::add);

            logger.debug("Trying to run STAGE 2 parsing. (LL prediction)", ex);
            tokenStream.seek(0);
            parser.reset();
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            try {
                var grammarContext = parser.commandExecution();
                treeVisitor.visitCommandExecution(grammarContext);
            } catch (RuntimeParseException e) {
                treeVisitor.getInfo().getErrorMessages().add(e.getMessage());
            }
            return treeVisitor.getInfo();
        }
    }
}

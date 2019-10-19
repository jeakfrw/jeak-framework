package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.antlr.CommandExecutionCtxLexer;
import de.fearnixx.jeak.antlr.CommandExecutionCtxParser;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.mlessmann.confort.lang.ParseVisitException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@FrameworkService(serviceInterface = ICommandService.class)
public class TypedCommandService extends CommandService {

    private static final String COMMAND_PREFIX = "!";
    private static final boolean DISABLE_LEGACY_WARN = Main.getProperty("jeak.commandSvc.disableLegacyWarn", false);
    private static final Integer THREAD_POOL_SIZE = Main.getProperty("jeak.commandSvc.poolSize", 2);
    private static final Integer AWAIT_TERMINATION_DELAY = Main.getProperty("jeak.commandSvc.terminateDelay", 5000);

    private static final Logger logger = LoggerFactory.getLogger(TypedCommandService.class);
    private final Map<String, ICommandSpec> commandSpecs = new ConcurrentHashMap<>();

    @Override
    protected int getThreadPoolSize() {
        return THREAD_POOL_SIZE;
    }

    @Override
    protected int getTerminateDelay() {
        return AWAIT_TERMINATION_DELAY;
    }

    @Override
    @Listener
    public void onTextMessage(IQueryEvent.INotification.ITextMessage event) {
        if (event.getMessage().startsWith(COMMAND_PREFIX)) {
            triggerCommand(event);
        }
    }

    private synchronized void triggerCommand(IQueryEvent.INotification.ITextMessage txtEvent) {
        String msg = txtEvent.getMessage();
        int firstSpace = msg.indexOf(' ');
        String command = msg.substring(COMMAND_PREFIX.length(), firstSpace);
        String arguments = msg.substring(firstSpace).trim();

        if (commandSpecs.containsKey(command)) {
            dispatchTyped(txtEvent, arguments, commandSpecs.get(command));

        } else if (getLegacyReceivers().containsKey(command)) {
            if (!DISABLE_LEGACY_WARN) {
                logger.warn("Command \"{}\" is only implemented using legacy command receivers. " +
                        "These will only continue to work in Jeak version 1.X", command);
            }
            super.onTextMessage(txtEvent);
        }
    }

    private void dispatchTyped(IQueryEvent.INotification.ITextMessage txtEvent, String arguments, ICommandSpec iCommandSpec) {

    }

    private CommandInfo parseCommandLine(String arguments) {
        CodePointCharStream charStream = CharStreams.fromString(arguments);
        var lexer = new CommandExecutionCtxLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new CommandExecutionCtxParser(tokenStream);

        // Use 2-stage parsing for expression performance
        // https://github.com/antlr/antlr4/blob/master/doc/faq/general.md#why-is-my-expression-parser-slow
        try {
            logger.debug("Trying to run STAGE 1 parsing. (SSL prediction)");
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            var grammarContext = parser.commandExecution();
            var treeVisitor = new CommandCtxVisitor();
            treeVisitor.visitCommandExecution(grammarContext);
            return treeVisitor.getInfo();
        } catch (Exception ex) {
            // STAGE 2
            logger.debug("Trying to run STAGE 2 parsing. (LL prediction)", ex);
            tokenStream.seek(0);
            parser.reset();
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);

            try {
                var grammarContext = parser.commandExecution();
                var treeVisitor = new CommandCtxVisitor();
                treeVisitor.visitCommandExecution(grammarContext);
                return treeVisitor.getInfo();
            } catch (ParseVisitException e) {
                throw new RuntimeException("NOT IMPLEMENTED!");
            }
        }
    }
}

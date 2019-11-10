package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.antlr.CommandExecutionCtxLexer;
import de.fearnixx.jeak.antlr.CommandExecutionCtxParser;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.command.matcher.*;
import de.fearnixx.jeak.service.command.matcher.meta.OneOfMatcher;
import de.fearnixx.jeak.service.command.matcher.meta.OptionalMatcher;
import de.fearnixx.jeak.service.command.reg.CommandRegistration;
import de.fearnixx.jeak.service.command.reg.MatchingContext;
import de.fearnixx.jeak.service.command.spec.ICommandArgumentSpec;
import de.fearnixx.jeak.service.command.spec.ICommandParamSpec;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.service.command.spec.IEvaluatedCriterion;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherRegistryService;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.locale.ILocaleContext;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import de.mlessmann.confort.lang.ParseVisitException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@FrameworkService(serviceInterface = ICommandService.class)
public class TypedCommandService extends CommandService {

    private static final String COMMAND_PREFIX = "!";
    private static final boolean DISABLE_LEGACY_WARN = Main.getProperty("jeak.commandSvc.disableLegacyWarn", false);
    private static final Integer THREAD_POOL_SIZE = Main.getProperty("jeak.commandSvc.poolSize", 2);
    private static final Integer AWAIT_TERMINATION_DELAY = Main.getProperty("jeak.commandSvc.terminateDelay", 5000);
    private static final String MSG_HAS_ERRORS = "execution.hasErrors";

    private static final Logger logger = LoggerFactory.getLogger(TypedCommandService.class);
    private final Map<String, CommandRegistration> typedCommands = new ConcurrentHashMap<>();

    @Inject
    private IMatcherRegistryService matcherRegistry;

    @Inject
    private IInjectionService injectionService;

    @Inject
    @LocaleUnit(value = "commandService", defaultResource = "localization/commandService.json")
    private ILocalizationUnit locales;

    @Override
    protected int getThreadPoolSize() {
        return THREAD_POOL_SIZE;
    }

    @Override
    protected int getTerminateDelay() {
        return AWAIT_TERMINATION_DELAY;
    }

    @Listener(order = Listener.Orders.SYSTEM)
    public void onPreInitialize(IBotStateEvent.IPreInitializeEvent event) {
        logger.debug("Registering default matchers.");
        registerMatcher(new BigDecimalParameterMatcher());
        registerMatcher(new BigIntegerParameterMatcher());
        registerMatcher(new BooleanParamMatcher());
        registerMatcher(new ChannelParameterMatcher());
        registerMatcher(new ClientParameterMatcher());
        registerMatcher(new DoubleParamMatcher());
        registerMatcher(new GroupParameterMatcher());
        registerMatcher(new IntegerParamMatcher());
        registerMatcher(new StringParamMatcher());
        registerMatcher(new SubjectParameterMatcher());
        registerMatcher(new UserParameterMatcher());

    }

    private <T> void registerMatcher(IParameterMatcher<T> matcher) {
        matcherRegistry.registerMatcher(injectionService.injectInto(matcher));
    }

    @Override
    @Listener
    public void onTextMessage(IQueryEvent.INotification.IClientTextMessage event) {
        if (event.getMessage().startsWith(COMMAND_PREFIX)) {
            triggerCommand(event);
        }
    }

    private synchronized void triggerCommand(IQueryEvent.INotification.IClientTextMessage txtEvent) {
        String msg = txtEvent.getMessage();
        String command;
        String arguments;
        if (msg.contains(" ")) {
            int firstSpace = msg.indexOf(' ');
            command = msg.substring(COMMAND_PREFIX.length(), firstSpace);
            arguments = msg.substring(firstSpace).trim();
        } else {
            command = msg.substring(COMMAND_PREFIX.length());
            arguments = msg.substring(COMMAND_PREFIX.length() + command.length()).trim();
        }

        if (typedCommands.containsKey(command)) {
            dispatchTyped(txtEvent, arguments, typedCommands.get(command));

        } else if (getLegacyReceivers().containsKey(command)) {
            if (!DISABLE_LEGACY_WARN) {
                logger.warn("Command \"{}\" is only implemented using legacy command receivers. " +
                        "These will only continue to work in Jeak version 1.X", command);
            }
            super.onTextMessage(txtEvent);
        }
    }

    private void dispatchTyped(IQueryEvent.INotification.IClientTextMessage txtEvent, String arguments, CommandRegistration registration) {
        ILocaleContext langCtx = locales.getContext(txtEvent.getSender().getCountryCode());
        CommandInfo info = parseCommandLine(arguments);
        if (!info.getErrorMessages().isEmpty()) {
            logger.info("Aborting command due to parsing errors.");
            info.getErrorMessages().add(0, langCtx.getMessage(MSG_HAS_ERRORS));
            sendErrorMessages(txtEvent, info);
            return;
        }

        ICommandSpec spec = registration.getCommandSpec();
        if (info.isArgumentized()) {
            if (spec.getArguments().isEmpty()) {
                info.getErrorMessages().add(langCtx.getMessage("command.notArgumented"));
            }
        } else if (info.isParameterized()) {
            if (spec.getParameters().isEmpty()) {
                info.getErrorMessages().add(langCtx.getMessage("command.notParameterized"));
            }
        }
        if (!info.getErrorMessages().isEmpty()) {
            logger.info("Aborting command due to precondition errors.");
            info.getErrorMessages().add(0, langCtx.getMessage(MSG_HAS_ERRORS));
            sendErrorMessages(txtEvent, info);
            return;
        }

        CommandExecutionContext commCtx = new CommandExecutionContext(txtEvent.getTarget(), info);
        registration.getMatchingContext()
                .stream()
                .map(ctx -> ctx.getMatcher().tryMatch(commCtx, ctx))
                .forEach(response -> {
                    if (response.getResponseType() == MatcherResponseType.ERROR) {
                        info.getErrorMessages().add(response.getFailureMessage());
                    }
                });
        if (!info.getErrorMessages().isEmpty()) {
            logger.info("Aborting command due to matching errors.");
            info.getErrorMessages().add(0, langCtx.getMessage(MSG_HAS_ERRORS));
            sendErrorMessages(txtEvent, info);
            return;
        }

        try {
            spec.getExecutor().execute(commCtx);
        } catch (CommandException e) {
            logger.debug("Command executor returned an exception.", e);
            info.getErrorMessages().add(0, langCtx.getMessage(MSG_HAS_ERRORS));
            sendErrorMessages(txtEvent, info);
        }
    }

    private void sendErrorMessages(IQueryEvent.INotification.IClientTextMessage txtEvent, CommandInfo info) {
        info.getErrorMessages()
                .forEach(msg -> txtEvent.getConnection().sendRequest(txtEvent.getSender().sendMessage(msg)));
    }

    protected CommandInfo parseCommandLine(String arguments) {
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
            } catch (ParseVisitException e) {
                treeVisitor.getInfo().getErrorMessages().add(e.getMessage());
            }
            return treeVisitor.getInfo();
        }
    }

    @SuppressWarnings("squid:S3864")
    @Override
    public void registerCommand(ICommandSpec spec) {
        List<ICommandParamSpec> parameters = spec.getParameters();
        List<ICommandArgumentSpec> arguments = spec.getArguments();
        if (!parameters.isEmpty() && !arguments.isEmpty()) {
            throw new IllegalArgumentException("A command may not have arguments AND parameters!");
        }

        final String command = spec.getCommand();
        List<MatchingContext> rootCtxs = new LinkedList<>();
        final AtomicInteger paramPositions = new AtomicInteger();
        parameters.forEach(par -> {
            makeParMatcherCtx(command, rootCtxs::add, paramPositions, par);
        });
        arguments.forEach(arg -> {
            makeArgMatcherCtx(command, rootCtxs::add, paramPositions, arg);
        });

        CommandRegistration registration = new CommandRegistration(spec, rootCtxs);
        typedCommands.put(command, registration);
        String aliasStr = spec.getAliases()
                .stream()
                .peek(alas -> typedCommands.put(alas, registration))
                .collect(Collectors.joining(","));
        logger.info("Registered command \"{}\" with aliases: [{}]", command, aliasStr);
    }

    private void makeArgMatcherCtx(String command, Consumer<MatchingContext> consumer, AtomicInteger paramPositions, ICommandArgumentSpec arg) {
        IParameterMatcher<?> matcher = translateSpec(arg, paramPositions);
        String argumentName = arg.getName();
        String argumentShorthand = arg.getShorthand();
        logger.trace("Adding argument \"{}/{}\" to command: \"{}\"", argumentName, argumentShorthand, command);
        MatchingContext matcherCtx = new MatchingContext(argumentName, argumentShorthand, matcher);
        consumer.accept(matcherCtx);

        if (arg.getSpecType() == IEvaluatedCriterion.SpecType.OPTIONAL) {
            makeArgMatcherCtx(command, matcherCtx::addChild, paramPositions, arg.getOptional());
        } else if (arg.getSpecType() == IEvaluatedCriterion.SpecType.FIRST_OF) {
            arg.getFirstOfP().forEach(subArg -> {
                makeArgMatcherCtx(command, matcherCtx::addChild, paramPositions, subArg);
            });
        }
    }

    private void makeParMatcherCtx(String command, Consumer<MatchingContext> consumer, AtomicInteger paramPositions, ICommandParamSpec par) {
        int pos = paramPositions.get();
        IParameterMatcher<?> matcher = translateSpec(par, paramPositions);
        String paramName = par.getName();
        logger.trace("Adding parameter \"{}\" at expected position [{}] to command: \"{}\"", paramName, pos, command);
        MatchingContext matchingCtx = new MatchingContext(paramName, matcher);
        consumer.accept(matchingCtx);

        if (par.getSpecType() == IEvaluatedCriterion.SpecType.OPTIONAL) {
            makeParMatcherCtx(command, matchingCtx::addChild, paramPositions, par.getOptional());
        } else if (par.getSpecType() == IEvaluatedCriterion.SpecType.FIRST_OF) {
            par.getFirstOfP().forEach(subPar -> {
                makeParMatcherCtx(command, matchingCtx::addChild, paramPositions, subPar);
            });
        }
    }

    private IParameterMatcher<?> translateSpec(IEvaluatedCriterion spec, AtomicInteger posTracker) {
        IEvaluatedCriterion.SpecType type = spec.getSpecType();
        Objects.requireNonNull(type, "Spec type may not be null! Param: " + spec.getName());
        switch (type) {
            case TYPE:
                Optional<IParameterMatcher<?>> optMatcher = matcherRegistry.findForType(spec.getValueType());
                posTracker.incrementAndGet();
                return optMatcher.orElseThrow(()
                        -> new IllegalArgumentException("No matcher found for type: " + spec.getValueType().getName()));

            case FIRST_OF:
                posTracker.incrementAndGet();
                return new OneOfMatcher();

            case OPTIONAL:
                return new OptionalMatcher();

            default:
                throw new IllegalArgumentException("Unknown spec type: " + type);
        }
    }
}

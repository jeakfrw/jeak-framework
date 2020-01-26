package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.antlr.CommandExecutionCtxLexer;
import de.fearnixx.jeak.antlr.CommandExecutionCtxParser;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.*;
import de.fearnixx.jeak.service.command.cmds.HelpCommand;
import de.fearnixx.jeak.service.command.matcher.*;
import de.fearnixx.jeak.service.command.matcher.meta.FirstOfMatcher;
import de.fearnixx.jeak.service.command.matcher.meta.HasPermissionMatcher;
import de.fearnixx.jeak.service.command.matcher.meta.OptionalMatcher;
import de.fearnixx.jeak.service.command.reg.CommandRegistration;
import de.fearnixx.jeak.service.command.reg.MatchingContext;
import de.fearnixx.jeak.service.command.spec.ICommandArgumentSpec;
import de.fearnixx.jeak.service.command.spec.ICommandParamSpec;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.service.command.spec.IEvaluatedCriterion;
import de.fearnixx.jeak.service.command.spec.matcher.ICriterionMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherRegistryService;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.locale.ILocaleContext;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.mlessmann.confort.lang.RuntimeParseException;
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
    private static final String MSG_HAS_EXCEPT = "execution.hasException";

    private static final Logger logger = LoggerFactory.getLogger(TypedCommandService.class);
    private final Map<String, CommandRegistration> typedCommands = new ConcurrentHashMap<>();
    private final Map<String, CommandRegistration> typedAliases = new ConcurrentHashMap<>();

    @Inject
    private IMatcherRegistryService matcherRegistry;

    @Inject
    private IInjectionService injectionService;

    @Inject
    @LocaleUnit(value = "commandService", defaultResource = "localization/commandService.json")
    private ILocalizationUnit locales;

    @Inject
    private IUserService userSvc;

    @Inject
    private IServer server;

    private FirstOfMatcher firstOfMatcher;
    private HasPermissionMatcher hasPermissionMatcher;
    private OptionalMatcher optionalMatcher;

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
        firstOfMatcher = injectionService.injectInto(new FirstOfMatcher());
        hasPermissionMatcher = injectionService.injectInto(new HasPermissionMatcher());
        optionalMatcher = injectionService.injectInto(new OptionalMatcher());

        registerCommand(HelpCommand.commandSpec(this, injectionService));
    }

    private <T> void registerMatcher(ICriterionMatcher<T> matcher) {
        matcherRegistry.registerMatcher(injectionService.injectInto(matcher));
    }

    @Override
    @Listener
    public void onTextMessage(IQueryEvent.INotification.IClientTextMessage event) {
        IDataHolder whoAmI = server.getConnection().getWhoAmI();
        int myId = whoAmI.getProperty("client_id")
                .map(Integer::parseInt)
                .orElse(0);
        if (event.getInvokerId() != myId && event.getMessage().startsWith(COMMAND_PREFIX)) {
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
        } else if (typedAliases.containsKey(command)) {
            dispatchTyped(txtEvent, arguments, typedAliases.get(command));

        } else if (getLegacyReceivers().containsKey(command)) {
            if (!DISABLE_LEGACY_WARN) {
                logger.warn("Command \"{}\" is only implemented using legacy command receivers. " +
                        "These will only continue to work in Jeak version 1.X", command);
            }
            super.onTextMessage(txtEvent);
        } else {
            IClient sender = txtEvent.getSender();
            String message = locales.getContext(sender.getCountryCode())
                    .getMessage("command.notFound", Map.of("command", command));
            txtEvent.getConnection().sendRequest(sender.sendMessage(message));
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
            info.getArguments()
                    .entrySet()
                    .forEach(argE -> {
                        if (registration.isClashed(argE.getKey())) {
                            String msg = locales.getContext(txtEvent.getSender().getCountryCode())
                                    .getMessage("command.argument.shorthandClashed", Map.of("shorthand", argE.getKey()));
                            info.getErrorMessages().add(msg);
                        } else {
                            String longName = registration.getLongName(argE.getKey());
                            if (longName != null) {
                                info.getArguments().put(longName, argE.getValue());
                            }
                        }
                    });

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

        CommandExecutionContext commCtx = new CommandExecutionContext(txtEvent.getTarget(), info, userSvc);
        commCtx.copyFrom(txtEvent);
        commCtx.setCaption(txtEvent.getCaption());
        commCtx.setClient(txtEvent.getTarget());
        commCtx.setConnection(txtEvent.getConnection());
        commCtx.setRawReference(txtEvent.getRawReference());
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
            info.getErrorMessages().add(e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Uncaught exception while executing command \"{}\" (executor: {})",
                    spec.getCommand(), spec.getExecutor().getClass().getName(), e);
            info.getErrorMessages().add(0, langCtx.getMessage(MSG_HAS_EXCEPT, Map.of(
                    "exceptionName", e.getClass().getSimpleName(),
                    "exceptionMessage", e.getMessage()
            )));
        } catch (Exception e) {
            logger.error("Uncaught checked exception while executing command \"{}\" (executor: {})",
                    spec.getCommand(), spec.getExecutor().getClass().getName(), e);
            info.getErrorMessages().add(0, langCtx.getMessage(MSG_HAS_EXCEPT, Map.of(
                    "exceptionName", e.getClass().getSimpleName(),
                    "exceptionMessage", e.getMessage()
            )));
        }
        sendErrorMessages(txtEvent, info);
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
            } catch (RuntimeParseException e) {
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
        final Map<String, ICommandArgumentSpec> shortHands = new HashMap<>();
        final List<String> clashedShorthands = new LinkedList<>();
        arguments.forEach(arg -> {
            makeArgMatcherCtx(command, rootCtxs::add, paramPositions, arg, shortHands, clashedShorthands);
        });
        rootCtxs.forEach(ctx -> ctx.setCommandSpec(spec));

        if (spec.getRequiredPermission().isPresent()) {
            MatchingContext permCtx = new MatchingContext("frw:permCheck", hasPermissionMatcher);
            permCtx.setCommandSpec(spec);
            rootCtxs.add(0, permCtx);
        }

        CommandRegistration registration = new CommandRegistration(spec, rootCtxs);
        shortHands.entrySet()
                .stream()
                .filter(e -> !e.getKey().equals(e.getValue().getName()))
                .forEach(e -> registration.addShorthand(e.getKey(), e.getValue().getName()));
        clashedShorthands.forEach(registration::addClashedShorthand);
        typedCommands.put(command, registration);
        String aliasStr = spec.getAliases()
                .stream()
                .peek(alas -> typedAliases.put(alas, registration))
                .collect(Collectors.joining(","));
        logger.info("Registered command \"{}\" with aliases: [{}]", command, aliasStr);
    }

    private void makeArgMatcherCtx(String command, Consumer<MatchingContext> consumer, AtomicInteger paramPositions,
                                   ICommandArgumentSpec arg,
                                   Map<String, ICommandArgumentSpec> shortHands, List<String> clashedShorthands) {
        ICriterionMatcher<?> matcher = translateSpec(arg, paramPositions);
        String argumentName = arg.getName();
        String argumentShorthand = arg.getShorthand();
        logger.trace("Adding argument \"{}/{}\" to command: \"{}\"", argumentName, argumentShorthand, command);
        MatchingContext matcherCtx = new MatchingContext(argumentName, argumentShorthand, matcher);
        consumer.accept(matcherCtx);

        if (IEvaluatedCriterion.SpecType.OPTIONAL.equals(arg.getSpecType())) {
            makeArgMatcherCtx(command, matcherCtx::addChild, paramPositions, arg.getOptional(), shortHands, clashedShorthands);
        } else if (IEvaluatedCriterion.SpecType.FIRST_OF.equals(arg.getSpecType())) {
            arg.getFirstOfP().forEach(subArg -> {
                makeArgMatcherCtx(command, matcherCtx::addChild, paramPositions, subArg, shortHands, clashedShorthands);
            });
        } else if (IEvaluatedCriterion.SpecType.TYPE.equals(arg.getSpecType())) {
            ICommandArgumentSpec replaced = shortHands.put(arg.getName(), arg);
            if (replaced != null) {
                String msg = String.format("Argument name \"%s\" of type \"%s clashes with another argument name of type: \"%s",
                        arg.getName(), arg.getValueType().getName(), replaced.getValueType().getName());
                throw new IllegalArgumentException(msg);
            }

            ICommandArgumentSpec replacedShorthand = shortHands.put(arg.getShorthand(), arg);
            if (replacedShorthand != null) {
                logger.warn("Argument shorthand \"{}\" of type \"{}\" clashes with another argument of type \"{}\". Users are forced to use the full name.",
                        arg.getShorthand(), arg.getValueType().getName(), replacedShorthand.getValueType().getName(),
                        new IllegalArgumentException());
                clashedShorthands.add(arg.getShorthand());
            }
        }
    }

    private void makeParMatcherCtx(String command, Consumer<MatchingContext> consumer, AtomicInteger paramPositions, ICommandParamSpec par) {
        int pos = paramPositions.get();
        ICriterionMatcher<?> matcher = translateSpec(par, paramPositions);
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

    private ICriterionMatcher<?> translateSpec(IEvaluatedCriterion spec, AtomicInteger posTracker) {
        IEvaluatedCriterion.SpecType type = spec.getSpecType();
        Objects.requireNonNull(type, "Spec type may not be null! Param: " + spec.getName());
        switch (type) {
            case TYPE:
                Optional<ICriterionMatcher<?>> optMatcher = matcherRegistry.findForType(spec.getValueType());
                posTracker.incrementAndGet();
                return optMatcher.orElseThrow(()
                        -> new IllegalArgumentException("No matcher found for type: " + spec.getValueType().getName()));

            case FIRST_OF:
                posTracker.incrementAndGet();
                return firstOfMatcher;

            case OPTIONAL:
                return optionalMatcher;

            default:
                throw new IllegalArgumentException("Unknown spec type: " + type);
        }
    }

    public List<CommandRegistration> getCommands() {
        return typedCommands.values()
                .stream()
                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<CommandRegistration> getCommand(String command) {
        return Optional.ofNullable(typedCommands.getOrDefault(command, null));
    }
}

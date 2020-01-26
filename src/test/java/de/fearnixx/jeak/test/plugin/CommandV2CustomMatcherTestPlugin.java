package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.service.command.spec.matcher.*;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.test.AbstractTestPlugin;

import static de.fearnixx.jeak.service.command.spec.Commands.commandSpec;
import static de.fearnixx.jeak.service.command.spec.Commands.paramSpec;

@JeakBotPlugin(id = "commandv2matchertest")
public class CommandV2CustomMatcherTestPlugin extends AbstractTestPlugin {
    private static final String CMD_PARAM_NAME = "custom";

    @Inject
    private ICommandService commandService;

    @Inject
    private IMatcherRegistryService matcherRegistry;

    @Inject
    private IServer server;

    public CommandV2CustomMatcherTestPlugin() {
        addTest("resultSuccess");
    }

    @Inject
    @LocaleUnit(value = "commandv2test", defaultResource = "localization/commandv2test.json")
    private ILocalizationUnit localeUnit;

    @Listener
    public void onPreInit(IBotStateEvent.IPreInitializeEvent event) {
        matcherRegistry.registerMatcher(new CustomMatcher(localeUnit));
    }

    @Listener
    public void onInit(IBotStateEvent.IInitializeEvent event) {
        ICommandSpec spec = commandSpec("v2-custom")
                .parameters(
                        paramSpec().optional(paramSpec("custom", CustomType.class))
                )
                .permission("test.subcommand", 4)
                .executor(this::executeCmd)
                .build();
        commandService.registerCommand(spec);
    }

    private void executeCmd(ICommandExecutionContext ctx) {
        this.success("resultSuccess");
        StringBuilder message = new StringBuilder("Test command. ");

        ctx.getOne(CMD_PARAM_NAME, CustomType.class)
                .ifPresent(u -> message.append("Custom param provided: ").append(u.toString()));

        server.getConnection().sendRequest(ctx.getSender().sendMessage(message.toString()));
    }

    private static class CustomType {
        private final String someName;

        public CustomType(String someName) {
            this.someName = someName;
        }

        @Override
        public String toString() {
            return "I am a custom with name: " + someName;
        }
    }

    private static class CustomMatcher extends AbstractTypeMatcher<CustomType> {

        private ILocalizationUnit localeUnit;

        public CustomMatcher(ILocalizationUnit localeUnit) {
            this.localeUnit = localeUnit;
        }

        @Override
        public Class<CustomType> getSupportedType() {
            return CustomType.class;
        }

        @Override
        protected ILocalizationUnit getLocaleUnit() {
            return localeUnit;
        }

        @Override
        protected IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), new CustomType(extracted));
            ctx.getParameterIndex().getAndIncrement();
            return BasicMatcherResponse.SUCCESS;
        }
    }
}
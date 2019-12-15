package de.fearnixx.jeak.service.command.cmds;

import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.TypedCommandService;
import de.fearnixx.jeak.service.command.reg.CommandRegistration;
import de.fearnixx.jeak.service.command.spec.*;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.util.*;

public class HelpCommand {

    public static ICommandSpec commandSpec(TypedCommandService commSvc, IInjectionService injectSvc) {
        return Commands.commandSpec("help")
                .alias("cmdsvc:help")
                .permission("framework.commands.help")
                .parameters(
                        Commands.paramSpec().optional(Commands.paramSpec("command", String.class))
                )
                .executor(injectSvc.injectInto(new HelpCommand(commSvc))::execute)
                .build();
    }

    private static final int CHAR_LIMIT_MSG = 1024;

    private TypedCommandService commandSvcImpl;

    @Inject
    @LocaleUnit("commandService")
    private ILocalizationUnit locale;

    protected HelpCommand(TypedCommandService commandSvcImpl) {
        this.commandSvcImpl = commandSvcImpl;
    }

    public void execute(ICommandExecutionContext commandCtx) {
        Optional<String> optCommand = commandCtx.getOne("command", String.class);
        if (optCommand.isEmpty()) {
            sendAllCommands(commandCtx);
        } else {
            sendSingleCommand(commandCtx, optCommand.get());
        }
    }

    private void sendAllCommands(ICommandExecutionContext commandCtx) {
        List<String> messages = new LinkedList<>();
        final StringBuilder builder = new StringBuilder();
        List<CommandRegistration> commands = new ArrayList<>(commandSvcImpl.getCommands());
        commands.sort(Comparator.comparing(c -> c.getCommandSpec().getCommand()));
        commands.forEach(cmd -> {
            String command = cmd.getCommandSpec().getCommand();
            if (builder.length() + command.length() + 1 > CHAR_LIMIT_MSG) {
                messages.add(builder.toString());
                // Reset builder
                builder.setLength(0);
            }
            builder.append("\n!").append(command);
        });

        if (builder.length() > 0) {
            messages.add(builder.toString());
        }

        messages.add(0,
                locale.getContext(commandCtx.getSender().getCountryCode())
                        .getMessage("command.help.allCommands")
        );
        messages.add(
                locale.getContext(commandCtx.getSender().getCountryCode())
                        .getMessage("command.help.typedNote")
        );
        messages.stream()
                .map(commandCtx.getSender()::sendMessage)
                .forEach(commandCtx.getConnection()::sendRequest);
    }

    private void sendSingleCommand(ICommandExecutionContext commandCtx, String command) {
        Optional<CommandRegistration> optCommand = commandSvcImpl.getCommand(command);
        if (optCommand.isEmpty()) {
            commandCtx.getConnection().sendRequest(
                    commandCtx.getSender().sendMessage(
                            locale.getContext(commandCtx.getSender().getCountryCode())
                                    .getMessage("command.help.notfound",
                                            Map.of("command", command))
                    )
            );
            return;
        }

        CommandRegistration commandReg = optCommand.get();
        final List<String> messages = new LinkedList<>();
        final StringBuilder message = new StringBuilder();
        message.append("!").append(command).append(" ");
        List<ICommandArgumentSpec> args = commandReg.getCommandSpec().getArguments();
        if (!args.isEmpty()) {
            serializeArguments(messages, message, args);
        }
        List<ICommandParamSpec> params = commandReg.getCommandSpec().getParameters();
        if (!params.isEmpty()) {
            serializeParameters(messages, message, params);
        }

        messages.add(0,
                locale.getContext(commandCtx.getSender().getCountryCode())
                        .getMessage("command.help.oneCommand", Map.of("command", command))
        );
        messages.add(
                locale.getContext(commandCtx.getSender().getCountryCode())
                        .getMessage("command.help.typedNote")
        );
        String aliasesStr =
                String.join(",", commandReg.getCommandSpec().getAliases());
        messages.add(
                locale.getContext(commandCtx.getSender().getCountryCode())
                        .getMessage("command.help.aliases", Map.of("aliases", aliasesStr))
        );
        messages.stream()
                .map(commandCtx.getSender()::sendMessage)
                .forEach(commandCtx.getConnection()::sendRequest);
    }

    private void serializeParameters(List<String> messages, StringBuilder message, List<ICommandParamSpec> params) {
        params.forEach(param -> {
            rotateIfExeeding(messages, message, message.length() + 1);
            message.append(" ");
            if (IEvaluatedCriterion.SpecType.TYPE.equals(param.getSpecType())) {
                addAndSerializeTypeParameter(messages, message, param);
            } else if (IEvaluatedCriterion.SpecType.OPTIONAL.equals(param.getSpecType())) {
                rotateIfExeeding(messages, message, message.length() + 1);
                message.append("[");
                addAndSerializeTypeParameter(messages, message, param.getOptional());
                rotateIfExeeding(messages, message, message.length() + 1);
                message.append("]");
            } else if (IEvaluatedCriterion.SpecType.FIRST_OF.equals(param.getSpecType())) {
                List<ICommandParamSpec> children = param.getFirstOfP();
                int size = children.size();
                for (int i = 0; i < size; i++) {
                    addAndSerializeTypeParameter(messages, message, children.get(i));
                    if (i < size - 1) {
                        rotateIfExeeding(messages, message);
                        message.append("|");
                    }
                }
            }
        });

        if (message.length() > 0) {
            messages.add(message.toString());
            message.setLength(0);
        }
    }

    private void addAndSerializeTypeParameter(List<String> messages, StringBuilder message, ICommandParamSpec param) {
        String parName = param.getName();
        rotateIfExeeding(messages, message, message.length() + parName.length());
        message.append(parName);
    }

    private void serializeArguments(List<String> messages, StringBuilder message, List<ICommandArgumentSpec> args) {
        args.forEach(arg -> {
            rotateIfExeeding(messages, message, message.length() + 1);
            message.append(" ");
            if (IEvaluatedCriterion.SpecType.TYPE.equals(arg.getSpecType())) {
                addAndSerializeTypeArgument(messages, message, arg);
            } else if (IEvaluatedCriterion.SpecType.OPTIONAL.equals(arg.getSpecType())) {
                rotateIfExeeding(messages, message);
                message.append("[");
                addAndSerializeTypeArgument(messages, message, arg.getOptional());
                message.append("]");
                rotateIfExeeding(messages, message);

            } else if (IEvaluatedCriterion.SpecType.FIRST_OF.equals(arg.getSpecType())) {
                List<ICommandArgumentSpec> children = arg.getFirstOfP();
                int size = children.size();
                for (int i = 0; i < size; i++) {
                    addAndSerializeTypeArgument(messages, message, children.get(i));
                    if (i < size - 1) {
                        rotateIfExeeding(messages, message);
                        message.append("|");
                    }
                }
            }
        });
        if (message.length() > 0) {
            messages.add(message.toString());
        }
    }

    private void rotateIfExeeding(List<String> messages, StringBuilder message) {
        rotateIfExeeding(messages, message, message.length());
    }

    private void rotateIfExeeding(List<String> messages, StringBuilder message, int length) {
        if (length >= CHAR_LIMIT_MSG) {
            messages.add(message.toString());
            message.setLength(0);
        }
    }

    private void addAndSerializeTypeArgument(List<String> messages, StringBuilder message, ICommandArgumentSpec arg) {
        String argName = arg.getName();
        String argShort = arg.getShorthand();
        rotateIfExeeding(messages, message, message.length() + 5 + argShort.length() + argName.length());
        message.append("--").append(argShort).append("=").append("<").append(argName).append(">");
    }
}

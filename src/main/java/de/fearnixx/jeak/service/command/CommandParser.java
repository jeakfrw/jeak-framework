package de.fearnixx.jeak.service.command;

import java.util.Optional;

/**
 * Created by MarkL4YG on 15-Feb-18
 */
public class CommandParser {

    public Optional<CommandContext> parseLine(String commandLine) throws CommandParserException {

        if (commandLine == null || !commandLine.startsWith(CommandService.COMMAND_PREFIX))
            return Optional.empty();

        CommandContext ctx = new CommandContext();
        // Arguments shouldn't be much more than this
        // Messages on TS3 are capped to 1024 characters anyways
        char[] buff = new char[256];
        int buffPos = 0;
        boolean commandParsed = false;
        boolean quoted = false;

        // Starting at 1 will not include the command symbol
        for (int i = 1; i < commandLine.length(); i++) {
            char ch = commandLine.charAt(i);
            switch (ch) {
                case '"':
                    quoted = !quoted;
                    break;
                case '\n':
                case ' ':
                    if (!quoted) {
                        String str = new String(buff, 0, buffPos);
                        if (commandParsed) {
                            ctx.getArguments().add(str);
                        } else {
                            ctx.setCommand(str);
                            commandParsed = true;
                        }
                        quoted = false;
                        buffPos = 0;
                        break;
                    }
                default:
                    if (buffPos == buff.length)
                        throw new CommandParserException("Argument buffer exceeded");
                    buff[buffPos++] = ch;
            }
        }

        return Optional.of(ctx);
    }
}

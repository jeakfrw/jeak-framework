package de.fearnixx.jeak.service.command.reg;

import de.fearnixx.jeak.service.command.spec.ICommandSpec;

import java.util.List;

public class CommandRegistration {

    private final ICommandSpec commandSpec;
    private final List<MatchingContext> matchingContext;

    public CommandRegistration(ICommandSpec commandSpec, List<MatchingContext> ctx) {
        this.commandSpec = commandSpec;
        this.matchingContext = ctx;
    }

    public ICommandSpec getCommandSpec() {
        return commandSpec;
    }

    public List<MatchingContext> getMatchingContext() {
        return matchingContext;
    }
}

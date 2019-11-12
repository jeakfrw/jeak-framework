package de.fearnixx.jeak.service.command.reg;

import de.fearnixx.jeak.service.command.spec.ICommandSpec;

import java.util.*;

public class CommandRegistration {

    private final ICommandSpec commandSpec;
    private final List<MatchingContext> matchingContext;
    private final Map<String, String> shortHands = new HashMap<>();
    private final Set<String> clashedShortHands = new HashSet<>();


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


    public void addShorthand(String shorthand, String longhand) {
        shortHands.put(shorthand, longhand);
    }

    public void addClashedShorthand(String shorthand) {
        clashedShortHands.add(shorthand);
    }

    public boolean isClashed(String shorthand) {
        return clashedShortHands.contains(shorthand);
    }

    public String getLongName(String shorthand) {
        return shortHands.getOrDefault(shorthand, null);
    }
}

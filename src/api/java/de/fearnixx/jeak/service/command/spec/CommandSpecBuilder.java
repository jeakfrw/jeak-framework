package de.fearnixx.jeak.service.command.spec;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class CommandSpecBuilder {

    private final List<String> aliases = new LinkedList<>();
    private final List<ICommandSpec> subcommands = new LinkedList<>();
    private final List<ICommandArgumentSpec> argumentSpecs = new LinkedList<>();
    private final List<ICommandParamSpec> paramSpecs = new LinkedList<>();
    private Consumer<Object> executor;
    private String name;

    CommandSpecBuilder() {
    }

    public CommandSpecBuilder alias(String... aliases) {
        if (aliases != null) {
            this.aliases.addAll(Arrays.asList(aliases));
        }
        return this;
    }

    public CommandSpecBuilder subcommand(ICommandSpec spec) {
        subcommands.add(spec);
        return this;
    }

    public CommandSpecBuilder arguments(ICommandArgumentSpec... arguments) {
        if (!paramSpecs.isEmpty()) {
            throw new IllegalStateException("Commands cannot use both arguments AND parameters at the same time.");
        }
        argumentSpecs.addAll(Arrays.asList(arguments));
        return this;
    }

    public CommandSpecBuilder parameters(ICommandParamSpec... parameters) {
        if (!argumentSpecs.isEmpty()) {
            throw new IllegalStateException("Commands cannot use both arguments AND parameters at the same time.");
        }
        paramSpecs.addAll(Arrays.asList(parameters));
        return this;
    }

    public CommandSpecBuilder executor(Consumer<Object> executor) {
        this.executor = executor;
        return this;
    }

    public ICommandSpec build() {
        return null;
    }

    public CommandSpecBuilder name(String name) {
        this.name = name;
        return this;
    }
}

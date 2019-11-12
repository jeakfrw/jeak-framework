package de.fearnixx.jeak.service.command.spec;

import java.util.*;

public class CommandSpecBuilder {

    private final List<String> aliases = new LinkedList<>();
    private final List<ICommandArgumentSpec> argumentSpecs = new LinkedList<>();
    private final List<ICommandParamSpec> paramSpecs = new LinkedList<>();
    private ICommandExecutor executor;
    private String name;
    private String requiredPerm;
    private int requiredPermValue = 1;

    CommandSpecBuilder() {
    }

    public CommandSpecBuilder alias(String... aliases) {
        if (aliases != null) {
            this.aliases.addAll(Arrays.asList(aliases));
        }
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

    public CommandSpecBuilder executor(ICommandExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandSpecBuilder permission(String permissionId) {
        requiredPerm = permissionId;
        return this;
    }

    public CommandSpecBuilder permission(String permissionId, int ofAtLeast) {
        requiredPerm = permissionId;
        requiredPermValue = ofAtLeast;
        return this;
    }

    public CommandSpecBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ICommandSpec build() {
        return new ICommandSpec() {
            private final String fName = name;
            private final List<String> fAliases = new ArrayList<>(aliases);
            private final List<ICommandParamSpec> fParams = new ArrayList<>(paramSpecs);
            private final List<ICommandArgumentSpec> fArguments = new ArrayList<>(argumentSpecs);
            private final ICommandExecutor fExecutor = executor;
            private final String fRequiredPermission = requiredPerm;
            private final int fRequiredPermValue = requiredPermValue;

            @Override
            public String getCommand() {
                return fName;
            }

            @Override
            public List<String> getAliases() {
                return fAliases;
            }

            @Override
            public List<ICommandParamSpec> getParameters() {
                return fParams;
            }

            @Override
            public List<ICommandArgumentSpec> getArguments() {
                return fArguments;
            }

            @Override
            public ICommandExecutor getExecutor() {
                return fExecutor;
            }

            @Override
            public Optional<String> getRequiredPermission() {
                return Optional.ofNullable(fRequiredPermission);
            }

            @Override
            public int getRequiredPermissionValue() {
                return fRequiredPermValue;
            }
        };
    }
}

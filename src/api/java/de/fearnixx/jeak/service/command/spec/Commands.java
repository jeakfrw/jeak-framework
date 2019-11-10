package de.fearnixx.jeak.service.command.spec;

public class Commands {

    private Commands() {
    }

    public static CommandSpecBuilder commandSpec() {
        return new CommandSpecBuilder();
    }

    public static CommandSpecBuilder commandSpec(String name, String... aliases) {
        return commandSpec().name(name).alias(aliases);
    }

    public static ArgumentSpecBuilder argumentSpec() {
        return new ArgumentSpecBuilder();
    }

    public static ArgumentSpecBuilder argumentSpec(String name, String shorthand) {
        return argumentSpec().name(name).shorthand(shorthand);
    }

    public static ICommandArgumentSpec argumentSpec(String name, String shorthand, Class<?> type) {
        return argumentSpec(name, shorthand).type(type).build();
    }

    public static ParamSpecBuilder paramSpec() {
        return new ParamSpecBuilder();
    }

    public static ParamSpecBuilder paramSpec(String paramName) {
        return paramSpec().name(paramName);
    }

    public static ICommandParamSpec paramSpec(String paramName, Class<?> type) {
        return paramSpec(paramName).type(type).build();
    }
}

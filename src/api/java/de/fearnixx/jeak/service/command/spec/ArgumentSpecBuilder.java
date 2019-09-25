package de.fearnixx.jeak.service.command.spec;

public class ArgumentSpecBuilder {

    private String name;
    private String shorthand;
    private Class<?> type;

    ArgumentSpecBuilder() {
    }

    public ArgumentSpecBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ArgumentSpecBuilder shorthand(String shorthand) {
        this.shorthand = shorthand;
        return this;
    }

    public ArgumentSpecBuilder type(Class<?> type) {
        this.type = type;
        return this;
    }

    public ICommandArgumentSpec build() {
        return null;
    }
}

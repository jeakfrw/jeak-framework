package de.fearnixx.jeak.service.command.spec;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ParamSpecBuilder {

    private ICommandParamSpec optionalSpec;
    private final List<ICommandParamSpec> firstOfSpecs = new LinkedList<>();
    private String name;
    private Class<?> valueType;

    ParamSpecBuilder() {
    }

    public ICommandParamSpec firstMatching(ICommandParamSpec... parameters) {
        firstOfSpecs.addAll(Arrays.asList(parameters));
        return this.build();
    }

    public ICommandParamSpec optional(ICommandParamSpec param) {
        optionalSpec = param;
        return this.build();
    }

    public ParamSpecBuilder name(String paramName) {
        name = paramName;
        return this;
    }

    public ParamSpecBuilder type(Class<?> type) {
        valueType = type;
        return this;
    }

    public ICommandParamSpec build() {
        return null;
    }
}

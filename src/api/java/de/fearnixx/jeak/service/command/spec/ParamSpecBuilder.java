package de.fearnixx.jeak.service.command.spec;

import java.util.ArrayList;
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
        return new ICommandParamSpec() {

            private final String fName = name;
            private final SpecType fSpecType = null;
            private final List<ICommandParamSpec> fFirstOfSpecs = new ArrayList<>(firstOfSpecs);
            private final ICommandParamSpec fOptionalParamSpec = optionalSpec;
            private final Class<?> fValueType = valueType;

            @Override
            public String getName() {
                return fName;
            }

            @Override
            public SpecType getSpecType() {
                return fSpecType;
            }

            @Override
            public List<ICommandParamSpec> getFirstOfP() {
                return fFirstOfSpecs;
            }

            @Override
            public ICommandParamSpec getOptional() {
                return fOptionalParamSpec;
            }

            @Override
            public Class<?> getValueType() {
                return fValueType;
            }
        };
    }
}

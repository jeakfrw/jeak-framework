package de.fearnixx.jeak.service.command.spec;

import java.util.List;

public class ArgumentSpecBuilder {

    private String name;
    private String shorthand;
    private Class<?> type;
    private IEvaluatedCriterion.SpecType specType;

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
        this.specType = IEvaluatedCriterion.SpecType.TYPE;
        return this;
    }

    public ICommandArgumentSpec build() {
        return new ICommandArgumentSpec() {
            private final String fName = name;
            private final String fShorthand = shorthand;
            private final Class<?> fValueType = type;
            private final SpecType fSpecType = specType;

            @Override
            public String getName() {
                return fName;
            }

            @Override
            public String getShorthand() {
                return fShorthand;
            }

            @Override
            public SpecType getSpecType() {
                return fSpecType;
            }

            @Override
            public List<ICommandArgumentSpec> getFirstOfP() {
                return null;
            }

            @Override
            public ICommandArgumentSpec getOptional() {
                return null;
            }

            @Override
            public Class<?> getValueType() {
                return fValueType;
            }
        };
    }
}

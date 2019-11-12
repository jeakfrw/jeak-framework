package de.fearnixx.jeak.service.command.spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentSpecBuilder {

    private String name;
    private String shorthand;
    private Class<?> type;
    private IEvaluatedCriterion.SpecType specType;
    private ICommandArgumentSpec optionalSpec;
    private final List<ICommandArgumentSpec> firstOfSpecs = new ArrayList<>();

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

    public ICommandArgumentSpec optional(ICommandArgumentSpec spec) {
        this.optionalSpec = spec;
        this.specType = IEvaluatedCriterion.SpecType.OPTIONAL;
        return build();
    }

    public ICommandArgumentSpec firstMatching(ICommandArgumentSpec... specs) {
        this.firstOfSpecs.addAll(Arrays.asList(specs));
        this.specType = IEvaluatedCriterion.SpecType.FIRST_OF;
        return build();
    }

    public ICommandArgumentSpec build() {
        return new ICommandArgumentSpec() {
            private final String fName = name;
            private final String fShorthand = shorthand;
            private final Class<?> fValueType = type;
            private final SpecType fSpecType = specType;
            private final ICommandArgumentSpec fOptionalSpec = optionalSpec;
            private final List<ICommandArgumentSpec> fFirstOfSpecs = new ArrayList<>(firstOfSpecs);

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
                return fFirstOfSpecs;
            }

            @Override
            public ICommandArgumentSpec getOptional() {
                return fOptionalSpec;
            }

            @Override
            public Class<?> getValueType() {
                return fValueType;
            }
        };
    }
}

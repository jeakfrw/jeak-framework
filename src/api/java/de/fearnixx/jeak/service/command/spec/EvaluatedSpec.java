package de.fearnixx.jeak.service.command.spec;

import java.util.List;

public interface EvaluatedSpec<T> {

    SpecType getSpecType();

    List<T> getFirstOfParams();

    T getOptionalParam();

    Class<?> getValueType();

    enum SpecType {
        FIRST_OF,
        OPTIONAL,
        TYPE,
    }
}

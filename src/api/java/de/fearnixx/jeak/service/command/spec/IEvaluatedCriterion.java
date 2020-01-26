package de.fearnixx.jeak.service.command.spec;

import java.util.List;

/**
 * Assistance interface for properties common in arguments and parameters (both are considered criteria).
 */
public interface IEvaluatedCriterion<T> {

    /**
     * The full name of the argument or parameter.
     */
    String getName();

    /**
     * The type of the criterion. Used to tell simple type arguments from more complex constructs such as
     * optional parameters, one-of parameters and has-permission parameters.
     */
    SpecType getSpecType();

    /**
     * When {@link #getSpecType()} is {@link ICommandParamSpec.SpecType#FIRST_OF}, these should be the parameters to be
     * evaluated. Keep in mind that meta-criteria do not make any sense in here!
     */
    List<T> getFirstOfP();

    /**
     * When {@link #getSpecType()} is {@link ICommandParamSpec.SpecType#OPTIONAL}, this should be the parameter to be
     * evaluated. Keep in mind that meta-criteria do not make any sense in here!
     */
    T getOptional();

    /**
     * When {@link #getSpecType()} is {@link ICommandParamSpec.SpecType#TYPE}, this should be the interface of the wanted
     * type to be matched.
     */
    Class<?> getValueType();

    /**
     * @see #getSpecType()
     */
    enum SpecType {
        FIRST_OF,
        OPTIONAL,
        TYPE,
    }
}

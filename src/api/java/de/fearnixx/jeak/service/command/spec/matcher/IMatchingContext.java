package de.fearnixx.jeak.service.command.spec.matcher;

import java.util.List;

public interface IMatchingContext {
    boolean isParameter();

    boolean isArgument();

    String getArgumentOrParamName();

    String getArgShorthand();

    List<IMatchingContext> getChildren();

    IParameterMatcher<?> getMatcher();
}

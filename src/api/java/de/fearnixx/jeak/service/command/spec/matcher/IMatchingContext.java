package de.fearnixx.jeak.service.command.spec.matcher;

import de.fearnixx.jeak.service.command.spec.ICommandSpec;

import java.util.List;

public interface IMatchingContext {
    boolean isParameter();

    boolean isArgument();

    String getArgumentOrParamName();

    String getArgShorthand();

    List<IMatchingContext> getChildren();

    IParameterMatcher<?> getMatcher();

    ICommandSpec getCommandSpec();
}

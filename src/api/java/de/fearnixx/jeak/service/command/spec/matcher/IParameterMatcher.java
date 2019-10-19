package de.fearnixx.jeak.service.command.spec.matcher;

import de.fearnixx.jeak.service.command.CommandExecutionContext;

public interface IParameterMatcher<T> {

    Class<T> getSupportedType();

    IMatcherResponse tryMatch(CommandExecutionContext ctx, int startParamPosition, String parameterName);
}

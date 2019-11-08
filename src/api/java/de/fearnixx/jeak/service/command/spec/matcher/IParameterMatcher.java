package de.fearnixx.jeak.service.command.spec.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;

public interface IParameterMatcher<T> {

    Class<T> getSupportedType();

    IMatcherResponse tryMatch(ICommandExecutionContext ctx, IMatchingContext matchingContext);
}

package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.CommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;

public class StringParamMatcher implements IParameterMatcher<String> {

    @Override
    public Class<String> getSupportedType() {
        return String.class;
    }

    @Override
    public IMatcherResponse tryMatch(CommandExecutionContext ctx, int startParamPosition, String parameterName) {
        ctx.getParameters().put(parameterName, ctx.getArguments().get(0));
        return MatcherResponse.SUCCESS;
    }
}

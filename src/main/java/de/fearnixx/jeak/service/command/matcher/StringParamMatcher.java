package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;

public class StringParamMatcher implements IParameterMatcher<String> {

    @Override
    public Class<String> getSupportedType() {
        return String.class;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, int startParamPosition, String parameterName) {
        ctx.putOrReplaceOne(parameterName, ctx.getArguments().get(0));
        return MatcherResponse.SUCCESS;
    }
}

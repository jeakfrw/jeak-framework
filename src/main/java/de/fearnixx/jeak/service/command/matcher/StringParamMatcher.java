package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;

public class StringParamMatcher extends AbstractTypeMatcher<String> {

    @Override
    public Class<String> getSupportedType() {
        return String.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), extracted);
        ctx.getParameterIndex().getAndIncrement();
        return MatcherResponse.SUCCESS;
    }
}

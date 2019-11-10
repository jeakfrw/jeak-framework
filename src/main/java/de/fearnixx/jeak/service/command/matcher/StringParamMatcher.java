package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;

public class StringParamMatcher extends AbstractFrameworkTypeMatcher<String> {

    @Override
    public Class<String> getSupportedType() {
        return String.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), extracted);
        ctx.getParameterIndex().getAndIncrement();
        return BasicMatcherResponse.SUCCESS;
    }
}

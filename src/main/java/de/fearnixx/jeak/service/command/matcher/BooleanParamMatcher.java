package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;

public class BooleanParamMatcher extends AbstractFrameworkTypeMatcher<Boolean> {

    @Override
    public Class<Boolean> getSupportedType() {
        return Boolean.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        if ("t".equals(extracted) || "1".equals(extracted) || "true".equals(extracted)
                || "y".equals(extracted) || "yes".equals(extracted)) {
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), Boolean.TRUE);
            ctx.getParameterIndex().incrementAndGet();
            return BasicMatcherResponse.SUCCESS;

        } else if ("f".equals(extracted) || "0".equals(extracted) || "false".equals(extracted)
                || "n".equals(extracted) || "no".equals(extracted)) {
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), Boolean.FALSE);
            ctx.getParameterIndex().incrementAndGet();
            return BasicMatcherResponse.SUCCESS;
        }

        return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
    }
}

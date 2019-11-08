package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerParamMatcher extends AbstractTypeMatcher<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(IntegerParamMatcher.class);

    @Override
    public Class<Integer> getSupportedType() {
        return Integer.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        Integer number = null;
        try {
            number = Integer.parseInt(extracted);
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), number);
            ctx.getParameterIndex().incrementAndGet();
            return MatcherResponse.SUCCESS;
        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
        }
    }
}

package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;

import java.math.BigInteger;

public class BigIntegerParameterMatcher extends AbstractFrameworkTypeMatcher<BigInteger> {

    @Override
    public Class<BigInteger> getSupportedType() {
        return BigInteger.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        try {
            BigInteger number = new BigInteger(extracted);
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), number);
            ctx.getParameterIndex().incrementAndGet();
            return BasicMatcherResponse.SUCCESS;

        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
        }
    }
}

package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;

import java.math.BigInteger;

public class BigIntegerParameterMatcher extends AbstractTypeMatcher<BigInteger> {

    @Override
    public Class<BigInteger> getSupportedType() {
        return BigInteger.class;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, int startParamPosition, String argName) {
        BigInteger number = null;
        try {
            number = new BigInteger(ctx.getArguments().get(startParamPosition));
            ctx.putOrReplaceOne(argName, number);
            return MatcherResponse.SUCCESS;

        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, startParamPosition);
        }
    }
}
package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.CommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class BigDecimalParameterMatcher extends AbstractTypeMatcher<BigDecimal> {

    private static final Logger logger = LoggerFactory.getLogger(BigDecimalParameterMatcher.class);

    @Override
    public Class<BigDecimal> getSupportedType() {
        return BigDecimal.class;
    }

    @Override
    public IMatcherResponse tryMatch(CommandExecutionContext ctx, int startParamPosition, String argName) {
        BigDecimal number = null;
        try {
            number = new BigDecimal(ctx.getArguments().get(startParamPosition));
            ctx.getParameters().put(argName, number);
            return MatcherResponse.SUCCESS;

        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, startParamPosition);
        }
    }
}

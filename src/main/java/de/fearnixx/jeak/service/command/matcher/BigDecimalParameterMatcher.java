package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class BigDecimalParameterMatcher extends AbstractFrameworkTypeMatcher<BigDecimal> {

    private static final Logger logger = LoggerFactory.getLogger(BigDecimalParameterMatcher.class);

    @Override
    public Class<BigDecimal> getSupportedType() {
        return BigDecimal.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        try {
            BigDecimal number = new BigDecimal(extracted);
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), number);
            ctx.getParameterIndex().incrementAndGet();
            return BasicMatcherResponse.SUCCESS;
        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
        }
    }
}

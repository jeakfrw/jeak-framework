package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleParamMatcher extends AbstractTypeMatcher<Double> {

    private static final Logger logger = LoggerFactory.getLogger(DoubleParamMatcher.class);

    @Override
    public Class<Double> getSupportedType() {
        return Double.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        Double number = null;
        try {
            number = Double.parseDouble(extracted);
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), number);
            ctx.getParameterIndex().incrementAndGet();
            return MatcherResponse.SUCCESS;

        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
        }
    }
}

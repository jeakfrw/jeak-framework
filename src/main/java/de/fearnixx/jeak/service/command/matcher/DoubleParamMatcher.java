package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleParamMatcher extends AbstractFrameworkTypeMatcher<Double> {

    private static final Logger logger = LoggerFactory.getLogger(DoubleParamMatcher.class);

    @Override
    public Class<Double> getSupportedType() {
        return Double.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        try {
            Double number = Double.parseDouble(extracted);
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), number);
            ctx.getParameterIndex().incrementAndGet();
            return BasicMatcherResponse.SUCCESS;

        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
        }
    }
}

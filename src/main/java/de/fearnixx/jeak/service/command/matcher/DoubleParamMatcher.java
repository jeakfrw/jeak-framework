package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.CommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoubleParamMatcher extends AbstractTypeMatcher<Double> {

    private static final Logger logger = LoggerFactory.getLogger(DoubleParamMatcher.class);

    @Override
    public Class<Double> getSupportedType() {
        return Double.class;
    }

    @Override
    public IMatcherResponse tryMatch(CommandExecutionContext ctx, int startParamPosition, String parameterName) {
        Double number = null;
        try {
            number = Double.parseDouble(ctx.getArguments().get(startParamPosition));
            ctx.getParameters().put(parameterName, number);
            return MatcherResponse.SUCCESS;

        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, startParamPosition);
        }
    }
}

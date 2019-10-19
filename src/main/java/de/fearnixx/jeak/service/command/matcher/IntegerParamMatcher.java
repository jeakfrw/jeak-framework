package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.CommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerParamMatcher extends AbstractTypeMatcher<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(IntegerParamMatcher.class);

    @Override
    public Class<Integer> getSupportedType() {
        return Integer.class;
    }

    @Override
    public IMatcherResponse tryMatch(CommandExecutionContext ctx, int startParamPosition, String parameterName) {
        Integer number = null;
        try {
            number = Integer.parseInt(ctx.getArguments().get(startParamPosition));
            ctx.getParameters().put(parameterName, number);
            return MatcherResponse.SUCCESS;
        } catch (NumberFormatException e) {
            return getIncompatibleTypeResponse(ctx, startParamPosition);
        }
    }
}

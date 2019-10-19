package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.CommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;

public class BooleanParamMatcher extends AbstractTypeMatcher<Boolean> {

    @Override
    public Class<Boolean> getSupportedType() {
        return Boolean.class;
    }

    @Override
    public IMatcherResponse tryMatch(CommandExecutionContext ctx, int startParamPosition, String argName) {
        String paramString = ctx.getArguments().get(startParamPosition);

        if ("t".equals(paramString) || "1".equals(paramString) || "true".equals(paramString)
                || "y".equals(paramString) || "yes".equals(paramString)) {
            ctx.getParameters().put(argName, Boolean.TRUE);
            return MatcherResponse.SUCCESS;

        } else if ("f".equals(paramString) || "0".equals(paramString) || "false".equals(paramString)
                || "n".equals(paramString) || "no".equals(paramString)) {
            ctx.getParameters().put(argName, Boolean.FALSE);
        }

        return getIncompatibleTypeResponse(ctx, startParamPosition);
    }
}

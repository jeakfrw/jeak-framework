package de.fearnixx.jeak.service.command.spec.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.ICommandInfo;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.util.Map;

public abstract class AbstractTypeMatcher<T> implements IParameterMatcher<T> {

    private static final String DEFAULT_INCOMPATIBLE_MSG = "matcher.type.incompatible";
    private static final String DEFAULT_MISSING_MSG = "matcher.type.missing";

    protected String getMissingMsgId() {
        return DEFAULT_MISSING_MSG;
    }

    protected String getIncompatibleTypeMsgId() {
        return DEFAULT_INCOMPATIBLE_MSG;
    }

    protected IMatcherResponse getIncompatibleTypeResponse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String input) {
        var messageParams = Map.of(
                "type", getSupportedType().getSimpleName(),
                "param", matchingContext.getArgumentOrParamName(),
                "input", input,
                "reportedBy", getClass().getSimpleName()
        );
        var incompatibleMessage =
                getLocaleUnit().getContext(ctx.getSender().getCountryCode())
                        .getMessage(getIncompatibleTypeMsgId(), messageParams);
        return new BasicMatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(), incompatibleMessage);
    }

    protected IMatcherResponse getMissingParameterResponse(ICommandExecutionContext ctx, String name) {
        var messageParams = Map.of(
                "type", getSupportedType().getSimpleName(),
                "param", name,
                "reportedBy", getClass().getSimpleName()
        );
        var message =
                getLocaleUnit().getContext(ctx.getSender().getCountryCode())
                        .getMessage(getMissingMsgId(), messageParams);
        return new BasicMatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(), message);
    }

    protected String extractArgument(ICommandInfo info, IMatchingContext matchingContext) {
        String str = info.getArguments().getOrDefault(matchingContext.getArgumentOrParamName(), null);
        if (str == null) {
            str = info.getArguments().getOrDefault(matchingContext.getArgShorthand(), null);
        }
        return str;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, IMatchingContext matchingContext) {
        ICommandInfo commInfo = ctx.getCommandInfo();
        String str = null;
        if (commInfo.isArgumentized()) {
            str = extractArgument(commInfo, matchingContext);
        } else {
            int idx = ctx.getParameterIndex().get();
            if (idx < commInfo.getParameters().size()) {
                str = commInfo.getParameters().get(idx);
            }
        }
        if (str == null) {
            return getMissingParameterResponse(ctx, matchingContext.getArgumentOrParamName());
        }

        return parse(ctx, matchingContext, str);
    }

    protected abstract ILocalizationUnit getLocaleUnit();

    protected abstract IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted);
}

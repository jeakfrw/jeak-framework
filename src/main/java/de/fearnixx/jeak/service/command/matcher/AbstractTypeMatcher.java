package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.ICommandInfo;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.util.Map;

public abstract class AbstractTypeMatcher<T> implements IParameterMatcher<T> {

    @Inject
    @LocaleUnit("commandService")
    private ILocalizationUnit localeUnit;

    protected ILocalizationUnit getLocaleUnit() {
        return localeUnit;
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
                        .getMessage("matcher.type.incompatible", messageParams);
        return new MatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(), incompatibleMessage);
    }

    protected IMatcherResponse getMissingParameterResponse(ICommandExecutionContext ctx, String name) {
        var messageParams = Map.of(
                "param", name
        );
        var message =
                getLocaleUnit().getContext(ctx.getSender().getCountryCode())
                        .getMessage("matcher.type.missing", messageParams);
        return new MatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(), message);
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

    protected abstract IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted);
}

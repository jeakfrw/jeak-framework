package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.util.Map;

public abstract class AbstractTypeMatcher<T> implements IParameterMatcher<T> {

    @Inject
    @LocaleUnit("commandSvc")
    private ILocalizationUnit localeUnit;

    protected ILocalizationUnit getLocaleUnit() {
        return localeUnit;
    }

    protected IMatcherResponse getIncompatibleTypeResponse(ICommandExecutionContext ctx, int paramIndex) {
        var incompatibleMessage =
                getLocaleUnit().getContext(ctx.getSender().getCountryCode())
                        .getMessage("matcher.type.incompatible",
                                Map.of("type", getClass().getSimpleName()));
        return new MatcherResponse(MatcherResponseType.ERROR, paramIndex, incompatibleMessage);
    }
}

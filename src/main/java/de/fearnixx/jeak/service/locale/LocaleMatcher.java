package de.fearnixx.jeak.service.locale;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.AbstractTypeMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;

import java.util.Locale;

public class LocaleMatcher extends AbstractTypeMatcher<Locale> {

    @Inject
    @LocaleUnit("commandService")
    private ILocalizationUnit localizationUnit;

    @Inject
    private ILocalizationService localeSvc;

    @Override
    protected ILocalizationUnit getLocaleUnit() {
        return localizationUnit;
    }

    @Override
    protected IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        if ("unset".equals(extracted)) {
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), new Locale.Builder().setLanguage("XX"));
            ctx.getParameterIndex().getAndIncrement();
            return BasicMatcherResponse.SUCCESS;
        } else {
            if (extracted.isBlank()) {
                return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
            }
            Locale locale = localeSvc.getLocaleForCountryId(extracted);
            ctx.putOrReplaceOne("locale", locale);
            ctx.getParameterIndex().getAndIncrement();
            return BasicMatcherResponse.SUCCESS;
        }
    }

    @Override
    public Class<Locale> getSupportedType() {
        return Locale.class;
    }
}

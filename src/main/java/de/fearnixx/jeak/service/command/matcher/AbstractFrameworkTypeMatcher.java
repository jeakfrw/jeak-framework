package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.spec.matcher.AbstractTypeMatcher;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

public abstract class AbstractFrameworkTypeMatcher<T> extends AbstractTypeMatcher<T> {

    @Inject
    @LocaleUnit("commandService")
    private ILocalizationUnit localeUnit;

    @Override
    protected ILocalizationUnit getLocaleUnit() {
        return localeUnit;
    }
}

package de.fearnixx.jeak.service.permission.framework.commands;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.*;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionService;

import java.util.Optional;

public class GroupSubjectMatcher extends AbstractTypeMatcher<IGroup> {

    @Inject
    @LocaleUnit("permissionService")
    private ILocalizationUnit localizationUnit;

    @Inject
    private IPermissionService permSvc;

    @Override
    protected ILocalizationUnit getLocaleUnit() {
        return localizationUnit;
    }

    @Override
    protected IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        Optional<IGroup> optGroup = permSvc.getFrameworkProvider().findGroupByName(extracted);
        if (optGroup.isEmpty()) {
            return new BasicMatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(),
                    "No group found for name: " + extracted);
        } else {
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), optGroup.get());
            ctx.getParameterIndex().getAndIncrement();
            return BasicMatcherResponse.SUCCESS;
        }
    }

    @Override
    public Class<IGroup> getSupportedType() {
        return null;
    }
}

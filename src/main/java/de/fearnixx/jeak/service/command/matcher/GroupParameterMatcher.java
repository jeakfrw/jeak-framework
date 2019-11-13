package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionService;

public class GroupParameterMatcher extends AbstractFrameworkTypeMatcher<IGroup> {

    @Inject
    private IPermissionService permService;

    @Override
    public Class<IGroup> getSupportedType() {
        return IGroup.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        var optGroup = permService.getFrameworkProvider().findGroupByName(extracted);

        if (optGroup.isEmpty()) {
            return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
        } else {
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), optGroup.get());
            ctx.getParameterIndex().incrementAndGet();
            return BasicMatcherResponse.SUCCESS;
        }
    }
}

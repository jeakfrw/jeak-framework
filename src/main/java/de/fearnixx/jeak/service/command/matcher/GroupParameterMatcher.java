package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.permission.base.IGroup;
import de.fearnixx.jeak.service.permission.base.IPermissionService;

public class GroupParameterMatcher extends AbstractTypeMatcher<IGroup> {

    @Inject
    private IPermissionService permService;

    @Override
    public Class<IGroup> getSupportedType() {
        return IGroup.class;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, int startParamPosition, String parameterName) {
        var optGroup = permService.getFrameworkProvider().findGroupByName(ctx.getArguments().get(startParamPosition));

        if (optGroup.isEmpty()) {
            return getIncompatibleTypeResponse(ctx, startParamPosition);
        } else {
            ctx.putOrReplaceOne(parameterName, optGroup.get());
            return MatcherResponse.SUCCESS;
        }
    }
}

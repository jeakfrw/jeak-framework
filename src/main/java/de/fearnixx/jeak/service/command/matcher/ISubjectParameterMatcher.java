package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.base.ISubject;

import java.util.Map;
import java.util.UUID;

public class ISubjectParameterMatcher extends AbstractTypeMatcher<ISubject> {

    @Inject
    private IPermissionService permissionService;

    @Override
    public Class<ISubject> getSupportedType() {
        return ISubject.class;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, int startParamPosition, String parameterName) {
        try {
            var optSubject = permissionService.getFrameworkProvider()
                    .getSubject(UUID.fromString(ctx.getArguments().get(startParamPosition)));
            if (optSubject.isEmpty()) {
                return getIncompatibleTypeResponse(ctx, startParamPosition);
            }
            ctx.putOrReplaceOne(parameterName, optSubject.get());
            ctx.putOrReplaceOne(parameterName + "Id", optSubject.get().getUniqueID());
            return MatcherResponse.SUCCESS;

        } catch (IllegalArgumentException e) {
            String invalidArgumentMessage =
                    getLocaleUnit().getContext(ctx.getSender().getCountryCode())
                            .getMessage("matcher.type.incompatible", Map.of("type", UUID.class.getSimpleName()));
            return new MatcherResponse(MatcherResponseType.ERROR, startParamPosition, invalidArgumentMessage);
        }
    }
}

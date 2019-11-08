package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.permission.base.IPermissionService;
import de.fearnixx.jeak.service.permission.base.ISubject;

import java.util.Map;
import java.util.UUID;

public class SubjectParameterMatcher extends AbstractTypeMatcher<ISubject> {

    @Inject
    private IPermissionService permissionService;

    @Override
    public Class<ISubject> getSupportedType() {
        return ISubject.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        try {
            var optSubject = permissionService.getFrameworkProvider()
                    .getSubject(UUID.fromString(extracted));
            if (optSubject.isEmpty()) {
                return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
            }
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName(), optSubject.get());
            ctx.putOrReplaceOne(matchingContext.getArgumentOrParamName() + "Id", optSubject.get().getUniqueID());
            ctx.getParameterIndex().getAndIncrement();
            return MatcherResponse.SUCCESS;

        } catch (IllegalArgumentException e) {
            String invalidArgumentMessage =
                    getLocaleUnit().getContext(ctx.getSender().getCountryCode())
                            .getMessage("matcher.type.incompatible", Map.of("type", UUID.class.getSimpleName()));
            return new MatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(), invalidArgumentMessage);
        }
    }
}

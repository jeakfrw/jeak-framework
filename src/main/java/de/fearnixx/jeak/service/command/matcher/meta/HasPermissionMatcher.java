package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Map;

public class HasPermissionMatcher implements IParameterMatcher<Void> {

    private final String requiredPermission;
    private final int requiredValue;

    @Inject
    @LocaleUnit("commandSvc")
    private ILocalizationUnit localeUnit;

    public HasPermissionMatcher(String requiredPermission, int requiredValue) {
        this.requiredPermission = requiredPermission;
        this.requiredValue = requiredValue;
    }

    @Override
    public Class<Void> getSupportedType() {
        return null;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, int startParamPosition, String name) {
        IClient sender = ctx.getSender();

        var optPerm = sender.getPermission(requiredPermission);
        if (optPerm.map(p -> p.getValue() >= requiredValue).orElse(false)) {
            var params = (Map) Map.of("permName", requiredPermission, "permValue", requiredValue);
            var missingPermMessage =
                    localeUnit.getContext(sender.getCountryCode())
                            .getMessage("matcher.perm.missing", params);
            return new MatcherResponse(MatcherResponseType.ERROR, -1, missingPermMessage);
        }

        return MatcherResponse.SUCCESS;
    }
}

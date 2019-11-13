package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.*;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import de.fearnixx.jeak.teamspeak.data.IClient;

import java.util.Map;

public class HasPermissionMatcher implements ICriterionMatcher<Void> {

    @Inject
    @LocaleUnit("commandService")
    private ILocalizationUnit localeUnit;

    @Override
    public Class<Void> getSupportedType() {
        return Void.class;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, IMatchingContext matchingContext) {
        IClient sender = ctx.getSender();
        String requiredPermission = matchingContext.getCommandSpec().getRequiredPermission().orElse(null);
        int requiredValue = matchingContext.getCommandSpec().getRequiredPermissionValue();
        if (requiredPermission == null) {
            return BasicMatcherResponse.SUCCESS;
        }

        var optPerm = sender.getPermission(requiredPermission);
        if (optPerm.map(permEntry -> permEntry.getValue() < requiredValue).orElse(true)) {
            var params = Map.of("permName", requiredPermission, "permValue", Integer.toString(requiredValue));
            var missingPermMessage =
                    localeUnit.getContext(sender.getCountryCode())
                            .getMessage("matcher.perm.missing", params);
            return new BasicMatcherResponse(MatcherResponseType.ERROR, -1, missingPermMessage);
        }

        return BasicMatcherResponse.SUCCESS;
    }
}

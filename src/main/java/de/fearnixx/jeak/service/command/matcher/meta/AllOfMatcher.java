package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AllOfMatcher implements IParameterMatcher<Void> {

    @Inject
    @LocaleUnit("commandService")
    private ILocalizationUnit localeUnit;

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, IMatchingContext matchingContext) {
        List<String> notices = new LinkedList<>();

        for (var child : matchingContext.getChildren()) {
            IParameterMatcher<?> param = child.getMatcher();

            var childResponse = param.tryMatch(ctx, child);
            if (childResponse.getResponseType().equals(MatcherResponseType.ERROR)) {
                return childResponse;
            } else if (childResponse.getResponseType().equals(MatcherResponseType.NOTICE)) {
                notices.add(childResponse.getNoticeMessage());
            }
        }

        if (notices.isEmpty()) {
            return MatcherResponse.SUCCESS;
        } else {
            Map<String, String> params = new HashMap<>();
            String fullNotice = localeUnit.getContext(ctx.getSender().getCountryCode())
                    .getMessage("matcher.allOf.notices", params);
            return new MatcherResponse(fullNotice);
        }
    }

    @Override
    public Class<Void> getSupportedType() {
        return Void.class;
    }
}

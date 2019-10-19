package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.CommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.util.*;

public class AllOfMatcher implements IParameterMatcher<Void> {

    private final Map<String, IParameterMatcher<?>> parameters = new LinkedHashMap<>();

    @Inject
    @LocaleUnit("commandSvc")
    private ILocalizationUnit localeUnit;

    @Override
    public IMatcherResponse tryMatch(CommandExecutionContext ctx, int paramStartIndex, String name) {
        List<String> notices = new LinkedList<>();
        int matcherIndex = 0;
        for (var paramEntry : parameters.entrySet()) {
            IParameterMatcher<?> param = paramEntry.getValue();

            var childResponse = param.tryMatch(ctx, paramStartIndex + matcherIndex, paramEntry.getKey());
            if (childResponse.getResponseType().equals(MatcherResponseType.ERROR)) {
                return childResponse;
            } else if (childResponse.getResponseType().equals(MatcherResponseType.NOTICE)) {
                notices.add(childResponse.getNoticeMessage());
            }

            matcherIndex++;
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
        return null;
    }
}

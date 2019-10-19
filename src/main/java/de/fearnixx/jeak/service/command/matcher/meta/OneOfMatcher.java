package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.CommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OneOfMatcher implements IParameterMatcher<Void> {

    private final Map<String, IParameterMatcher<?>> matchers = new LinkedHashMap<>();

    @Inject
    @LocaleUnit("commandService")
    private ILocalizationUnit localeUnit;

    @Override
    public Class<Void> getSupportedType() {
        return null;
    }

    @Override
    public IMatcherResponse tryMatch(CommandExecutionContext ctx, int startParamPosition, String name) {
        for (var matcherEntry : matchers.entrySet()) {
            var childResponse = matcherEntry.getValue().tryMatch(ctx, startParamPosition, matcherEntry.getKey());

            if (childResponse.getResponseType().equals(MatcherResponseType.SUCCESS)) {
                return childResponse;
            }
        }

        String typeList = matchers
                .values()
                .stream()
                .map(m -> m.getSupportedType().getName())
                .collect(Collectors.joining(", "));
        String unmatchedMessage = localeUnit.getContext(ctx.getSender().getCountryCode())
                .getMessage("matcher.oneOf.unmatched", Map.of("types", typeList));
        return new MatcherResponse(MatcherResponseType.ERROR, startParamPosition, unmatchedMessage);
    }
}

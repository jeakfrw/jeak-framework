package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.*;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;

import java.util.Map;
import java.util.stream.Collectors;

public class FirstOfMatcher implements ICriterionMatcher<Void> {

    @Inject
    @LocaleUnit("commandService")
    private ILocalizationUnit localeUnit;

    @Override
    public Class<Void> getSupportedType() {
        return Void.class;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, IMatchingContext matchingContext) {
        for (var child : matchingContext.getChildren()) {
            var childResponse = child.getMatcher().tryMatch(ctx, child);

            if (childResponse.getResponseType().equals(MatcherResponseType.SUCCESS)) {
                ctx.getParameterIndex().incrementAndGet();
                return childResponse;
            } else if (childResponse.getResponseType().equals(MatcherResponseType.NOTICE)) {
                ctx.getCommandInfo().getErrorMessages().add(childResponse.getFailureMessage());
            }
        }

        String typeList = matchingContext
                .getChildren()
                .stream()
                .map(m -> m.getMatcher().getSupportedType().getName())
                .collect(Collectors.joining(", "));
        String unmatchedMessage = localeUnit.getContext(ctx.getSender().getCountryCode())
                .getMessage("matcher.firstOf.unmatched",
                        Map.of(
                                "types", typeList
                        ));
        return new BasicMatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(), unmatchedMessage);
    }
}

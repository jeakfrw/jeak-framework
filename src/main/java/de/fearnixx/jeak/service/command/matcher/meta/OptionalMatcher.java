package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.*;

public class OptionalMatcher implements ICriterionMatcher<Void> {

    @Override
    public Class<Void> getSupportedType() {
        return Void.class;
    }

    @Override
    public IMatcherResponse tryMatch(ICommandExecutionContext ctx, IMatchingContext matchingContext) {
        IMatchingContext optional = matchingContext.getChildren().get(0);

        IMatcherResponse response = optional.getMatcher().tryMatch(ctx, optional);
        if (response.getResponseType() == MatcherResponseType.SUCCESS) {
            return response;
        } else {
            return BasicMatcherResponse.SUCCESS;
        }
    }
}

package de.fearnixx.jeak.service.command.matcher.meta;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;

public class OptionalMatcher implements IParameterMatcher<Void> {

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
            return MatcherResponse.SUCCESS;
        }
    }
}

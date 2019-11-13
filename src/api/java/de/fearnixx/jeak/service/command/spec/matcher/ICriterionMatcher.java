package de.fearnixx.jeak.service.command.spec.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;

/**
 * Interface for use with custom types that plugins want to resolve when their commands are invoked.
 * As matchers are register by instance and never cloned, this <em>has to be stateless</em>.
 * Matchers may only have fields corresponding to services or other classes whose instances never change and are at least
 * application scoped. <em>Having stateful matchers will break commands using them.</em>
 *
 * @param <T> the type that this parameter matcher can resolve.
 */
public interface ICriterionMatcher<T> {

    /**
     * The type this matcher will be able to resolve.
     */
    Class<T> getSupportedType();

    /**
     * A command using this matcher has been invoked and the matcher is asked to attempt to resolve associated criteria.
     *
     * @param ctx the context of the current command execution bound to the message sent by the user
     * @param matchingContext the context of the command itself bound to the commands registration.
     * @return a {@link IMatcherResponse} that reports whether or not this matcher was applied successfully.
     *      Take a look at {@link BasicMatcherResponse} for an implementation.
     */
    IMatcherResponse tryMatch(ICommandExecutionContext ctx, IMatchingContext matchingContext);
}

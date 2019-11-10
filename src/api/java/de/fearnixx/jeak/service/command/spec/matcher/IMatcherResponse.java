package de.fearnixx.jeak.service.command.spec.matcher;

import de.fearnixx.jeak.service.command.ICommandExecutionContext;

/**
 * After {{@link IParameterMatcher#tryMatch(ICommandExecutionContext, IMatchingContext)}}, this will tell the
 * command service implementation whether or not that matcher was applied successfully.
 * <p>Notes:
 * <ul>
 *     <li>
 *         If the result of a matcher contains a failure message
 *         <ol>
 *             <li>the command executor will not be fired.</li>
 *             <li>the matcher should not have consumed a parameter ({@link ICommandExecutionContext#getParameterIndex()} must be the same as when the invocation started.)</li>
 *         </ol>
 *     </li>
 * </ul>
 */
public interface IMatcherResponse {

    MatcherResponseType getResponseType();

    String getNoticeMessage();

    String getFailureMessage();

    int getFailedAtIndex();
}

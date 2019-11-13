package de.fearnixx.jeak.service.command.spec.matcher;

import de.fearnixx.jeak.service.command.spec.ICommandSpec;

import java.util.List;

/**
 * Context used to determine what matcher to use for a certain criterion.
 * When a matcher is called, this will always be the current context meaning that
 * {@link #getMatcher()} will be the matcher currently running (aka. {@code this})
 * When invoking nested matchers, the matching context passed must be the nested context where the nested matcher
 * was retrieved from.
 */
public interface IMatchingContext {

    /**
     * Whether or not the criterion at this position should be a parameter.
     */
    boolean isParameter();

    /**
     * Whether or not the criterion at this position should be an argument.
     */
    boolean isArgument();

    /**
     * Get the name of the criterion that is currently being evaluated. Used to store the resolved instance
     * in the {@link de.fearnixx.jeak.service.command.ICommandExecutionContext}.
     */
    String getArgumentOrParamName();

    /**
     * When {@link #isArgument()} returns true, this will be the shorthand specified for this argument.
     * Since argument shorthands are already translated to their full name by the command service, this
     * will not be of much use to matchers and has mainly reference purposes.
     */
    String getArgShorthand();

    /**
     * If the criterion this context was built from has children, their respective contexts will be nested here.
     * Obviously, this only applies to criteria that are not resolved to a specific type.
     */
    List<IMatchingContext> getChildren();

    /**
     * As explained in the class javadoc, this is the matcher to be used for evaluation of the current argument.
     * When this is invoked on a matching context that was passed to a matcher, this will return the matcher itself.
     * That means that the matching context passed to matchers must always match the depth of the matchers themselves.
     */
    ICriterionMatcher<?> getMatcher();

    /**
     * For reference-purposes, this will be the {@link ICommandSpec} used to register the command that is currently
     * being evaluated.
     */
    ICommandSpec getCommandSpec();
}

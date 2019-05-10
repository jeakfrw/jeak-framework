package de.fearnixx.jeak.service.locale;

import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * For performance reasons, we pre-split messages on loading.
 * This avoids having to compile (and/or evaluate) regular expressions repeatedly during runtime.
 * </p>
 * <p>
 * The idea: evaluate contained parameters during initialization, then split the message at found parameters.
 * When the message should be used, re-assemble the messages with the given parameters in place.
 * StringBuilder operations are VERY common in the framework, therefore the involved code is likely to be optimized.
 * </p>
 */
public class MessageRep {

    private static final Logger logger = LoggerFactory.getLogger(MessageRep.class);
    private static final Pattern PARAM_PATTERN = Pattern.compile("%\\[([a-zA-Z.0-9\\-_]+)]");

    private final String rawTemplate;
    private final List<String> splitMessageParts = new LinkedList<>();
    private final List<String> requiredParams = new LinkedList<>();

    public MessageRep(String rawTemplate) {
        this.rawTemplate = rawTemplate;
        explodeTemplate();
    }

    private void explodeTemplate() {
        Matcher paramMatcher = PARAM_PATTERN.matcher(rawTemplate);

        int lastStart = 0;
        int lastEnd = 0;
        while (paramMatcher.find()) {
            final String parameterName = paramMatcher.group(1);
            requiredParams.add(parameterName);

            if (logger.isDebugEnabled()) {
                logger.debug("Found parameter: {}", parameterName);
            }

            final int start = paramMatcher.start();
            final String left = rawTemplate.substring(lastEnd, start);
            splitMessageParts.add(left);
            lastStart = start;
            lastEnd = paramMatcher.end();
        }

        // If there's text to the right of the last match, we missed it.
        if (rawTemplate.length() > lastEnd) {
            final String right = rawTemplate.substring(lastEnd);
            splitMessageParts.add(right);
        }
    }

    public String getRawTemplate() {
        return rawTemplate;
    }

    public String getWithParams(Map<String, String> params) throws MissingParameterException {
        StringBuilder builder = new StringBuilder();

        // As we split at each parameter above,
        // parts.size() will be >= requiredParams.size()!
        // Therefore, we can iterate simultaneously.

        Iterator<String> partsIt = splitMessageParts.iterator();
        Iterator<String> requiredIt = requiredParams.iterator();

        // We want both loops to look the same!
        //noinspection WhileLoopReplaceableByForEach
        while (requiredIt.hasNext()) {
            final String leftSide = partsIt.next();
            final String paramName = requiredIt.next();

            if (params.containsKey(paramName)) {
                builder.append(leftSide);
                builder.append(params.get(paramName));
            } else {
                throw new MissingParameterException(paramName);
            }
        }

        // Again, don't miss the right side at the end.
        if (partsIt.hasNext()) {
            builder.append(partsIt.next());
        }

        // Fail-fast approach. If this happens ever, for any reason, we want the administrator to know it.
        if (partsIt.hasNext()) {
            throw new ConsistencyViolationException("Parts iterator is out of sync with params iterator! THIS SHOULD NOT HAPPEN!");
        }

        return builder.toString();
    }

    /* TEST ACCESSORS */
    public List<String> getSplitMessageParts() {
        return Collections.unmodifiableList(splitMessageParts);
    }

    public List<String> getRequiredParams() {
        return Collections.unmodifiableList(requiredParams);
    }
}

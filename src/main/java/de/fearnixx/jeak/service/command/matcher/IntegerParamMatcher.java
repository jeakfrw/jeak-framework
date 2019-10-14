package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class IntegerParamMatcher implements IParameterMatcher<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(IntegerParamMatcher.class);

    @Override
    public Class<Integer> getSupportedType() {
        return Integer.class;
    }

    @Override
    public Optional<Integer> tryMatch(String paramString) {
        Integer number = null;
        try {
            number = Integer.parseInt(paramString);
        } catch (NumberFormatException e) {
            logger.info("Failed to extract number parameter from given value: {}", paramString);
        }
        return Optional.ofNullable(number);
    }
}

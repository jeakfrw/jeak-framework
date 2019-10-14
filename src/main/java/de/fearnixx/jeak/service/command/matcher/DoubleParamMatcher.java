package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DoubleParamMatcher implements IParameterMatcher<Double> {

    private static final Logger logger = LoggerFactory.getLogger(DoubleParamMatcher.class);

    @Override
    public Class<Double> getSupportedType() {
        return Double.class;
    }

    @Override
    public Optional<Double> tryMatch(String paramString) {
        Double number = null;
        try {
            number = Double.parseDouble(paramString);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse double parameter: {}", e);
        }
        return Optional.ofNullable(number);
    }
}

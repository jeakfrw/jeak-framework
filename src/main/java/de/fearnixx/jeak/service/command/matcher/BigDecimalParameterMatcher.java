package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class BigDecimalParameterMatcher implements IParameterMatcher<BigDecimal> {

    private static final Logger logger = LoggerFactory.getLogger(BigDecimalParameterMatcher.class);

    @Override
    public Class<BigDecimal> getSupportedType() {
        return BigDecimal.class;
    }

    @Override
    public Optional<BigDecimal> tryMatch(String paramString) {
        BigDecimal number = null;
        try {
            number = new BigDecimal(paramString);
        } catch (NumberFormatException e) {
            logger.info("Failed to parse input number: {}", paramString);
        }
        return Optional.ofNullable(number);
    }
}

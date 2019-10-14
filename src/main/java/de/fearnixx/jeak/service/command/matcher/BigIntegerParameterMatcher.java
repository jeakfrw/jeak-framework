package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Optional;

public class BigIntegerParameterMatcher implements IParameterMatcher<BigInteger> {

    private static final Logger logger = LoggerFactory.getLogger(BigIntegerParameterMatcher.class);

    @Override
    public Class<BigInteger> getSupportedType() {
        return BigInteger.class;
    }

    @Override
    public Optional<BigInteger> tryMatch(String paramString) {
        BigInteger number = null;
        try {
            number = new BigInteger(paramString);
        } catch (NumberFormatException e) {
            logger.info("Could not parse number parameter: {}", paramString);
        }
        return Optional.ofNullable(number);
    }
}

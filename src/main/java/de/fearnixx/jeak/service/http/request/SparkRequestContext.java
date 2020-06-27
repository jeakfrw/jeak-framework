package de.fearnixx.jeak.service.http.request;

import spark.Request;

import java.util.Objects;
import java.util.Optional;

public class SparkRequestContext implements IRequestContext {

    private final Request sparkRequest;

    public SparkRequestContext(Request sparkRequest) {
        this.sparkRequest = sparkRequest;
    }

    @Override
    public <T> Optional<T> optAttribute(String name, Class<T> hint) {
        Objects.requireNonNull(name, "Attribute name may not be null!");
        Objects.requireNonNull(hint, "Attribute type hint may not be null!");
        var sparkValue = sparkRequest.attribute(name);

        if (sparkValue == null || !hint.isAssignableFrom(sparkValue.getClass())) {
            return Optional.empty();
        } else {
            return Optional.of(hint.cast(sparkValue));
        }
    }
}

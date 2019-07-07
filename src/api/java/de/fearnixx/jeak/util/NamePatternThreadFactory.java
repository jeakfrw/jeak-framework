package de.fearnixx.jeak.util;

import java.util.concurrent.atomic.AtomicInteger;

public class NamePatternThreadFactory extends NamingThreadFactory {

    private final AtomicInteger threadCounter = new AtomicInteger(0);
    private final String namePattern;

    public NamePatternThreadFactory(String namePattern) {
        super();
        this.namePattern = namePattern;
        this.setNameSupplier(this::supplyName);
    }

    private String supplyName() {
        return String.format(namePattern, threadCounter.getAndDecrement());
    }
}

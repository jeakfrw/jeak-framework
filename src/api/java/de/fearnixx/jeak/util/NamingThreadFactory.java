package de.fearnixx.jeak.util;

import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

public class NamingThreadFactory implements ThreadFactory {

    private Supplier<String> nameSupplier;

    public NamingThreadFactory(Supplier<String> nameSupplier) {
        this.nameSupplier = nameSupplier;
    }

    public NamingThreadFactory() {
    }

    protected void setNameSupplier(Supplier<String> nameSupplier) {
        this.nameSupplier = nameSupplier;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(nameSupplier.get());
        return thread;
    }
}

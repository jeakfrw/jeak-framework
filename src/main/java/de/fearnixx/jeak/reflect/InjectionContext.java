package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.service.IServiceManager;

import java.util.Objects;

public class InjectionContext {

    private final IServiceManager serviceManager;
    private final String contextId;
    private ClassLoader classLoader;

    public InjectionContext(IServiceManager serviceManager, String contextId, ClassLoader classLoader) {
        this.serviceManager = serviceManager;
        this.contextId = contextId;
        this.classLoader = classLoader;
    }

    public IServiceManager getServiceManager() {
        return serviceManager;
    }


    public String getContextId() {
        return contextId;
    }

    public InjectionContext getChild(String contextId) {
        return new InjectionContext(serviceManager, contextId, classLoader);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "ClassLoader may not be null!");
        this.classLoader = classLoader;
    }
}

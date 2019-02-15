package de.fearnixx.jeak.reflect;

import de.fearnixx.jeak.service.IServiceManager;

public class InjectionContext {

    private final IServiceManager serviceManager;
    private final String contextId;

    public InjectionContext(IServiceManager serviceManager, String contextId) {
        this.serviceManager = serviceManager;
        this.contextId = contextId;
    }

    public IServiceManager getServiceManager() {
        return serviceManager;
    }


    public String getContextId() {
        return contextId;
    }

    public InjectionContext getChild(String contextId) {
        return new InjectionContext(serviceManager, contextId);
    }
}

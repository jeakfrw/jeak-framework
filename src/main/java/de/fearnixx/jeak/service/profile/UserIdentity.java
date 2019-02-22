package de.fearnixx.jeak.service.profile;

import de.fearnixx.jeak.profile.IUserIdentity;

public class UserIdentity implements IUserIdentity {

    private final String serviceId;
    private final String identity;

    public UserIdentity(String serviceId, String identity) {
        this.serviceId = serviceId;
        this.identity = identity;
    }

    @Override
    public String serviceId() {
        return serviceId;
    }

    @Override
    public String identity() {
        return identity;
    }
}

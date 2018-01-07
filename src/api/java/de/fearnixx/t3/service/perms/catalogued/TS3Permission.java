package de.fearnixx.t3.service.perms.catalogued;

import de.fearnixx.t3.service.perms.permission.IPermission;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public abstract class TS3Permission implements IPermission {

    private final String qualifiedID = getProviderID() + ':' + getInternalSID();

    @Override
    public String getProviderID() {
        return "ts3";
    }

    @Override
    public Integer getDefaultValue() {
        return 0;
    }

    @Override
    public String getQualifiedID() {
        return qualifiedID;
    }

    @Override
    public int hashCode() {
        return qualifiedID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TS3Permission
               && qualifiedID.equals(((TS3Permission) obj).getQualifiedID());
    }
}

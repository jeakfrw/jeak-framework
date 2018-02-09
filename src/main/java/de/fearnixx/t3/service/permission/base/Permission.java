package de.fearnixx.t3.service.permission.base;

import de.fearnixx.t3.teamspeak.data.DataHolder;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public class Permission extends DataHolder implements IPermission {

    private String systemID;
    private String permSID;

    public Permission(String systemID, String permSID) {
        this.systemID = systemID;
        this.permSID = permSID;
    }

    @Override
    public Integer getValue() {
        return Integer.parseInt(getValues().get("value"));
    }

    @Override
    public String getSystemID() {
        return systemID;
    }

    @Override
    public String getSID() {
        return permSID;
    }

    @Override
    public String getFullyQualifiedID() {
        return getSystemID() + ':' + getSID();
    }
}

package de.fearnixx.jeak.service.permission.base;

import de.fearnixx.jeak.teamspeak.data.BasicDataHolder;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public class Permission extends BasicDataHolder implements IPermission {

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

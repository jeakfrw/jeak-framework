package de.fearnixx.t3.service.permission.base;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public interface IPermissionProvider {

    IPermission getPermission(String permSID, String clientUID);
}

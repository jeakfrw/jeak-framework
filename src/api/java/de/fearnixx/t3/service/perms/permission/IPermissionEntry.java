package de.fearnixx.t3.service.perms.permission;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public interface IPermissionEntry {

    IPermission getPermission();

    Integer getValue();

    Boolean getNegated();

    Boolean getSkipFlag();

    Integer getSystemPriority();
}

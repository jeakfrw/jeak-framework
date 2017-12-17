package de.fearnixx.t3.service.perms.permission;

import de.fearnixx.t3.ts3.keys.PropertyKeys;
import de.fearnixx.t3.ts3.query.IQueryMessageObject;

/**
 * Created by MarkL4YG on 26-Nov-17
 */
public class TS3PermissionEntry implements IPermissionEntry {

    private IPermission parent;
    private IQueryMessageObject object;

    public TS3PermissionEntry(IPermission parent, IQueryMessageObject object) {
        this.parent = parent;
        this.object = object;
    }

    @Override
    public IPermission getPermission() {
        return parent;
    }

    @Override
    public Integer getValue() {
        return object.getProperty(PropertyKeys.Permission.VALUE)
                     .map(Integer::valueOf)
                     .orElse(parent.getDefaultValue());
    }

    @Override
    public Boolean getNegated() {
        return "1".equals(object.getProperty(PropertyKeys.Permission.FLAG_NEGATED)
                                .orElse("0"));
    }

    @Override
    public Boolean getSkipFlag() {
        return "1".equals(object.getProperty(PropertyKeys.Permission.FLAG_SKIP)
                                .orElse("0"));
    }
}

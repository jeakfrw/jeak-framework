package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.service.permission.base.Permission;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public class TS3Permission extends Permission implements ITS3Permission {

    private PriorityType type;
    private String sid;

    public TS3Permission(PriorityType type, String sid) {
        super("teamspeak", sid);
        this.type = type;
        this.sid = sid;
    }

    @Override
    public PriorityType getPriorityType() {
        return type;
    }

    @Override
    public Integer getValue() {
        return Integer.parseInt(getValues().get("permvalue"));
    }

    @Override
    public Boolean getNegate() {
        return "1".equals(getProperty("permnegated").orElse(null));
    }

    @Override
    public Boolean getSkip() {
        return "1".equals(getProperty("permskip").orElse(null));
    }
}

package de.fearnixx.t3.service.permission.teamspeak;

import de.fearnixx.t3.teamspeak.data.DataHolder;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public class TS3Permission extends DataHolder implements ITS3Permission {

    private PriorityType type;
    private String sid;

    public TS3Permission(PriorityType type, String sid) {
        this.type = type;
        this.sid = sid;
    }

    @Override
    public PriorityType getPriorityType() {
        return type;
    }

    @Override
    public String getSID() {
        return sid;
    }

    @Override
    public String getSystemID() {
        return "teamspeak";
    }

    @Override
    public String getFullyQualifiedID() {
        return getSystemID() + getSID();
    }

    @Override
    public Integer getValue() {
        return Integer.parseInt(getValues().get("value"));
    }

    @Override
    public Boolean getNegate() {
        return null;
    }

    @Override
    public Boolean getSkip() {
        return null;
    }
}

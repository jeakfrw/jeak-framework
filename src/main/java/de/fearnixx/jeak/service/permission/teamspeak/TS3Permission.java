package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.profile.IUserIdentity;
import de.fearnixx.jeak.service.permission.base.Permission;

/**
 * Created by MarkL4YG on 07-Feb-18
 */
public class TS3Permission extends Permission implements ITS3Permission {

    private final PriorityType type;

    public TS3Permission(PriorityType type, String permSID) {
        super(IUserIdentity.SERVICE_TEAMSPEAK, permSID);
        this.type = type;
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

    public void setValue(Integer value) {
        setProperty("permvalue", value.toString());
    }

    public void setNegated(boolean negated) {
        setProperty("permnegated", negated ? "1" : "0");
    }

    public void setSkipped(boolean skipped) {
        setProperty("permskip", skipped ? "1" : "0");
    }
}

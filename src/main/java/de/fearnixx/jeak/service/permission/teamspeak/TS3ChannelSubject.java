package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.teamspeak.query.IQueryRequest;

import java.util.Optional;

public class TS3ChannelSubject extends TS3PermissionSubject implements ITS3ChannelSubject {

    private final int channelID;

    public TS3ChannelSubject(ITS3PermissionProvider permissionProvider, int channelID) {
        super(permissionProvider);
        this.channelID = channelID;
    }

    @Override
    public Integer getChannelID() {
        return channelID;
    }

    @Override
    public Optional<ITS3Permission> getTS3Permission(String permSID) {
        return Optional.empty();
    }

    @Override
    public Optional<ITS3Permission> getActiveTS3Permission(String permSID) {
        return Optional.empty();
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip, boolean permNegated) {
        return null;
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value, boolean permSkip) {
        return null;
    }

    @Override
    public IQueryRequest assignPermission(String permSID, int value) {
        return null;
    }

    @Override
    public IQueryRequest revokePermission(String permSID) {
        return null;
    }
}

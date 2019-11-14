package de.fearnixx.jeak.service.teamspeak;

import de.fearnixx.jeak.teamspeak.data.IUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CachedUserResult {

    private final List<IUser> users;
    private final LocalDateTime expiry;

    public CachedUserResult(List<IUser> users, LocalDateTime expiry) {
        this.users = users;
        this.expiry = expiry;
    }

    public List<IUser> getUsers() {
        return new ArrayList<>(users);
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }
}

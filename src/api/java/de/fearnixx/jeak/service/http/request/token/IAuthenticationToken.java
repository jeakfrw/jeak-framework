package de.fearnixx.jeak.service.http.request.token;

import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.teamspeak.data.IUser;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface IAuthenticationToken {

    String getTokenString();

    IUser getTokenOwner();

    Optional<ZonedDateTime> getExpiry();
}

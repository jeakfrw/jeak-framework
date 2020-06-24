package de.fearnixx.jeak.service.controller.request.token;

import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.teamspeak.data.IUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZonedDateTime;

public interface ITokenAuthService {

    @Nonnull
    IAuthenticationToken generateToken(IUser subject);

    void setTokenExpiry(@Nonnull IAuthenticationToken token, @Nullable ZonedDateTime expiryValue);

    void revokeToken(IAuthenticationToken token);

    void revokeTokens(ISubject subject);
}

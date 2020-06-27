package de.fearnixx.jeak.service.http.request.token;

import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.teamspeak.data.IUser;

import java.time.ZonedDateTime;

public interface ITokenAuthService {

    IAuthenticationToken generateToken(IUser subject);

    void setTokenExpiry(IAuthenticationToken token, ZonedDateTime expiryValue);

    void revokeToken(IAuthenticationToken token);

    void revokeTokens(ISubject subject);
}

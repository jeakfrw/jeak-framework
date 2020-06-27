package de.fearnixx.jeak.service.http.request.token;

import de.fearnixx.jeak.teamspeak.data.IUser;

import java.time.ZonedDateTime;

public interface ITokenAuthService {

    IAuthenticationToken generateToken(IUser tokenOwner);

    void setTokenExpiry(IAuthenticationToken token, ZonedDateTime expiryValue);

    void revokeToken(IAuthenticationToken token);

    void revokeTokens(IUser tokenOwner);
}

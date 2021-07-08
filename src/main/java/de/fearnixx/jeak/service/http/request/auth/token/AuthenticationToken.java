package de.fearnixx.jeak.service.http.request.auth.token;

import de.fearnixx.jeak.service.http.request.token.IAuthenticationToken;
import de.fearnixx.jeak.teamspeak.data.IUser;

import java.time.ZonedDateTime;
import java.util.Optional;

public class AuthenticationToken implements IAuthenticationToken {

    private final String tokenStr;
    private final IUser tokenOwner;
    private final ZonedDateTime tokenExpiry;

    public AuthenticationToken(String tokenStr, IUser tokenOwner, ZonedDateTime tokenExpiry) {
        this.tokenStr = tokenStr;
        this.tokenOwner = tokenOwner;
        this.tokenExpiry = tokenExpiry;
    }

    @Override
    public String getTokenString() {
        return tokenStr;
    }

    @Override
    public IUser getTokenOwner() {
        return tokenOwner;
    }

    @Override
    public Optional<ZonedDateTime> getExpiry() {
        return Optional.ofNullable(tokenExpiry);
    }
}

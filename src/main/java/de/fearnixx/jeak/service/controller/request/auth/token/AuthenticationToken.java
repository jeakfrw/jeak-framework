package de.fearnixx.jeak.service.controller.request.auth.token;

import de.fearnixx.jeak.service.controller.request.token.IAuthenticationToken;
import de.fearnixx.jeak.teamspeak.data.IUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.Optional;

public class AuthenticationToken implements IAuthenticationToken {

    private final String tokenStr;
    private final IUser tokenOwner;
    private final ZonedDateTime tokenExpiry;

    public AuthenticationToken(@Nonnull String tokenStr, @Nonnull IUser tokenOwner, @Nullable ZonedDateTime tokenExpiry) {
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

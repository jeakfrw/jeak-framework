package de.fearnixx.jeak.service.controller.request;

import de.fearnixx.jeak.service.controller.request.token.IAuthenticationToken;

import java.util.Optional;

public interface IRequestContext {

    Optional<IAuthenticationToken> getAuthenticationToken();
}

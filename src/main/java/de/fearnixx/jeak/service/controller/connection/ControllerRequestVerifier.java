package de.fearnixx.jeak.service.controller.connection;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.token.ITokenService;

public class ControllerRequestVerifier implements IConnectionVerifier {
    private static final String TOKEN_TEXT = "Token ";

    @Inject
    private ITokenService tokenService;

    @Override
    public boolean verifyRequest(String endpoint, String authorizationText) {
        boolean isAuthorized = false;
        if (isToken(authorizationText)) {
            isAuthorized = tokenService.verifyToken(endpoint, extractToken(authorizationText));
        }
        return isAuthorized;
    }

    private boolean isToken(String authorizationText) {
        return authorizationText.contains(TOKEN_TEXT);
    }

    /**
     * Extract the token from a given String. This requires that it actually is a token.
     *
     * @param authorizationText The text as {@links String}.
     * @return The token as {@link String}
     */
    private String extractToken(String authorizationText) {
        return authorizationText.replace(TOKEN_TEXT, "");
    }
}

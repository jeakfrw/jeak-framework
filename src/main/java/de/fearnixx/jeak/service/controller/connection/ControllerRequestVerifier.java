package de.fearnixx.jeak.service.controller.connection;

import de.fearnixx.jeak.reflect.Inject;

public class ControllerRequestVerifier implements IConnectionVerifier {
    private static final String TOKEN_TEXT = "Token ";

    @Inject
    private ITokenService tokenService;

    @Override
    public boolean verifyRequest(Class<?> controllerClass, String authorizationText) {
        boolean isAuthorized = false;
        if (isToken(authorizationText)) {
            isAuthorized = tokenService.verifyToken(controllerClass, extractToken(authorizationText));
        }
        return isAuthorized;
    }

    private boolean isToken(String authorizationText) {
        return authorizationText.contains(TOKEN_TEXT);
    }

    private String extractToken(String authorizationText) {
        return authorizationText.replace(TOKEN_TEXT, "");
    }
}

package de.fearnixx.jeak.controller.connection;

public class ControllerRequestVerifier implements IConnectionVerifier {
    @Override
    public boolean verifyRequest(Class<?> controllerClass, String authorizationText) {
        if (isToken(authorizationText)) {

        }
        return authorizationText.equals("Token Hallo");
    }

    private boolean isToken(String authorizationText) {
        return authorizationText.contains("Token ");
    }
}

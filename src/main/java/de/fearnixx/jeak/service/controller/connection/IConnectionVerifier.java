package de.fearnixx.jeak.service.controller.connection;

/**
 * Used to verify an authorization text.
 *
 */
public interface IConnectionVerifier {
    /**
     * Verify an HTTP-Authorization text.
     *
     * @param controllerClass The class of the controller, used to identify the necessary authorization information.
     * @param authorizationText The text from the HTTP-Authorization header.
     * @return true if the request could be verified,
     * false otherwise.
     */
    boolean verifyRequest(Class<?> controllerClass, String authorizationText);
}

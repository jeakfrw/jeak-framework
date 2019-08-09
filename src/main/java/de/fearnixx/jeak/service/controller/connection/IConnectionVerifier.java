package de.fearnixx.jeak.service.controller.connection;

/**
 * Used to verify an authorization text.
 *
 */
public interface IConnectionVerifier {
    /**
     * Verify an HTTP-Authorization text.
     *
     * @param endpoint The endpoint to verify the token for.
     * @param authorizationText The text from the HTTP-Authorization header.
     * @return true if the request could be verified, false otherwise.
     */
    boolean verifyRequest(String endpoint, String authorizationText);
}

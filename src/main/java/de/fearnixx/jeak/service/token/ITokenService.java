package de.fearnixx.jeak.service.token;

import java.util.Set;

public interface ITokenService {
    /**
     * Verify a given token.
     *
     * @param endpoint The endpoint to verify the token for.
     * @param token The token as {@link String} to verify.
     * @return true if the token could be verified,
     * false otherwise
     */
    boolean verifyToken(String endpoint, String token);

    /**
     * Generate a new token for the verification of requests.
     *
     * @param endpointSet The endpoints to register the token for. The List needs to have at least one item.
     * @return The generated token.
     */
    String generateToken(Set<String> endpointSet);
}

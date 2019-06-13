package de.fearnixx.jeak.service.token;

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
}

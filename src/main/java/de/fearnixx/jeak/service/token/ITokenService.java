package de.fearnixx.jeak.service.token;

public interface ITokenService {
    /**
     * Verify a given token.
     *
     * @param controllerClass The class of the used controller. Used to identify the token to use.
     * @param token The token as {@link String} to verify.
     * @return true if the token could be verified,
     * false otherwise
     */
    boolean verifyToken(Class<?> controllerClass, String token);
}

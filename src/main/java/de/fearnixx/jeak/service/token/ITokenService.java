package de.fearnixx.jeak.service.token;

public interface ITokenService {
    boolean verifyToken(Class<?> controllerClass, String token);
}

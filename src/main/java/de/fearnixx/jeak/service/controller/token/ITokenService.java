package de.fearnixx.jeak.service.controller.token;

public interface ITokenService {
    boolean verifyToken(Class<?> controllerClass, String token);
}

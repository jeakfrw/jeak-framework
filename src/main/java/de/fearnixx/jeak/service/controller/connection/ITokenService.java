package de.fearnixx.jeak.service.controller.connection;

public interface ITokenService {
    boolean verifyToken(Class<?> controllerClass, String token);
}

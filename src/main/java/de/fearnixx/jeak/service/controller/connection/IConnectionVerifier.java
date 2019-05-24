package de.fearnixx.jeak.service.controller.connection;

public interface IConnectionVerifier {
    boolean verifyRequest(Class<?> controllerClass, String authorizationText);
}

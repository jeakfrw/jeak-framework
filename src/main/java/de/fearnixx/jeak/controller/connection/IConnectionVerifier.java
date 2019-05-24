package de.fearnixx.jeak.controller.connection;

public interface IConnectionVerifier {
    boolean verifyRequest(Class<?> controllerClass, String authorizationText);
}

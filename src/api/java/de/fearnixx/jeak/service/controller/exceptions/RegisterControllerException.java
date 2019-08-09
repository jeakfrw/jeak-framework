package de.fearnixx.jeak.service.controller.exceptions;

public class RegisterControllerException extends RuntimeException {
    public RegisterControllerException(String message) {
        super(message);
    }

    public RegisterControllerException(String message, Throwable cause) {
        super(message, cause);
    }
}

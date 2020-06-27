package de.fearnixx.jeak.service.http.exceptions;

public class RegisterControllerException extends RuntimeException{
    public RegisterControllerException(String message) {
        super(message);
    }

    public RegisterControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegisterControllerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

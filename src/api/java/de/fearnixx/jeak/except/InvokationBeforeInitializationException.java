package de.fearnixx.jeak.except;

public class InvokationBeforeInitializationException extends RuntimeException {

    public InvokationBeforeInitializationException(String message) {
        super(message);
    }

    public InvokationBeforeInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokationBeforeInitializationException(Throwable cause) {
        super(cause);
    }
}

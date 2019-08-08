package de.fearnixx.jeak.service.permission.except;

/**
 * Fired when addMember or addParent calls detect an inheritance circularity.
 * This is strictly forbidden and therefore interrupts normal execution.
 */
public class CircularInheritanceException extends RuntimeException {

    public CircularInheritanceException() {
    }

    public CircularInheritanceException(String message) {
        super(message);
    }

    public CircularInheritanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CircularInheritanceException(Throwable cause) {
        super(cause);
    }
}

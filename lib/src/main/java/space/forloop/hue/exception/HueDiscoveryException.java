package space.forloop.hue.exception;

import java.io.Serial;
import java.io.Serializable;

/** Exception thrown when an error occurs during the discovery of Hue bridges. */
public final class HueDiscoveryException extends HueException implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    /** Constructs a new {@code HueDiscoveryException} with no detail message. */
    public HueDiscoveryException() {
        super();
    }

    /**
     * Constructs a new {@code HueDiscoveryException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public HueDiscoveryException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code HueDiscoveryException} with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of the exception
     */
    public HueDiscoveryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

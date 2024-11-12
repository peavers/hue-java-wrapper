package space.forloop.hue.exception;

import java.io.Serial;
import java.io.Serializable;

/**
 * Base exception class for all exceptions related to the Hue client library. This class is sealed
 * to restrict which classes can extend it.
 */
public sealed class HueException extends Exception implements Serializable
        permits HueDiscoveryException, HueConnectionException, HueAuthenticationException {

    @Serial private static final long serialVersionUID = 1L;

    /** Constructs a new {@code HueException} with no detail message. */
    public HueException() {
        super();
    }

    /**
     * Constructs a new {@code HueException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public HueException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code HueException} with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of the exception
     */
    public HueException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

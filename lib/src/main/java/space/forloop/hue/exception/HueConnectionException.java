package space.forloop.hue.exception;

import java.io.Serial;
import java.io.Serializable;

/** Exception thrown when a connection to the Hue bridge cannot be established. */
public final class HueConnectionException extends HueException implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    /** Constructs a new {@code HueConnectionException} with no detail message. */
    public HueConnectionException() {
        super();
    }

    /**
     * Constructs a new {@code HueConnectionException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public HueConnectionException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code HueConnectionException} with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of the exception
     */
    public HueConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

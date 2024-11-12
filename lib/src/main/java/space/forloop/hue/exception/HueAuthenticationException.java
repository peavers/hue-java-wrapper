package space.forloop.hue.exception;

import java.io.Serial;
import java.io.Serializable;

/** Exception thrown when authentication with the Hue bridge fails. */
public final class HueAuthenticationException extends HueException implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    /** Constructs a new {@code HueAuthenticationException} with no detail message. */
    public HueAuthenticationException() {
        super();
    }

    /**
     * Constructs a new {@code HueAuthenticationException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public HueAuthenticationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code HueAuthenticationException} with the specified detail message and
     * cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of the exception
     */
    public HueAuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

package fap.core.exception;

/**
 * General fap runtime exception.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 * @see RuntimeException
 */
public class CoreRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new core runtime exception with {@code null} as its message.
     */
    public CoreRuntimeException() {
    }

    /**
     * Constructs a new core runtime exception with the specified message.
     * 
     * @param msg the message
     */
    public CoreRuntimeException(String msg) {
        super(msg);
    }

}

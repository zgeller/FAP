package fap.core.exception;

/**
 * General fap exception.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 * @see Exception
 */
public class CoreException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new core exception with {@code null} as its message.
     */
    public CoreException() {
    }

    /**
     * Constructs a new core exception with the specified message.
     * 
     * @param msg the message
     */
    public CoreException(String msg) {
        super(msg);
    }

}

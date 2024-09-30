package fap.exception;

import fap.core.exception.CoreRuntimeException;

/**
 * Runtime exception thrown when the specified classifier is not compatible with
 * a task.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 * @see CoreRuntimeException
 */
public class IncompatibleClassifierException extends CoreRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new IncomparableTimeSeries exception with {@code null} as its
     * message.
     */
    public IncompatibleClassifierException() {
    }

    /**
     * Constructs a new IncomparableTimeSeries exception with the specified message.
     * 
     * @param msg the message
     */
    public IncompatibleClassifierException(String msg) {
        super(msg);
    }
    
}

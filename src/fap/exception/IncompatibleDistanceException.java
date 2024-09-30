package fap.exception;

import fap.core.exception.CoreRuntimeException;

/**
 * Runtime exception thrown when the specified distance measure is not
 * compatible with a task.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 * @see CoreRuntimeException
 */
public class IncompatibleDistanceException extends CoreRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new IncomparableTimeSeries exception with {@code null} as its
     * message.
     */
    public IncompatibleDistanceException() {
    }

    /**
     * Constructs a new IncomparableTimeSeries exception with the specified message.
     * 
     * @param msg the message
     */
    public IncompatibleDistanceException(String msg) {
        super(msg);
    }
    
}

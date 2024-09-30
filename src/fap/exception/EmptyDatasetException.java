package fap.exception;

import fap.core.data.Dataset;
import fap.core.exception.CoreRuntimeException;

/**
 * Runtime exception thrown when the specified dataset cannot be empty but it is empty.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 * @see CoreRuntimeException
 */
public class EmptyDatasetException extends CoreRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new EmptyDataset exception with {@code null} as its message.
     */
    public EmptyDatasetException() {
    }
    
    /**
     * Constructs a new EmptyDataset exception with the specified message.
     * 
     * @param msg the message
     */
    public EmptyDatasetException(String msg) {
        super(msg);
    }
    
    /**
     * Checks if the specified dataset is empty and throws an {@code EmptyDataset} exception
     * if it is empty.
     * 
     * @throws EmptyDatasetException if the training dataset is empty
     */
    public static void check(Dataset dataset) {
        
        if (dataset.isEmpty())
            throw new EmptyDatasetException("The dataset cannot be empty.");
        
    }

}

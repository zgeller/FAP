package fap.util;

/**
 * Defines common methods for classes that implements resumable operations.
 * 
 * <p>
 * Resumeable operations should be able to resume execution from (or close to)
 * the point where they were interrupted. Furthermore, they should be able to
 * {@link #reset} their state and thus restart the operation.
 * 
 * @author Zoltan Geller
 * @version 2024.07.18.
 */
public interface Resumable {

    /**
     * Resets the operation for reuse the object.
     */
    public void reset();

    /**
     * Indicates whether the resumable operation has completed.
     * 
     * @return {@code true} if the operation has completed
     */
    public boolean isDone();

    /**
     * Indicates whether the resumable operation is still in progress.
     * 
     * @return {@code true} if the resumable operation is still in progress
     */
    public boolean isInProgress();

}

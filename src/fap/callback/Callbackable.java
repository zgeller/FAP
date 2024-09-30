package fap.callback;

/**
 * Declares common methods for callbackable operations.
 * 
 * <p>
 * Callbackable operations should call the {@link Callback#callback(Object)
 * callback} method of the specified {@link Callback} object at regular
 * intervals.
 * 
 * <p>
 * The {@code Callback} object reports the desired number of callbacks via the
 * {@link Callback#getDesiredCallbackNumber() getDesiredCallbackNumber} method.
 * 
 * <p>
 * Depending on the total number of steps, the callbackable operation can report
 * the possible number of callbacks via the
 * {@link Callback#setPossibleCallbackNumber(int) setPossibleCallbackNumber}
 * method of the {@code Callback} object.
 * 
 * @author Zoltan Geller
 * @version 2024.08.17.
 * @see Callback
 */
public interface Callbackable {

    /**
     * Sets the callback object.
     * 
     * @param callback the callback object
     */
    public void setCallback(Callback callback);

    /**
     * Returns the callback object.
     * 
     * @return the callback object
     */
    public Callback getCallback();

}

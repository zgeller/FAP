/**
 * 
 */
package fap.callback;

/**
 * Defines common methods and fields for {@code Callback} objects.
 * 
 * @author Zoltan Geller
 * @version 2024.09.13.
 * @see Callback
 * @see Callbackable
 */
public abstract class AbstractCallback implements Callback {

    /**
     * The desired number of callbacks. Default value is {@code 30}.
     */
    protected int desiredCBNumber = 30;

    /**
     * The possible number of callbacks. Default value is {@code 30}.
     */
    protected int possibleCBNumber = 30;

    /**
     * The current number of callbacks.
     */
    protected int cbCount = 0;
    
    /**
     * Empty constructor.
     */
    public AbstractCallback() {
    }
    
    /**
     * Constructor that sets the desired number of callbacks.
     * @param desiredCBNumber the desired number of callbacks
     */
    public AbstractCallback(int desiredCBNumber) {
        this.desiredCBNumber = desiredCBNumber;
        this.setPossibleCallbackNumber(desiredCBNumber);
    }

    @Override
    public int getDesiredCallbackNumber() {
        return this.desiredCBNumber;
    }

    @Override
    public int getPossibleCallbackNumber() {
        return this.possibleCBNumber;
    }

    @Override
    public void setPossibleCallbackNumber(int cbNumber) {
        this.possibleCBNumber = cbNumber;
    }

    @Override
    public int getCallbackCount() {
        return this.cbCount;
    }

    /**
     * Sets the callback counter ({@link #cbCount}).
     * 
     * <p>
     * If a negative value is specified, the callback counter will be equal to the
     * number of possible callbacks.
     * 
     * @param cbCount the value of the callback counter to set
     * @see #getPossibleCallbackNumber()
     */
    @Override
    public void setCallbackCount(int cbCount) {
        if (cbCount < 0)
            this.cbCount = this.getPossibleCallbackNumber();
        else
            this.cbCount = cbCount;
    }

    /**
     * Increments the current number of callbacks ({@link #cbCount}) by one (1).
     */
    @Override
    public void callback(Object obj) throws Exception {
        cbCount++;
    }

    @Override
    public int getProgress() {
        return getProgress(this.getCallbackCount());
    }

    @Override
    public int getProgress(int cbCount) {

        int possibleCBNumber = this.getPossibleCallbackNumber();

        if (cbCount > possibleCBNumber)
            cbCount = possibleCBNumber;
        else if (cbCount < 0)
            cbCount = 0;
        
        int desired = this.getDesiredCallbackNumber();
        
        if (desired == 0)
            return cbCount;
        else
            return (int) ((cbCount / (double) possibleCBNumber) * desired);

    }

}

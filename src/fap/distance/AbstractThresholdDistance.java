package fap.distance;

import fap.util.TimeSeriesUtils;

/**
 * Defines basic methods for distance measures that rely on a matching
 * threshold.
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 * @see ThresholdDistance
 */
public abstract class AbstractThresholdDistance extends AbstractCopyableDistance implements ThresholdDistance {

    private static final long serialVersionUID = 1L;

    /**
     * The matching threshold. Two data points are considered to match if their
     * distance is not greater than the matching threshold. Default value is
     * specified by {@link TimeSeriesUtils#getMatchingThreshold()}.
     */
    private double epsilon = TimeSeriesUtils.getMatchingThreshold();
    
    /**
     * Empty constructor.
     */
    public AbstractThresholdDistance() {
    }
    
    /**
     * Constructor with the possibility to enable or disable storing distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractThresholdDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Constructor with a matching threshold.
     * 
     * @param epsilon the matching threshold
     */
    public AbstractThresholdDistance(double epsilon) {
        this.setEpsilon(epsilon);
    }
    
    /**
     * Constructor with a matching threshold and the possibility to enable or disable storing distances.
     * 
     * @param epsilon the matching threshold
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractThresholdDistance(double epsilon, boolean storing) {
        super(storing);
        this.setEpsilon(epsilon);
    }

    /**
     * Sets the value of the matching threshold. Two data points are considered to
     * match if their distance is not greater than the matching treshold. Must be
     * {@code epsilon >= 0}.
     * 
     * @param epsilon the new value of the matching threshold, it must be
     *                {@code >= 0}
     * @throws IllegalArgumentException if {@code epsilon < 0}
     */
    @Override
    public void setEpsilon(double epsilon) throws IllegalArgumentException {
        
        if (epsilon < 0)
            throw new IllegalArgumentException("Must be epsilon >= 0.");
        
        if (this.epsilon != epsilon) {
            this.clearStorage();
            this.epsilon = epsilon;
        }
        
    }

    @Override
    public double getEpsilon() {
        return this.epsilon;
    }
    
    /**
     * Initializes the specified distance with the common data structures of this
     * distance.
     * 
     * @param copy the distance whose data structures is to be initialized
     */
    protected void init(AbstractThresholdDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setEpsilon(this.getEpsilon());
    }
    

    @Override
    public String toString() {
        return super.toString() + ", epsilon=" + getEpsilon();
    }

}

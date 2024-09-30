package fap.core.data;

import java.io.Serializable;

/**
 * Declares common methods for time-series representations.
 * 
 * @author Aleksa Todorovic, Brankica Bratic, Zoltan Geller
 * @version 2024.09.14.
 * @see Serializable
 */
public interface Representation extends Serializable {

    public static final float OUTBOUND_VALUE = Float.NaN;

    /**
     * The value of the time series at timestamp {@code x} in this representation.
     * If point {@code x} is out of bounds, {@code OUTBOUND_VALUE} should returned.
     * 
     * @param x the timestamp of the data point
     * @return the value of the time series at timestamp {@code x} in this
     *         representation
     */
    public double getValue(double x);

    /**
     * Returns time series representation.
     * 
     * @return time series representation
     */
    public Object[] getRepresentation();
    
}

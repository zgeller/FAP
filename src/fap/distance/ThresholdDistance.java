package fap.distance;

import fap.core.distance.Distance;

/**
 * Declares basic methods for distance measures that rely on a matching
 * threshold.
 * 
 * @author Zoltan Geller
 * @version 2024.08.22.
 * @see Distance
 */
public interface ThresholdDistance extends Distance {

    /**
     * Sets the value of the matching threshold.
     * 
     * @param epsilon the value of the matching threshold
     */
    public void setEpsilon(double epsilon);

    /**
     * Returns the value of the matching threshold.
     * 
     * @return the value of the matching threshold
     */
    public double getEpsilon();

}

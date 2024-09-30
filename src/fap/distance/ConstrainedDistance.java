package fap.distance;

import fap.core.distance.Distance;

/**
 * Declares basic methods for constrained elastic distance measures.
 * 
 * @author Zoltan Geller
 * @version 2024.09.01.
 * @see Distance
 */
public interface ConstrainedDistance extends Distance {

    /**
     * Sets the relative width of the warping (editing) window (as a percentage of
     * the length of the time series).
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series)
     */
    public void setR(double r);

    /**
     * Returns the relative width of the warping (editing) window (as a percentage
     * of the length of the time series).
     * 
     * @return the relative width of the warping (editing) window (as a percentage
     *         of the length of the time series)
     */
    public double getR();

    /**
     * Sets the absolute width of the warping (editing) window.
     * 
     * @param w the absolute width of the warping (editing) window
     */
    public void setW(int w);

    /**
     * Returns the absolute width of the warping (editing) window.
     * 
     * @return the absolute width of the warping (editing) window
     */
    public int getW();

}

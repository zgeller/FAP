package fap.distance;

import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Minkowski (Lp) distance measure. Time series must be the same length (n):
 * 
 * <blockquote> <img src="doc-files/MinkowskiDistance-1.png"> </blockquote>
 * 
 * <p>
 * Default values:
 * <ul>
 * <li>{@code p = 2}
 * </ul>
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 */
public class MinkowskiDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * The {@code p} parameter. Default value is {@code 2}.
     */
    private double p = 2;

    /**
     * Constructs a new Minkowski (Lp) distance measure with the default value of
     * {@link #p}.
     */
    public MinkowskiDistance() {
    }

    /**
     * Constructs a new Minkowski (Lp) distance measure with the default value of
     * {@link #p}, and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public MinkowskiDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Constructs a new Minkowski (Lp) distance measure with the specified value of
     * {@code p}.
     * 
     * @param p the value of p
     */
    public MinkowskiDistance(double p) {
        this.setP(p);
    }
    
    /**
     * Constructs a new Minkowski (Lp) distance measure with the specified value of
     * {@code p}, and sets whether to store distances.
     * 
     * @param p       the value of p
     * @param storing {@code true} if storing distances should be enabled
     */
    public MinkowskiDistance(double p, boolean storing) {
        super(storing);
        this.setP(p);
    }

    /**
     * Sets the value of the parameter {@code p}.
     * 
     * @param p the value of {@code p}
     */
    public void setP(double p) {
        
        if (this.p != p) {
            this.clearStorage();
            this.p = p;
        }
        
    }

    /**
     * Returns the value of the parameter {@code p}.
     * 
     * @return the value of {@code p}
     */
    public double getP() {
        return this.p;
    }

    /**
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     */
    @Override
    public double distance(TimeSeries series1, TimeSeries series2) throws IncomparableTimeSeriesException {

        // try to recall the distance
        double distance = this.recall(series1, series2);
        if (!Double.isNaN(distance))
            return distance;
        
        int len = IncomparableTimeSeriesException.checkLength(series1, series2);

        distance = 0;
        
        double p = this.getP();

        for (int i = 0; i < len; i++) {
            
            double y1 = series1.getY(i);
            double y2 = series2.getY(i);

            double tmp = Math.abs(y1 - y2);

            if (p == Double.POSITIVE_INFINITY)
                distance = Math.max(distance, tmp);
            else
                distance += Math.pow(tmp, p);
            
        }

        if (p < Double.POSITIVE_INFINITY)
            distance = Math.pow(distance, 1 / p);

        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;

    }

    /**
     * Initializes the specified distance measure with the common data structures of this
     * distance.
     * 
     * @param copy the distance measure whose data structures is to be initialized
     */
    protected void init(MinkowskiDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setP(this.getP());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        MinkowskiDistance copy = new MinkowskiDistance();
        init(copy, deep);
        return copy;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", p=" + getP();
    }

}

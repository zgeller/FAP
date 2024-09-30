package fap.distance;

import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Manhattan distance (L1, city block) distance measure. Time series must be the
 * same length (n):
 * 
 * <blockquote> <img src="doc-files/ManhattanDistance-1.png"> </blockquote>
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 */
public class ManhattanDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Manhattan (L1, city block) distance measure.
     */
    public ManhattanDistance() {
    }
    
    /**
     * Constructs a new Manhattan (L1, city block) distance measure and
     * sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public ManhattanDistance(boolean storing) {
        super(storing);
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

        for (int i = 0; i < len; i++) {
            
            double y1 = series1.getY(i);
            double y2 = series2.getY(i);

            distance += Math.abs(y1 - y2);
            
        }

        // save the distance into the memory
        this.store(series1, series2, distance);

        return distance;

    }

    @Override
    public Object makeACopy(boolean deep) {
        ManhattanDistance copy = new ManhattanDistance();
        init(copy, deep);
        return copy;
   }

}
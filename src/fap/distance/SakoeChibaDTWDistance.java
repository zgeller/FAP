package fap.distance;

import fap.core.data.TimeSeries;
import fap.distance.util.ConstraintUtils;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Sakoe-Chiba constrained {@link DTWDistance DTW} (Dynamic Time Warping) distance measure. Time
 * series must be the same length.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> H. Sakoe, S. Chiba, Dynamic programming algorithm optimization for spoken
 *       word recognition, IEEE Trans. Acoust. 26 (1978) 43–49. 
 *       <a href="https://doi.org/10.1109/TASSP.1978.1163055">
 *          https://doi.org/10.1109/TASSP.1978.1163055</a>.
 * </ol>
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractConstrainedDistance
 * @see DTWDistance
 */
public class SakoeChibaDTWDistance extends AbstractConstrainedDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Sakoe-Chiba constrained DTW distance measure with the default
     * warping-widnow width ({@link AbstractConstrainedDistance#r r}).
     */
    public SakoeChibaDTWDistance() {
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained DTW distance measure with the default
     * warping-widnow width ({@link AbstractConstrainedDistance#r r}) and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaDTWDistance(boolean storing) {
        super(storing);
    }

    /**
     * Constructs a new Sakoe-Chiba constrained DTW distance measure with the specified
     * relative warping-window width.
     * 
     * @param r the relative width of the warping window (as a percentage of the
     *          length of the time series)
     */
    public SakoeChibaDTWDistance(double r) {
        super(r);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained DTW distance measure with the specified
     * relative warping-window width and whether to store distances.
     * 
     * @param r       the relative width of the warping window (as a percentage of
     *                the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaDTWDistance(double r, boolean storing) {
        super(r, storing);
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
        
        // throws IncomparableTimeSeriesException if the time series are not the same
        // length
        int scWidth = ConstraintUtils.getWarpingWindowWidth(series1, series2, this.getR(), this.getW());

        int len = series1.length();

        double curRow[] = new double[len + 1];
        double prevRow[] = new double[len + 1];

        // initialization
        prevRow[0] = 0;
        for (int i = 1; i <= len; i++)
            prevRow[i] = Double.POSITIVE_INFINITY;

        double tmp[];

        for (int i = 1; i <= len; i++) {

            int start = Math.max(1, i - scWidth);
            int end = Math.min(len, i + scWidth);

            // initializing left and right side
            
            curRow[start - 1] = Double.POSITIVE_INFINITY;   // left side
            
            if (i + scWidth <= len)
                prevRow[end] = Double.POSITIVE_INFINITY;    // right side

            double y1 = series1.getY(i - 1);

            for (int j = start; j <= end; j++) {

                double delta = y1 - series2.getY(j - 1);

                curRow[j] = delta * delta + // Math.abs(delta) +
                        Math.min(prevRow[j], Math.min(prevRow[j - 1], curRow[j - 1]));

            }

            tmp = curRow;
            curRow = prevRow;
            prevRow = tmp;
        }
        
        distance = prevRow[len];
        
        // save the distance into the memory
        this.store(series1, series2, distance);

        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        SakoeChibaDTWDistance copy = new SakoeChibaDTWDistance();
        init(copy, deep);
        return copy;
    }

}

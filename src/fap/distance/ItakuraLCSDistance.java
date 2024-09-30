package fap.distance;

import fap.core.data.TimeSeries;
import fap.distance.util.ItakuraParallelogram;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Itakura-constrained {@link LCSDistance LCS} (Longest Common Subsequence)
 * distance measure. Time series must be the same length.
 *
 * <p>
 * Two data points are considered to match if their distance is not greater than
 * the {@link AbstractConstrainedThresholdDistance#epsilon matching threshold}.
 * 
 * <p>
 * References:
 * <ol>
 * <li> F. Itakura, Minimum prediction residual principle applied to speech
 *      recognition, IEEE Trans. Acoust. 23 (1975) 67–72. 
 *      <a href="https://doi.org/10.1109/TASSP.1975.1162641">
 *         https://doi.org/10.1109/TASSP.1975.1162641</a>.
 * </ol>
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractConstrainedDistance
 * @see LCSDistance
 */
public class ItakuraLCSDistance extends AbstractConstrainedThresholdDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Auxiliary object for generating and storing Itakura parallelograms.
     */
    private ItakuraParallelogram itPara = new ItakuraParallelogram();

    /**
     * Constructs a new Itakura constrained LCS distance measure with the default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}).
     */
    public ItakuraLCSDistance() {
    }
    
    /**
     * Constructs a new Itakura constrained LCS distance measure with the default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}), and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraLCSDistance(boolean storing) {
        super(storing);
    }

    /**
     * Constructs a new Itakura constrained LCS distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}).
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series)
     */
    public ItakuraLCSDistance(double r) {
        super(r);
    }
    
    /**
     * Constructs a new Itakura constrained LCS distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}), and sets
     * whether to store distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraLCSDistance(double r, boolean storing) {
        super(r, storing);
    }
    
    /**
     * Constructs a new Itakura constrained LCS distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the matching
     * threshold value ({@code epsilon}).
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     */
    public ItakuraLCSDistance(double r, double epsilon) {
        super(r, epsilon);
    }
    
    /**
     * Constructs a new Itakura constrained LCS distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the matching
     * threshold value ({@code epsilon}), and sets whether to store distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraLCSDistance(double r, double epsilon, boolean storing) {
        super(r, epsilon, storing);
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
        int sei[][] = itPara.getSEI(series1, series2, getR(), getW()); 

        int len = series1.length();

        int startj[] = sei[0];
        int endj[] = sei[1];

        long min = 0;

        long curRow[] = new long[len + 1];
        long prevRow[] = new long[len + 1];

        double epsilon = getEpsilon();

        // initialization
        prevRow[0] = min;

        int prevEnd = 0;

        long tmp[];

        for (int i = 1; i <= len; i++) {

            int start = startj[i];
            int end = endj[i];
            
            // initializing left and right side

            curRow[start - 1] = prevRow[start - 1]; // left side

            if (prevEnd < len)
                for (int t = prevEnd + 1; t <= end; t++) // right side
                    prevRow[t] = prevRow[prevEnd];
            prevEnd = end;

            double y1 = series1.getY(i - 1);

            for (int j = start; j <= end; j++) {
                
                int jm1 = j - 1;
                double y2 = series2.getY(jm1);

                if (Math.abs(y1 - y2) <= epsilon)
                    curRow[j] = 1 + prevRow[jm1];
                else
                    curRow[j] = Math.max(prevRow[j], curRow[jm1]);
                
            }

            tmp = curRow;
            curRow = prevRow;
            prevRow = tmp;

        }

        distance = 0;
        if (len > 0)
            distance = (double) (len - prevRow[len]) / (double) len;

        // save the distance into the memory
        this.store(series1, series2, distance);

        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        ItakuraLCSDistance copy = new ItakuraLCSDistance();
        init(copy, deep);
        return copy;
    }

}

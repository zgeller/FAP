package fap.distance;

import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;
import fap.util.MathUtils;

/**
 * Wave-Hedges distance measure. Time series must be the same length (n): 
 * 
 * <blockquote> <img src="doc-files/WaveHedgesDistance-1.png"> </blockquote>
 * 
 * <ul>
 *  <li> <b>If not all elements of A and B are non-negative, the result may be
 *       negative.</b></li>
 *  <li> {@code 0/0} is treated as {@code 0} (see [2])
 *  <li> zero denominator is replaced by {@link MathUtils#getZeroDenominator()} 
 *       (see [2])
 * </ul>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> M.M. Deza, E. Deza, Encyclopedia of Distances, Springer Berlin
 *       Heidelberg, Berlin, Heidelberg, 2016.
 *       <a href="https://doi.org/10.1007/978-3-662-52844-0">
 *          https://doi.org/10.1007/978-3-662-52844-0</a>.
 *  <li> S.-H. Cha, Comprehensive Survey on Distance/Similarity Measures between
 *       Probability Density Functions, Int. J. Math. Model. Methods Appl. Sci. 1
 *       (2007) 300–307. 
 *       <a href="http://www.gly.fsu.edu/~parker/geostats/Cha.pdf">
 *          http://www.gly.fsu.edu/~parker/geostats/Cha.pdf</a>.
 * </ol>
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 */

public class WaveHedgesDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Wave-Hedges distance measure.
     */
    public WaveHedgesDistance() {
    }
    
    /**
     * Constructs a new Wave-Hedges distance measure and sets whether to store
     * distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public WaveHedgesDistance(boolean storing) {
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

            double abs = Math.abs(y1 - y2);
            double max = y1;
            if (y2 > y1)
                max = y2;
            
            // 0/0 is treated as 0 (see [2]) 
            if (abs != 0 && max != 0)
                distance += abs / max;
            else if (max == 0)
                distance += abs / MathUtils.getZeroDenominator();
            
        }
        
        // save the distance into the memory
        this.store(series1, series2, distance);

        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        WaveHedgesDistance copy = new WaveHedgesDistance();
        init(copy, deep);
        return copy;
    }

}

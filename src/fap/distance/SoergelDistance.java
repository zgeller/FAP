/**
 * 
 */
package fap.distance;

import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;
import fap.util.MathUtils;

/**
 * Soergel distance measure. Time series must be the same length (n):
 *
 * <blockquote> <img src="doc-files/SoergelDistance-1.png"> </blockquote>
 * 
 * <ul>
 *  <li> <b>If not all elements of A and B are non-negative, the result may be
 *       negative.</b>
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
 *  <li> H.A. Abu Alfeilat, A.B.A. Hassanat, O. Lasassmeh, A.S. Tarawneh, M.B.
 *       Alhasanat, H.S. Eyal Salman, V.B.S. Prasath, Effects of Distance Measure
 *       Choice on K-Nearest Neighbor Classifier Performance: A Review, Big Data. 7
 *       (2019) 221–248. 
 *       <a href="https://doi.org/10.1089/big.2018.0175">
 *          https://doi.org/10.1089/big.2018.0175</a>.
 * </ol>
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 */
public class SoergelDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Soergel distance measure.
     */
    public SoergelDistance() {
    }
    
    /**
     * Constructs a new Soergel distance measure and sets whether to store
     * distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public SoergelDistance(boolean storing) {
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

        double sumAbs = 0;
        double sumMax = 0;

        for (int i = 0; i < len; i++) {
            
            double y1 = series1.getY(i);
            double y2 = series2.getY(i);

            sumAbs += Math.abs(y1 - y2);
            
            if (y1 < y2)
                sumMax += y2;
            else
                sumMax += y1;

        }

        if (sumAbs == 0 && sumMax == 0)
            distance = 0;
        else if (sumMax == 0)
            distance = sumAbs / MathUtils.getZeroDenominator();
        else
            distance = sumAbs / sumMax;
        
        // save the distance into the memory
        this.store(series1, series2, distance);

        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        SoergelDistance copy = new SoergelDistance();
        init(copy, deep);
        return copy;
    }

}

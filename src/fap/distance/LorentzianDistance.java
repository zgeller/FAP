package fap.distance;

import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Lorentzian distance measure. Time series must be the same length (n):
 * 
 * <blockquote> <img src="doc-files/LorentzianDistance-1.png"> </blockquote>
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
public class LorentzianDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Lorentzian distance measure.
     */
    public LorentzianDistance() {
    }

    /**
     * Constructs a new Lorentzian distance measure and sets whether to store
     * distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public LorentzianDistance(boolean storing) {
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

            distance += Math.log(1 + Math.abs(y1 - y2));
            
        }

        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        LorentzianDistance copy = new LorentzianDistance();
        init(copy, deep);
        return copy;
    }

}

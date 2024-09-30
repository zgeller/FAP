package fap.util;

import fap.core.data.DataPoint;
import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Statistic utilities.
 * 
 * @author Zoltan Geller
 * @version 2024.08.28.
 */
public final class StatisticsUtils {

    private StatisticsUtils() {
    }

    /**
     * Corrected Repeated r-time k-fold Cross-Validation test statistic.
     * 
     * <p>
     * References:
     * <ol>
     *  <li> R.R. Bouckaert, E. Frank, Evaluating the Replicability of Significance
     *       Tests for Comparing Learning Algorithms, in: H. Dai, R. Srikant, C. Zhang
     *       (Eds.), Adv. Knowl. Discov. Data Min., Springer Berlin Heidelberg, 2004: pp.
     *       3–12. 
     *       <a href="https://doi.org/10.1007/978-3-540-24775-3_3">
     *          https://doi.org/10.1007/978-3-540-24775-3_3</a>.
     * </ol>
     * 
     * @param series the differences between the results
     * @param n1     number of instances used for training
     * @param n2     number of instances used for testing
     * @return the corrected repeated r-times k-fold cross-validation test statistic
     */
    public static double correctedRepeatedCVTest(TimeSeries series, int n1, int n2) {
        return correctedRepeatedCVTest(series, (double) n2 / n1);
    }

    /**
     * Corrected Repeated r-time k-fold Cross-Validation test statistic.
     * 
     * <p>
     * References:
     * <ol>
     *  <li> R.R. Bouckaert, E. Frank, Evaluating the Replicability of Significance
     *       Tests for Comparing Learning Algorithms, in: H. Dai, R. Srikant, C. Zhang
     *       (Eds.), Adv. Knowl. Discov. Data Min., Springer Berlin Heidelberg, 2004: pp.
     *       3–12. 
     *       <a href="https://doi.org/10.1007/978-3-540-24775-3_3">
     *          https://doi.org/10.1007/978-3-540-24775-3_3</a>.
     * </ol>
     * 
     * @param series     the differences between the results
     * @param correction n2/n1
     * @return the corrected repeated r-times k-fold cross-validation test statistic
     */
    public static double correctedRepeatedCVTest(TimeSeries series, double correction) {
        
        double mean = series.getMeanY();
        
        double variance = series.getVarianceY();
        
        return mean / Math.sqrt(variance * (1d / series.length() + correction));
        
    }

    /**
     * Corrected Repeated r-times k-fold Cross-Validation test statistic.
     * 
     * <p>
     * References:
     * <ol>
     *  <li> R.R. Bouckaert, E. Frank, Evaluating the Replicability of Significance
     *       Tests for Comparing Learning Algorithms, in: H. Dai, R. Srikant, C. Zhang
     *       (Eds.), Adv. Knowl. Discov. Data Min., Springer Berlin Heidelberg, 2004: pp.
     *       3–12. 
     *       <a href="https://doi.org/10.1007/978-3-540-24775-3_3">
     *          https://doi.org/10.1007/978-3-540-24775-3_3</a>.
     * </ol>
     * 
     * @param a  the first time series
     * @param b  the second time series
     * @param n1 number of instances used for training
     * @param n2 number of instances used for testing
     * @return the corrected repeated r-times k-fold cross-validation test statistic
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     */
    public static double correctedRepeatedCVTest(TimeSeries a, TimeSeries b, int n1, int n2) {
        return correctedRepeatedCVTest(a, b, (double) n2 / n1);
    }

    /**
     * Corrected Repeated r-times k-fold Cross-Validation test statistic.
     * 
     * <p>
     * References:
     * <ol>
     *  <li> R.R. Bouckaert, E. Frank, Evaluating the Replicability of Significance
     *       Tests for Comparing Learning Algorithms, in: H. Dai, R. Srikant, C. Zhang
     *       (Eds.), Adv. Knowl. Discov. Data Min., Springer Berlin Heidelberg, 2004: pp.
     *       3–12. 
     *       <a href="https://doi.org/10.1007/978-3-540-24775-3_3">
     *          https://doi.org/10.1007/978-3-540-24775-3_3</a>.
     * </ol>
     * 
     * @param series1          the first time series
     * @param series2          the second time series
     * @param correction       n2 / n1
     * @return the corrected repeated r-times k-fold cross-validation test statistic
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     */
    public static double correctedRepeatedCVTest(TimeSeries series1, TimeSeries series2, double correction) {
        
        IncomparableTimeSeriesException.checkLength(series1, series2);
        
        double len = series1.length();
        
        TimeSeries xts = new TimeSeries();
        
        for (int i = 0; i < len; i++)
            xts.add(new DataPoint(i, series1.getY(i) - series2.getY(i)));
        
        return correctedRepeatedCVTest(xts, correction);
        
    }
}

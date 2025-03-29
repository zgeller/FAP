/*   
 * Copyright 2024 Zoltán Gellér
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fap.util;

import fap.core.data.DataPoint;
import fap.core.data.TimeSeries;

/**
 * Time series utilities.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.30.
 */
public final class TimeSeriesUtils {

    private TimeSeriesUtils() {
    }

    /***
     * The matching threshold (ε). {@code a} and {@code b} are considered to match
     * if {@code |a-b| ≤ ε}. The default value of the threshold parameter is 0.25
     * based on [1] and and assuming that the time series are {@code z}-normalized.
     * 
     * <ol>
     *  <li> L. Chen, M.T. Özsu, V. Oria, Robust and fast similarity search for moving
     *       object trajectories, in: Proc. 2005 ACM SIGMOD Int. Conf. Manag. Data -
     *       SIGMOD ’05, ACM Press, New York, New York, USA, 2005: pp. 491–502.
     *       <a href="https://doi.org/10.1145/1066157.1066213">
     *          https://doi.org/10.1145/1066157.1066213</a>.
     * </ol>
     */
    private static double matchingThreshold = 0.25;

    /**
     * Returns the matching threshold.
     * 
     * @return the matching threshold
     */
    public static double getMatchingThreshold() {
        return matchingThreshold;
    }

    /**
     * Sets the matching threshold.
     * 
     * @param matchingThreshold the matching threshold to set
     */
    public static void setMatchingThreshold(double matchingThreshold) {
        TimeSeriesUtils.matchingThreshold = matchingThreshold;
    }

    /**
     * Calculates the k-entropy of a time series.
     * 
     * @param neighbours matrix of neighbors
     * @param classes    array of classes (labels)
     * @param labels     classes (labels) of the time series
     * @param k          number of nearest neighbors to consider
     * @param index      index of the time series for which is k-entropy is
     *                   calculated
     * @return k-entropy of the given time series
     */
    public static double getKEntropy(int[][] neighbours, double[] classes, double[] labels, int k, int index) {
        double kEntropy = 0;
        double dk = (double) k;
        for (double label : classes) {
            int count = 0;
            for (int i = 0; i < k; i++)
                if (label == labels[neighbours[index][i]])
                    count++;
            if (count > 0) {
                double p = count / dk;
                kEntropy += p * Math.log(p);
            }
        }
        return -kEntropy;
    }

    /**
     * Calculates the k-entropy of a dataset.
     * 
     * @param neighbours matrix of neighbors
     * @param classes    array of classes (labels)
     * @param labels     classes (labels) of the time series
     * @param k          number of nearest neighbors to consider
     * @return k-entropy of the given dataset
     */
    public static double getKEntropy(int[][] neighbours, double[] classes, double[] labels, int k) {
        double kEntropy = 0;
        int len = labels.length;
        for (int index = 0; index < len; index++)
            kEntropy += getKEntropy(neighbours, classes, labels, k, index);
        return kEntropy / len;
    }

    /**
     * Calculates the k-entropy of individual time series and the dataset.
     * 
     * @param neighbours matrix of neighbors
     * @param classes    array of classes (labels)
     * @param labels     classes (labels) of the time series
     * @param k          number of nearest neighbors to consider
     * @return array of k-entropies of individual times series and the dataset as
     *         the last element of the array
     */
    public static double[] getKEntropies(int[][] neighbours, double[] classes, double[] labels, int k) {
        int len = labels.length;
        double[] kEntropies = new double[len + 1];
        double kEntropy = 0;
        for (int index = 0; index < len; index++) {
            double indexKEntropy = getKEntropy(neighbours, classes, labels, k, index);
            kEntropies[index] = indexKEntropy;
            kEntropy += indexKEntropy;
        }
        kEntropies[len] = kEntropy / len;
        return kEntropies;
    }

    /**
     * Z-normalizes the specified time series using sample standard deviation:
     * 
     * <blockquote> <img src="doc-files/TimeSeriesUtils-zNorm.png">, </blockquote>
     * 
     * where <code>μ<sub>A</sub></code> is the mean value of {@code A}, and
     * <code>σ<sub>A</sub></code> the standard deviation of {@code A}.
     * <p>
     * 
     * @param series the time series that is to be z-normalized
     */
    public static void zNormalize(TimeSeries series) {
        zNormalize(series, false);
    }

    /**
     * Z-normalizes the specified time series:
     * 
     * <blockquote> <img src="doc-files/TimeSeriesUtils-zNorm.png">, </blockquote>
     * 
     * where <code>μ<sub>A</sub></code> is the mean value of {@code A}, and
     * <code>σ<sub>A</sub></code> the standard deviation of {@code A}.
     * <p>
     * 
     * @param series     the time series that is to be z-normalized
     * @param population indicates whether to use population {@code true} od sample
     *                   {@code false} standard deviation
     */
    public static void zNormalize(TimeSeries series, boolean population) {

        if (series.length() > 1) {

            double stdev = series.getStDevY(population);

            if (stdev != 0) {

                double mean = series.getMeanY();

                for (DataPoint dp : series)
                    dp.setY((dp.getY() - mean) / stdev);

            }

        }

    }
    
    /**
     * Mean normalizes the specified time series:
     * 
     * <blockquote> <img src="doc-files/TimeSeriesUtils-meanNorm.png">, </blockquote>
     * 
     * where <code>μ<sub>A</sub></code> is the mean value of {@code A}.
     * <p>
     * 
     * @param series the time series to be mean normalized
     */
    public static void meanNormalize(TimeSeries series) {
        
        double mean = series.getMeanY();
        double delta = series.getMaxY() - series.getMinY();
        
        for (DataPoint dp : series) {
            
            if (delta != 0.0)
                dp.setY((dp.getY() - mean) / delta);
            
            else
                dp.setY(0);
            
        }
        
    }

    /**
     * Transforms the data points of the specified time series into range
     * {@code [0, 1]}:
     *
     * <blockquote> <img src="doc-files/TimeSeriesUtils-minMax-1.png"> </blockquote>
     * 
     * @param series the time series that is to be min-max normalized.
     */
    public static void minMaxNormalize(TimeSeries series) {
        minMaxNormalize(series, 0, 1);
    }

    /**
     * Transforms the data points of the specified time series into range
     * {@code [min, max]}:
     * 
     * <blockquote> <img src="doc-files/TimeSeriesUtils-minMax-2.png"> </blockquote>
     * 
     * @param series the time series that is to be min-max normalized.
     * @param min    the lower bound
     * @param max    the upper bound
     */
    public static void minMaxNormalize(TimeSeries series, double min, double max) {

        double mins = series.getMinY();
        double deltas = series.getMaxY() - mins;
        double delta = max - min;

        if (deltas != 0.0)

            for (DataPoint dp : series)
                dp.setY(min + ((dp.getY() - mins) / deltas) * delta);

        else

            for (DataPoint dp : series)
                dp.setY(min);

    }

    /**
     * Maximum absolute normalizes the specified time series:
     * 
     * <blockquote> <img src="doc-files/TimeSeriesUtils-maxAbs.png"> </blockquote>
     * 
     * @param series the time series to be maximum absolute normalized
     */
    public static void maxAbsNormalize(TimeSeries series) {
       
        double absMax = series.getMaxAbsY();

        if (absMax > 0)
            for (DataPoint dp : series)
                dp.setY(dp.getY() / absMax);
        
    }
    
    /**
     * Decimal scales the specified time series:
     *
     * <blockquote> <img src="doc-files/TimeSeriesUtils-decimalScaling.png">, </blockquote>
     * 
     * where {@code d} is the number of digits in the integer part of <code>max(|A|)</code>.
     * <p>
     * 
     * @param series the time series that is to be decimal scaled
     */
    public static void decimalScale(TimeSeries series) {

        int k = Long.toString((long) series.getMaxAbsY()).length();

        double tenp = Math.pow(10, k);

        for (DataPoint dp : series)
            dp.setY(dp.getY() / tenp);

    }

    /**
     * Scales the y coordinate of the data points of th specified time series by the specified
     * factor.
     * 
     * @param series the time series that is to be scaled
     * @param scaley scale factor for the y coordinate of the data points
     */
    public static void scale(TimeSeries series, double scaley) {
        
        for (DataPoint dp : series)
            dp.setY(dp.getY() * scaley);
        
    }
    
    /**
     * Scales the x and y coordinates of the data points of the specified time
     * series by the specified factors.
     * 
     * @param series the time series that is to be scaled
     * @param scalex scale factor for the x coordinate of the data points
     * @param scaley scale factor for the y coordinate of the data points
     */
    public static void scale(TimeSeries series, double scalex, double scaley) {

        for (DataPoint dp : series) {
            dp.setX(dp.getX() * scalex);
            dp.setY(dp.getY() * scaley);
        }

    }

    /**
     * Shifts the y coordinate of the data points of the specified time series by
     * the specified factor.
     * 
     * @param series the time series that is to be shifted
     * @param scaley shift factor for the y coordinate of the data points
     */
    public static void shift(TimeSeries series, double shifty) {

        for (DataPoint dp : series)
            dp.setY(dp.getY() + shifty);
        
    }

    /**
     * Shifts the x and y coordinates of the data points of the specified time
     * series by the specified factors.
     * 
     * @param series the time series that is to be shifted
     * @param scalex shift factor for the x coordinate of the data points
     * @param scaley shift factor for the y coordinate of the data points
     */
    public static void shift(TimeSeries series, double shiftx, double shifty) {
        
        for (DataPoint dp : series) {
            dp.setX(dp.getX() + shiftx);
            dp.setY(dp.getY() + shifty);
        }
        
    }
    
    /**
     * Transforms the given time series <b>not taking into account</b> x coordinates
     * of the original series.
     * 
     * @author Lidija Fodor, Zoltán Gellér
     * @version 2024.09.10.
     * 
     * @param series the time series to transform
     * @return the transformed time series
     */
    private static TimeSeries getLinearlyEquiscaledWithoutX(TimeSeries series, int desiredLength) {

        int slen = series.length() - 1; // length of series - 1
        double pointsDistance = (double) slen / (desiredLength - 1); // future distance between points in series

        double x = 0; // current x position of the new point
        int index = 0; // the number of already calculated points in series
        TimeSeries result = new TimeSeries(); // result time series

        double y1 = 0; // y coordinate for x1
        double y2 = 0; // y coordinate for x2
        double deltaY = 0; // y2-y1
        int prevX1 = -1; // the previous value of x1;

        DataPoint newPoint = null; // new point

        while (index < desiredLength) {

            int x1 = (int) x; // the index of the previous point in the original series

            // the new point is the last point from the original series
            if (x >= slen || index + 1 == desiredLength)
                newPoint = new DataPoint(index, series.getY(slen));

            // the place of the new point matches the place of the previous point
            else if (x == x1)
                newPoint = new DataPoint(index, series.getY(x1));

            // the new point is between the previous and the next point
            else {
                // calculating the y coordinate of the new point
                // calculating the equation of line, knowing two points it passes through:
                // A(x1,y1), B(x2,y2)
                // newY = (y2-y1)*(x-x1) + y1
                if (prevX1 != x1) {
                    y1 = series.getY(x1);
                    y2 = series.getY(x1 + 1);
                    deltaY = y2 - y1;
                    prevX1 = x1;
                }
                
                newPoint = new DataPoint(index, deltaY * (x - x1) + y1);
            }

            result.add(newPoint);
            x += pointsDistance;
            ++index;
        }

        return result;

    }

    /**
     * Transforms the given time series <b>taking into account</b> x coordinates of
     * the original series.
     * 
     * @author Lidija Fodor, Zoltán Gellér
     * @version 2024.09.10.
     * 
     * @param series the time series to transform
     * @return the transformed time series
     */
    private static TimeSeries getLinearlyEquiscaledWithX(TimeSeries series, int desiredLength) {

        int slen = series.length() - 1; // length of series - 1
        double pointsDistance = (double) slen / (desiredLength - 1); // future distance between points in series
        double realx = series.getX(0); // the real value of the current x position of the new point
        double realDistance = (series.getX(slen) - realx) / (desiredLength - 1);

        double x = 0; // current x position of the new point
        int index = 0; // the number of already calculated points in series
        TimeSeries result = new TimeSeries(); // result time series

        double y1 = 0; // y coordinate for x1
        double y2 = 0; // y coordinate for x2
        double deltaY = 0; // y2-y1
        int prevX1 = -1; // the previous value of x1;

        DataPoint newPoint = null; // new point

        while (index < desiredLength) {

            int x1 = (int) x; // the index of the previous point in the original series

            // the new point is the last point from the original series
            if (x >= slen || index + 1 == desiredLength)
                newPoint = new DataPoint(realx, series.getY(slen));

            // the place of the new point matches the place of the previous point
            else if (x == x1)
                newPoint = new DataPoint(realx, series.getY(x1));

            // the new point is between the previous and the next point
            else {
                // calculating the y coordinate of the new point
                // calculating the equation of line, knowing two points it passes through:
                // A(x1,y1), B(x2,y2)
                // newY = (y2-y1)*(x-x1) + y1
                if (prevX1 != x1) {
                    y1 = series.getY(x1);
                    y2 = series.getY(x1 + 1);
                    deltaY = y2 - y1;
                    prevX1 = x1;
                }
                
                newPoint = new DataPoint(realx, deltaY * (x - x1) + y1);
                
            }

            result.add(newPoint);
            x += pointsDistance;
            realx += realDistance;
            ++index;
        }

        return result;
    }

    /**
     * Linearly scales an <b>equidistant</b> time series to the length
     * <code>desiredLength</code>, using linear interpolation. Assumes that series
     * are sorted by time (x axes) component. It returns <code>null</code> if the
     * time series contains less than two points.
     * 
     * @author Lidija Fodor, Zoltán Gellér
     * @version 2024.09.10.
     * 
     * @param series        the time series whose linearly equiscaled version is to
     *                      be returned
     * @param desiredLength the desired length of time series
     * @param keepx         indicates whether to preserve x coordinates of the
     *                      original time series
     */
    public static TimeSeries getLinearlyEquiscaled(TimeSeries series, int desiredLength, boolean keepx) {
        
        if (desiredLength < 2)
            throw new IllegalArgumentException("The desired length must be >= 2.");
        
        if (series.length() < 2)
            return null;
        
        else if (keepx)
            return getLinearlyEquiscaledWithX(series, desiredLength);
        else
            return getLinearlyEquiscaledWithoutX(series, desiredLength);

    }
    

}

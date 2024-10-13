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

package fap.distance;

import fap.core.data.TimeSeries;
import fap.distance.util.ItakuraParallelogram;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Itakura-constrained {@link DTWDistance DTW} (Dynamic Time Warping) distance
 * measure. Time series must be the same length.
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
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see AbstractConstrainedDistance
 * @see DTWDistance
 */
public class ItakuraDTWDistance extends AbstractConstrainedDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Auxiliary object for generating and storing Itakura parallelograms.
     */
    private ItakuraParallelogram itPara = new ItakuraParallelogram();

    /**
     * Constructs a new Itakura constrained DTW distance measure with the default
     * warping-widnow width ({@link AbstractConstrainedDistance#r r}).
     */
    public ItakuraDTWDistance() {
    }
    
    /**
     * Constructs a new Itakura constrained DTW distance measure with the default
     * warping-widnow width ({@link AbstractConstrainedDistance#r r}) and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraDTWDistance(boolean storing) {
        super(storing);
    }

    /**
     * Constructs a new Itakura constrained DTW distance measure with the specified
     * relative warping-window width.
     * 
     * @param r the relative width of the warping window (as a percentage of the
     *          length of the time series)
     */
    public ItakuraDTWDistance(double r) {
        super(r);
    }
    
    /**
     * Constructs a new Itakura constrained DTW distance measure with the specified
     * relative warping-window width and whether to store distances.
     * 
     * @param r       the relative width of the warping window (as a percentage of
     *                the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraDTWDistance(double r, boolean storing) {
        super(r, storing);
    }

    /**
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     */
    @Override
    public double distance(TimeSeries series1, TimeSeries series2) throws IncomparableTimeSeriesException  {

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

        double max = Double.POSITIVE_INFINITY;

        double curRow[] = new double[len + 1];
        double prevRow[] = new double[len + 1];

        // initialization
        prevRow[0] = 0;

        int prevEnd = 0;

        double tmp[];

        for (int i = 1; i <= len; i++) {

            int start = startj[i];
            int end = endj[i];

            // initializing left and right side
            
            curRow[start - 1] = max; // left side

            if (prevEnd < len)
                for (int t = prevEnd + 1; t <= end; t++) // right side
                    prevRow[t] = max;
            prevEnd = end;

            double y1 = series1.getY(i - 1);

            for (int j = start; j <= end; j++) {

                int jm1 = j - 1;
                double delta = y1 - series2.getY(jm1);

                curRow[j] = delta * delta + // Math.abs(delta) +
                        Math.min(prevRow[j], Math.min(prevRow[jm1], curRow[jm1]));

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
        ItakuraDTWDistance copy = new ItakuraDTWDistance();
        init(copy, deep);
        return copy;
    }

}

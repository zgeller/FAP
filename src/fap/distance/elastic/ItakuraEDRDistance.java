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

package fap.distance.elastic;

import fap.core.data.TimeSeries;
import fap.distance.util.ItakuraParallelogram;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Itakura-constrained {@link EDRDistance EDR} (Edit Distance on Real sequence)
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
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see AbstractConstrainedDistance
 * @see EDRDistance
 */
public class ItakuraEDRDistance extends AbstractConstrainedThresholdDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Auxiliary object for generating and storing Itakura parallelograms.
     */
    private ItakuraParallelogram itPara = new ItakuraParallelogram();

    /**
     * Constructs a new Itakura constrained EDR distance measure with the default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}).
     */
    public ItakuraEDRDistance() {
    }
    
    /**
     * Constructs a new Itakura constrained EDR distance measure with the default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}), and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraEDRDistance(boolean storing) {
        super(storing);
    }

    /**
     * Constructs a new Itakura constrained EDR distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}).
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series)
     */
    public ItakuraEDRDistance(double r) {
        super(r);
    }
    
    /**
     * Constructs a new Itakura constrained EDR distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}), and sets
     * whether to store distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraEDRDistance(double r, boolean storing) {
        super(r, storing);
    }
    
    /**
     * Constructs a new Itakura constrained EDR distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the matching
     * threshold value ({@code epsilon}).
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     */
    public ItakuraEDRDistance(double r, double epsilon) {
        super(r, epsilon);
    }
    
    /**
     * Constructs a new Itakura constrained EDR distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the matching
     * threshold value ({@code epsilon}), whether to store distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraEDRDistance(double r, double epsilon, boolean storing) {
        super(r, epsilon, storing);
    }

    /**
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     */
    @Override
    public double distance(TimeSeries series1, TimeSeries series2) {

        // try to recall the distance
        double distance = this.recall(series1, series2);
        if (!Double.isNaN(distance))
            return distance;
        
        int sei[][] = itPara.getSEI(series1, series2, getR(), getW()); 
                                                                  
        int len = series1.length();

        final long max = Long.MAX_VALUE - 1; // to prevent overflow

        int startj[] = sei[0];
        int endj[] = sei[1];

        double epsilon = getEpsilon();

        long curRow[] = new long[len + 1];
        long prevRow[] = new long[len + 1];

        // initialization
        prevRow[0] = 0;
        for (int i = 1; i <= len; i++)
            prevRow[i] = i;

        int prevEnd = 0;

        long tmp[];

        for (int i = 1; i <= len; i++) {

            int start = startj[i];
            int end = endj[i];

            // initializing left and right side
            
            curRow[start - 1] = (start - 1 == 0) ? i : max; // left side

            if (i > 1 && prevEnd < len)
                for (int t = prevEnd + 1; t <= end; t++) // right side
                    prevRow[t] = max;
            prevEnd = end;

            double y1 = series1.getY(i - 1);

            for (int j = start; j <= end; j++) {
                
                int jm1 = j - 1;

                int subcost = Math.abs(y1 - series2.getY(jm1)) <= epsilon ? 0 : 1;

                curRow[j] = Math.min(prevRow[jm1] + subcost, 1 + Math.min(prevRow[j], curRow[jm1]));
                
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
        ItakuraEDRDistance copy = new ItakuraEDRDistance();
        init(copy, deep);
        return copy;
    }

}

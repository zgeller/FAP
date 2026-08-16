/*   
 * Copyright 2024-2026 Zoltán Gellér
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

import fap.data.TimeSeries;
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
 * @version 2026.08.16.
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
     * Constructs a default Itakura constrained EDR distance measure.
     */
    public ItakuraEDRDistance() {
    }
    
    /**
     * Constructs a new Itakura constrained EDR distance measure, specifying whether
     * calculated distances should be stored in memory for reuse.
     * 
     * @param storing {@code true} if calculated distances should be stored in
     *                memory for reuse
     */
    public ItakuraEDRDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Constructs a new Itakura constrained EDR distance measure with a
     * specified time series length.
     * 
     * @param length the length of the time series
     */
    public ItakuraEDRDistance(int length) {
        super(length);
    }
    
    /**
     * Constructs a new Itakura constrained EDR distance measure with a
     * specified time series length and an indication of whether calculated
     * distances should be stored in memory for reuse.
     * 
     * @param storing {@code true} if calculated distances should be stored in
     *                memory for reuse
     * @param length  the length of the time series
     */
    public ItakuraEDRDistance(boolean storing, int length) {
        super(storing, length);
    }

    /**
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     */
    @Override
    public double distance(TimeSeries series1, TimeSeries series2) {

        // try to recall the distance
        Double recall = this.recall(series1, series2);
        if (recall != null)
            return recall;
        
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
        
        double distance = prevRow[len];

        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        ItakuraEDRDistance copy = new ItakuraEDRDistance(this.getLength());
        init(copy, deep);
        return copy;
    }

}

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
import fap.distance.util.ConstraintUtils;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Sakoe-Chiba constrained {@link EDRDistance EDR} (Edit Distance on Real sequence)
 * distance measure. Time series must be the same length.
 * 
 * <p>
 * Two data points are considered to match if their distance is not greater than
 * the {@link AbstractConstrainedThresholdDistance#epsilon matching threshold}.
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
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see AbstractConstrainedThresholdDistance
 * @see EDRDistance
 */
public class SakoeChibaEDRDistance extends AbstractConstrainedThresholdDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Sakoe-Chiba constrained EDR distance measure with the default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}).
     */
    public SakoeChibaEDRDistance() {
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained EDR distance measure with the default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}), and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaEDRDistance(boolean storing) {
        super(storing);
    }

    /**
     * Constructs a new Sakoe-Chiba constrained EDR distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}).
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series)
     */
    public SakoeChibaEDRDistance(double r) {
        super(r);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained EDR distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}), and sets
     * whether to store distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaEDRDistance(double r, boolean storing) {
        super(r, storing);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained EDR distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the matching
     * threshold value ({@code epsilon}).
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     */
    public SakoeChibaEDRDistance(double r, double epsilon) {
        super(r, epsilon);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained EDR distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the matching
     * threshold value ({@code epsilon}), whether to store distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaEDRDistance(double r, double epsilon, boolean storing) {
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
        int scWidth = ConstraintUtils.getWarpingWindowWidth(series1, series2, getR(), getW()); 

        int len = series1.length();

        final long max = Long.MAX_VALUE - 1; // to prevent overflow

        double epsilon = getEpsilon();

        long curRow[] = new long[len + 1];
        long prevRow[] = new long[len + 1];

        // initialization
        prevRow[0] = 0;
        for (int i = 1; i <= len; i++)
            prevRow[i] = i;

        long tmp[];

        for (int i = 1; i <= len; i++) {
            
            int start = Math.max(1, i - scWidth);
            int end = Math.min(len, i + scWidth);

            // initializing left and right side
            
            curRow[start - 1] = (start - 1 == 0) ? i : max; // left side

            if (i > 1 && i + scWidth <= len)
                prevRow[end] = max; // right side

            double y1 = series1.getY(i - 1);

            for (int j = start; j <= end; j++) {
                
                int subcost = Math.abs(y1 - series2.getY(j - 1)) <= epsilon ? 0 : 1;

                curRow[j] = Math.min(prevRow[j - 1] + subcost, 1 + Math.min(prevRow[j], curRow[j - 1]));
                
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
        SakoeChibaEDRDistance copy = new SakoeChibaEDRDistance();
        init(copy, deep);
        return copy;
    }

}

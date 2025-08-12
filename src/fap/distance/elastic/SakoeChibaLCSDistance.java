/*   
 * Copyright 2024-2025 Zoltán Gellér
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
 * Sakoe-Chiba constrained {@link LCSDistance LCS} (Longest Common Subsequence)
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
 * @version 2024.08.12.
 * @see AbstractConstrainedThresholdDistance
 * @see LCSDistance
 */
public class SakoeChibaLCSDistance extends AbstractConstrainedThresholdDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Sakoe-Chiba constrained LCS distance measure with the default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}).
     */
    public SakoeChibaLCSDistance() {
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained LCS distance measure with the default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}), and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaLCSDistance(boolean storing) {
        super(storing);
    }

    /**
     * Constructs a new Sakoe-Chiba constrained LCS distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}).
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series)
     */
    public SakoeChibaLCSDistance(double r) {
        super(r);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained LCS distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of the matching threshold
     * ({@link AbstractConstrainedThresholdDistance#epsilon epsilon}), and sets
     * whether to store distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaLCSDistance(double r, boolean storing) {
        super(r, storing);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained LCS distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the matching
     * threshold value ({@code epsilon}).
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     */
    public SakoeChibaLCSDistance(double r, double epsilon) {
        super(r, epsilon);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained LCS distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the matching
     * threshold value ({@code epsilon}), and sets whether to store distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaLCSDistance(double r, double epsilon, boolean storing) {
        super(r, epsilon, storing);
    }

    /**
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     */
    @Override
    public double distance(TimeSeries series1, TimeSeries series2) throws IncomparableTimeSeriesException {

        // try to recall the distance
        Double recall = this.recall(series1, series2);
        if (recall != null)
            return recall;

        // throws IncomparableTimeSeriesException if the time series are not the same
        // length
        int scWidth = ConstraintUtils.getWarpingWindowWidth(series1, series2, this.getR(), this.getW()); 

        int len = series1.length();

        long curRow[] = new long[len + 1];
        long prevRow[] = new long[len + 1];

        double epsilon = getEpsilon();

        long tmp[];

        // initialization - not necessary as arrays are initialized with default values (0.0d)
//        for (int i = 0; i <= len; i++)
//            prevRow[i] = 0;

        for (int i = 1; i <= len; i++) {

            int start = Math.max(1, i - scWidth);
            int end = Math.min(len, i + scWidth);

            // initializing left and right side
            
            // this is needed only when scWidth = 0
            curRow[start - 1] = prevRow[start - 1];  // left side

            // this is needed only when scWidth = 0
            if (i + scWidth <= len)
                prevRow[end] = prevRow[end - 1];    // right side

            double y1 = series1.getY(i - 1);

            for (int j = start; j <= end; j++) {
                
                double y2 = series2.getY(j - 1);

                if (Math.abs(y1 - y2) <= epsilon)
                    curRow[j] = 1 + prevRow[j - 1];
                else
                    curRow[j] = Math.max(prevRow[j], curRow[j - 1]);
                
            }

            tmp = curRow;
            curRow = prevRow;
            prevRow = tmp;

        }

        double distance = 0;
        if (len > 0)
            distance = (double) (len - prevRow[len]) / (double) len;
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        SakoeChibaLCSDistance copy = new SakoeChibaLCSDistance();
        init(copy, deep);
        return copy;
    }

}

/*   
 * Copyright 2025 Zoltán Gellér
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
import fap.exception.IncomparableTimeSeriesException;

/**
 * Hassanat distance measure. Time series must be the same length (n):
 *
 * <blockquote> <img src="doc-files/HassanatDistance-1.png"> </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> A.B.A. Hassanat, Dimensionality Invariant Similarity Measure, J. Am. Sci.
 *       10 (2014) 221–226. 
 *       <a href="https://doi.org/10.7537/marsjas100814.31">
 *          https://doi.org/10.7537/marsjas100814.31</a>.
 *  <li> H.A. Abu Alfeilat, A.B.A. Hassanat, O. Lasassmeh, A.S. Tarawneh, M.B.
 *       Alhasanat, H.S. Eyal Salman, V.B.S. Prasath, Effects of Distance Measure
 *       Choice on K-Nearest Neighbor Classifier Performance: A Review, Big Data. 7
 *       (2019) 221–248. 
 *       <a href="https://doi.org/10.1089/big.2018.0175">
 *          https://doi.org/10.1089/big.2018.0175</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.08.31.
 * @see AbstractCopyableDistance
 */
public class HassanatDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Hassanat distance measure.
     */
    public HassanatDistance() {
    }

    /**
     * Constructs a new Hassanat distance measure and sets whether to store
     * distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public HassanatDistance(boolean storing) {
        super(storing);
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
        
        int len = IncomparableTimeSeriesException.checkLength(series1, series2);

        double distance = 0;

        for (int i = 0; i < len; i++) {
            
            double y1 = series1.getY(i);
            double y2 = series2.getY(i);

            double min = y1;
            double max = y2;
            if (y2 < y1) {
                min = y2;
                max = y1;
            }
            
            if (min < 0)
                distance += 1 / (1 + max - min);
            else
                distance += (1 + min) / (1 + max);
            
        }
        
        distance = len - distance;
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;

    }

    @Override
    public Object makeACopy(boolean deep) {
        HassanatDistance copy = new HassanatDistance();
        init(copy, deep);
        return copy;
    }
    
}

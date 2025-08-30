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
import fap.util.MathUtils;

/**
 * Min symmetric chi-square distance measure. Time series must be the same length
 * (n) and they should be non-negative:
 * 
 * <blockquote> <img src="doc-files/MinSymmetricChiSquareDistance-1.png"> </blockquote>
 * 
 * <ul>
 *  <li> {@code 0/0} is treated as {@code 0} (see [1]).
 *  <li> Zero denominator is replaced by {@link MathUtils#getZeroDenominator()} 
 *       (see [1]).
 * </ul>
 * 
 * <p>
 * References:
 * <ol>
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
 * @author Zoltán Gellér
 * @version 2025.08.30.
 * @see AbstractCopyableDistance
 */
public class MinSymmetricChiSquareDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new min symmetric chi-square distance measure.
     */
    public MinSymmetricChiSquareDistance() {
    }

    /**
     * Constructs a new min symmetric chi-square distance measure and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public MinSymmetricChiSquareDistance(boolean storing) {
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
        double distance2 = 0;

        for (int i = 0; i < len; i++) {
            
            double y1 = series1.getY(i);
            double y2 = series2.getY(i);

            // 0/0 is treated as 0 (see [1])
            if ((y1 == 0 && y2 == 0) || y1 == y2)
                continue;

            double tmp = y1 - y2;
            tmp *= tmp;
            
            if (y1 != 0)
                distance += tmp / y1;
            else
                distance += tmp / MathUtils.getZeroDenominator();
            
            if (y2 != 0)
                distance2 += tmp / y2;
            else
                distance2 += tmp / MathUtils.getZeroDenominator();
            
        }
        
        if (distance2 < distance)
            distance = distance2;
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;

    }

    @Override
    public Object makeACopy(boolean deep) {
        MinSymmetricChiSquareDistance copy = new MinSymmetricChiSquareDistance();
        init(copy, deep);
        return copy;
    }
    
}

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
 * Kumar-Johnson distance measure. Time series must be the same length (n) and
 * they should be non-negative:
 *
 * <blockquote> <img src="doc-files/KumarJohnsonDistance-1.png"> </blockquote>
 * 
 * <ul>
 *  <li> {@code 0/0} is treated as {@code 0} (see [2]).
 *  <li> Zero denominator is replaced by {@link MathUtils#getZeroDenominator()} 
 *       (see [2]).
 * </ul>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> P. Kumar, A. Johnson, On A Symmetric Divergence Measure and Information
 *       Inequalities, J. Inequalities Pure Appl. Math. 6 (2005) 1–13.
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
 * @version 2025.09.01.
 * @see AbstractCopyableDistance
 */
public class KumarJohnsonDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Kumar-Johnson distance measure.
     */
    public KumarJohnsonDistance() {
    }

    /**
     * Constructs a new Kumar-Johnson distance measure and sets whether to store
     * distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public KumarJohnsonDistance(boolean storing) {
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

            double tmp = y1 * y1 - y2 * y2;

            // 0/0 is treated as 0 (see [2]) 
            if (tmp != 0)
                if (y1 == 0 || y2 == 0)
                    distance += tmp * tmp / MathUtils.getZeroDenominator();
                else
                    distance += tmp * tmp / Math.pow(y1 * y2, 3/2);
            
        }
        
        distance /= 2;
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;

    }

    @Override
    public Object makeACopy(boolean deep) {
        KumarJohnsonDistance copy = new KumarJohnsonDistance();
        init(copy, deep);
        return copy;
    }
    
}

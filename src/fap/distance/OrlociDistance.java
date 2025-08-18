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
 * Orloci (chord) distance measure. Time series must be the same length (n):
 * 
 * <blockquote> <img src="doc-files/OrlociDistance-1.png"> </blockquote>
 * 
 * <ul>
 *  <li> If at least one of the two time series contains only zeros, the distance
 *       between them will be {@code Double.NaN}.
 * </ul>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> L. Orlóci, An Agglomerative Method for Classification of Plant
 *       Communities, J. Ecol. 55 (1967) 193. 
 *       <a href="https://doi.org/10.2307/2257725">
 *          https://doi.org/10.2307/2257725</a>.
 *  <li> E. Deza, M.-M. Deza, Dictionary of Distances, Elsevier, 2006. 
 *       <a href="https://doi.org/10.1016/B978-0-444-52087-6.X5000-8">
 *          https://doi.org/10.1016/B978-0-444-52087-6.X5000-8</a>.
 *  <li> M.M. Deza, E. Deza, Encyclopedia of Distances, Springer Berlin
 *       Heidelberg, Berlin, Heidelberg, 2016. 
 *       <a href="https://doi.org/10.1007/978-3-662-52844-0">
 *          https://doi.org/10.1007/978-3-662-52844-0</a>.
 *  <li> G. Gan, C. Ma, J. Wu, Data Clustering: Theory, Algorithms, and
 *       Applications, Society for Industrial and Applied Mathematics, 2007. 
 *       <a href="https://doi.org/10.1137/1.9780898718348">
 *          https://doi.org/10.1137/1.9780898718348</a>.
 *  <li> M. Jiřina, S. Krayem, The Distance Function Optimization for the Near
 *       Neighbors-Based Classifiers, ACM Trans. Knowl. Discov. Data. 16 (2022) 1–21.
 *       <a href="https://doi.org/10.1145/3434769">
 *          https://doi.org/10.1145/3434769</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.08.18.
 * @see AbstractCopyableDistance
 */
public class OrlociDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new Orloci distance measure.
     */
    public OrlociDistance() {
    }

    /**
     * Constructs a new Orloci distance measure and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public OrlociDistance(boolean storing) {
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
        
        double sumab = 0;
        double suma = 0;
        double sumb = 0;
        
        for (int i = 0; i < len; i++) {

            double y1 = series1.getY(i);
            double y2 = series2.getY(i);

            sumab += y1 * y2;
            suma += y1 * y1;
            sumb += y2 * y2;
            
        }

        double denominator = Math.sqrt(suma) * Math.sqrt(sumb);
        
        double distance;
        if (denominator == 0 )
            distance = Double.NaN;
        else
            distance = Math.sqrt(2 * (1 - sumab / denominator));
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        OrlociDistance copy = new OrlociDistance();
        init(copy, deep);
        return copy;
    }
    
}

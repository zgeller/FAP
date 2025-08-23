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
 * Angular distance measure. Time series must be the same length (n):
 * 
 * <blockquote> <img src="doc-files/AngularDistance-1.png"> </blockquote>
 * 
 * <ul>
 * <li>If at least one of the two time series contains only zeros, the distance
 * between them will be {@code Double.NaN}.
 * </ul>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> A. Levy, B.R. Shalom, M. Chalamish, A guide to similarity measures and
 *       their data science applications, J. Big Data. 12 (2025) 188.
 *       <a href="https://doi.org/10.1186/s40537-025-01227-1">
 *          https://doi.org/10.1186/s40537-025-01227-1</a>.
 *  <li> A. Vanacore, M.S. Pellegrino, Y.N. Marmor, E. Bashkansky, Analysis of
 *       consumer preferences expressed by prioritization chains, Qual. Reliab. Eng.
 *       Int. 35 (2019) 1424–1435. 
 *       <a href="https://doi.org/10.1002/qre.2530">
 *          https://doi.org/10.1002/qre.2530</a>.
 *  <li> H. Xiao, T. Chanwimalueang, D.P. Mandic, Multivariate Multiscale Cosine
 *       Similarity Entropy, in: ICASSP 2022 - 2022 IEEE Int. Conf. Acoust. Speech
 *       Signal Process., IEEE, 2022: pp. 5997–6001. 
 *       <a href="https://doi.org/10.1109/ICASSP43922.2022.9747282">
 *          https://doi.org/10.1109/ICASSP43922.2022.9747282</a>.
 *  <li> T. Chanwimalueang, D. Mandic, Cosine Similarity Entropy:
 *       Self-Correlation-Based Complexity Analysis of Dynamical Systems, Entropy. 19
 *       (2017) 652. 
 *       <a href="https://doi.org/10.3390/e19120652">
 *          https://doi.org/10.3390/e19120652</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.08.23.
 * @see AbstractCopyableDistance
 */
public class AngularDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Angular distance measure.
     */
    public AngularDistance() {
    }

    /**
     * Constructs a new Angular distance measure and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public AngularDistance(boolean storing) {
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

        double distance;
        
        if (suma == 0 || sumb == 0)
            distance = Double.NaN;
        
        // Math.sqrt is just an approximation of the true mathematical square root
        else if (sumab == suma && sumab == sumb)
            distance = 0;
        
        else
            distance = Math.acos(sumab / (Math.sqrt(suma) * Math.sqrt(sumb))) / Math.PI;
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;
        
    }

    @Override
    public Object makeACopy(boolean deep) {
        AngularDistance copy = new AngularDistance();
        init(copy, deep);
        return copy;
    }
    
}

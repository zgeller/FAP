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
 * Meehl distance measure. Time series must be the same length (n) and they must
 * contain at least two data points:
 * 
 * <blockquote> <img src="doc-files/MeehlDistance-1.png"> </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> H. Eidenberger, Distance measures for MPEG-7-based retrieval, in: Proc.
 *       5th ACM SIGMM Int. Work. Multimed. Inf. Retr. - MIR ’03, ACM Press, New York,
 *       New York, USA, 2003: pp. 130–137. 
 *       <a href="https://doi.org/10.1145/973264.973286">
 *          https://doi.org/10.1145/973264.973286</a>.
 *  <li> M.M. Deza, E. Deza, Encyclopedia of Distances, Springer Berlin
 *       Heidelberg, Berlin, Heidelberg, 2016.
 *       <a href="https://doi.org/10.1007/978-3-662-52844-0">
 *          https://doi.org/10.1007/978-3-662-52844-0</a>.
 *  <li> H.A. Abu Alfeilat, A.B.A. Hassanat, O. Lasassmeh, A.S. Tarawneh, M.B.
 *       Alhasanat, H.S. Eyal Salman, V.B.S. Prasath, Effects of Distance Measure
 *       Choice on K-Nearest Neighbor Classifier Performance: A Review, Big Data. 7
 *       (2019) 221–248. 
 *       <a href="https://doi.org/10.1089/big.2018.0175">
 *          https://doi.org/10.1089/big.2018.0175</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.08.26.
 * @see AbstractCopyableDistance
 */
public class MeehlDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Meehl distance measure.
     */
    public MeehlDistance() {
    }

    /**
     * Constructs a new Meehl distance measure and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public MeehlDistance(boolean storing) {
        super(storing);
    }


    /**
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length or contain less than two data
     *                                         points
     */
    @Override
    public double distance(TimeSeries series1, TimeSeries series2) {

        // try to recall the distance
        Double recall = this.recall(series1, series2);
        if (recall != null)
            return recall;
        
        int len = IncomparableTimeSeriesException.checkLength(series1, series2);
        
        if (len < 2)
            throw new IncomparableTimeSeriesException("Time series must contain at least two data points.");

        double distance = 0;
        
        double prevY1 = series1.getY(0);
        double prevY2 = series2.getY(0);

        for (int i = 1; i < len; i++) {
            
            double y1 = series1.getY(i);
            double y2 = series2.getY(i);
            
            double tmp = prevY1 - prevY2 - y1 + y2;
            
            distance += tmp * tmp;
            
            prevY1 = y1;
            prevY2 = y2;

        }
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;

    }

    @Override
    public Object makeACopy(boolean deep) {
        MeehlDistance copy = new MeehlDistance();
        init(copy, deep);
        return copy;
    }
    
}

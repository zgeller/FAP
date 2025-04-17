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
import fap.distance.AbstractThresholdDistance;

/**
 * EDR (Edit Distance on Real sequence) distance measure.
 * 
 * Two data points are considered to match if their distance is not greater than
 * the {@link AbstractThresholdDistance#epsilon matching threshold}.
 * 
 * <p>
 * Let <code>A = (a<sub>1</sub>, a<sub>2</sub>, …, a<sub>n</sub>)</code> and
 * <code>B = (b<sub>1</sub>, b<sub>2</sub>, …, b<sub>m</sub>)</code> be two time
 * series. Then:
 * 
 * <blockquote> <img src="doc-files/EDRDistance-1.png"> </blockquote>
 * 
 * where
 * 
 * <blockquote> <img src="doc-files/EDRDistance-2.png"> </blockquote>
 * 
 * and
 * 
 * <blockquote> <img src="doc-files/EDRDistance-3.png"> </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> L. Chen, M.T. Özsu, V. Oria, Robust and fast similarity search for
 *       moving object trajectories, in: Proc. 2005 ACM SIGMOD Int. Conf. Manag. Data
 * -     SIGMOD ’05, ACM Press, New York, New York, USA, 2005: pp. 491–502. 
 *       <a href="https://doi.org/10.1145/1066157.1066213">
 *          https://doi.org/10.1145/1066157.1066213</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see AbstractThresholdDistance
 */
public class EDRDistance extends AbstractThresholdDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new EDR distance measure with the default value of the matching threshold
     * ({@link AbstractThresholdDistance#epsilon}).
     */
    public EDRDistance() {
    }

    /**
     * Constructs a new EDR distance measure with the default value of the
     * matching threshold ({@link AbstractThresholdDistance#epsilon}) and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public EDRDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Constructs a new EDR distance measure with the specified matching threshold
     * value ({@code epsilon}).
     * 
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     */
    public EDRDistance(double epsilon) {
        setEpsilon(epsilon);
    }
    
    /**
     * Constructs a new EDR distance measure with the specified matching threshold
     * value ({@code epsilon}) and sets whether to store distances.
     * 
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     * @param storing {@code true} if storing distances should be enabled
     */
    public EDRDistance(double epsilon, boolean storing) {
        super(epsilon, storing);
    }

    @Override
    public double distance(TimeSeries series1, TimeSeries series2) {

        // try to recall the distance
        double distance = this.recall(series1, series2);
        if (!Double.isNaN(distance))
            return distance;
        
        TimeSeries sdata, gdata;

        int len1 = series1.length();
        int len2 = series2.length();

        int slen, glen;

        if (len1 < len2) {
            
            slen = len1;
            glen = len2;
            sdata = series1;
            gdata = series2;
            
        } else {
            
            slen = len2;
            glen = len1;
            sdata = series2;
            gdata = series1;
            
        }

        double epsilon = getEpsilon();

        long curRow[] = new long[slen + 1];
        long prevRow[] = new long[slen + 1];

        // initialization
        prevRow[0] = 0;
        for (int i = 1; i <= slen; i++)
            prevRow[i] = i;

        long tmp[];

        for (int i = 1; i <= glen; i++) {
            
            curRow[0] = i;

            double y1 = gdata.getY(i - 1);

            for (int j = 1; j <= slen; j++) {
                
                int subcost = Math.abs(y1 - sdata.getY(j - 1)) <= epsilon ? 0 : 1;

                curRow[j] = Math.min(prevRow[j - 1] + subcost, 1 + Math.min(prevRow[j], curRow[j - 1]));
                
            }

            tmp = curRow;
            curRow = prevRow;
            prevRow = tmp;
        }

        distance = prevRow[slen];
        
        // save the distance into the memory
        this.store(series1, series2, distance);

        return distance;

    }

    @Override
    public Object makeACopy(boolean deep) {
        EDRDistance copy = new EDRDistance();
        init(copy, deep);
        return copy;
    }

}

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
import fap.distance.AbstractCopyableDistance;

/**
 * ERP (Edit distance with Real Penalty) distance measure.
 * 
 * <p>
 * Let <code>A = (a<sub>1</sub>, a<sub>2</sub>, …, a<sub>n</sub>)</code> and
 * <code>B = (b<sub>1</sub>, b<sub>2</sub>, …, b<sub>m</sub>)</code> be two time
 * series. Then:
 * 
 * <blockquote> <img src="doc-files/ERPDistance-1.png"> </blockquote>
 * 
 * where {@link #g} is a constant value used to calculate the penalty for gaps,
 * and
 * 
 * <blockquote> <img src="doc-files/ERPDistance-2.png"> </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> L. Chen, R. Ng, On The Marriage of Lp-norms and Edit Distance, in: M.A.
 *       Nascimento, M.T. Özsu, D. Kossmann, R.J. Miller, J.A. Blakeley, K.B. Schiefer
 *       (Eds.), Proc. 2004 VLDB Conf., Elsevier, 2004: pp. 792–803.
 *       <a href="https://doi.org/10.1016/B978-012088469-8.50070-X">
 *          https://doi.org/10.1016/B978-012088469-8.50070-X</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.08.12.
 * @see AbstractCopyableDistance
 * @see ERPParameters
 */
public class ERPDistance extends AbstractCopyableDistance implements ERPParameters {

    private static final long serialVersionUID = 1L;

    /**
     * A constant value used to calculate the penalty for gaps. Default value is
     * {@code 0}.
     */
    private double g = 0;

    /**
     * Constructs a new ERP distance measure with the default value of {@link #g}.
     */
    public ERPDistance() {
    }
    
    /**
     * Constructs a new ERP distance measure with the default value of {@link #g}
     * and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public ERPDistance(boolean storing) {
        super(storing);
    }

    /**
     * Constructs a new ERP distance measure with the specified value of {@link #g}.
     * 
     * @param g the value that is to be used to calculate the penalty for gaps
     */
    public ERPDistance(double g) {
        this.setG(g);
    }
    
    /**
     * Constructs a new ERP distance measure with the specified value of {@link #g}
     * and sets whether to store distances.
     * 
     * @param g       the value that is to be used to calculate the penalty for gaps
     * @param storing {@code true} if storing distances should be enabled
     */
    public ERPDistance(double g, boolean storing) {
        super(storing);
        this.setG(g);
    }

    @Override
    public double getG() {
        return g;
    }

    @Override
    public void setG(double g) {
        
        if (this.g != g) {
            this.clearStorage();
            this.g = g;
        }
        
    }
    
    @Override
    public double distance(TimeSeries series1, TimeSeries series2) {

        // try to recall the distance
        Double recall = this.recall(series1, series2);
        if (recall != null)
            return recall;

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
        
        double g = this.getG();

        double curRow[] = new double[slen + 1];
        double prevRow[] = new double[slen + 1];

        // initialization
        prevRow[0] = 0;
        for (int i = 0; i < slen; i++)
            prevRow[i + 1] = prevRow[i] + Math.abs(sdata.getY(i) - g);

        double tmp[];

        for (int i = 0; i < glen; i++) {
            
            double y1 = gdata.getY(i);
            double absy1 = Math.abs(y1 - g);

            curRow[0] = prevRow[0] + absy1;

            for (int j = 1; j <= slen; j++) {
                
                double y2 = sdata.getY(j - 1);

                double E1 = prevRow[j - 1] + Math.abs(y1 - y2);
                double E2 = Math.min(E1, curRow[j - 1] + Math.abs(y2 - g));
                curRow[j] = Math.min(E2, prevRow[j] + absy1);
                
            }

            tmp = curRow;
            curRow = prevRow;
            prevRow = tmp;
            
        }

        double distance = prevRow[slen];
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;

    }
    
    /**
     * Initializes the specified distance measure with the common data structures of this
     * distance.
     * 
     * @param copy the distance measure whose data structures is to be initialized
     */
    protected void init(ERPDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setG(this.getG());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        ERPDistance copy = new ERPDistance();
        init(copy, deep);
        return copy;
    }

    @Override
    public String toString() {
        return super.toString() + ", g=" + getG();
    }

}

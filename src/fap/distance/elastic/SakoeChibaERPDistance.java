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
 * Sakoe-Chiba constrained {@link ERPDistance ERP} (Edit distance with Real
 * Penalty) distance measure. Time series must be the same length.
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
 * @version 2024.09.24.
 * @see AbstractConstrainedDistance
 * @see ERPParameters
 * @see ERPDistance
 */
public class SakoeChibaERPDistance extends AbstractConstrainedDistance implements ERPParameters {

    private static final long serialVersionUID = 1L;

    /**
     * A constant value used to calculate the penalty for gaps. Default value is
     * {@code 0}.
     */
    private double g = 0;
    
    /**
     * Constructs a new Sakoe-Chiba constrained ERP distance measure with the
     * default width of the warping (editing) window
     * ({@link AbstractConstrainedDistance#r r}) and the default value of
     * {@link #g}.
     */
    public SakoeChibaERPDistance() {
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained ERP distance measure with the, default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of {@link #g}, and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaERPDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained ERP distance measure with the
     * specified relative width of the warping (editing) window ({@code r}) and the
     * default value of {@link #g}.
     * 
     * @param r the relative width of the warping window (as a percentage of the
     *          length of the time series)
     */
    public SakoeChibaERPDistance(double r) {
        super(r);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained ERP distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of {@link #g}, and sets whether to store distances.
     * 
     * @param r       the relative width of the warping window (as a percentage of
     *                the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaERPDistance(double r, boolean storing) {
        super(r, storing);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained ERP distance measure with the
     * specified relative width of the warping (editing) window ({@code r}) and the
     * value of {@link #g}.
     * 
     * @param r the relative width of the warping window (as a percentage of the
     *          length of the time series)
     * @param g the value that is to be used to calculate the penalty for gaps
     */
    public SakoeChibaERPDistance(double r, double g) {
        super(r);
        this.setG(g);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained ERP distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the value of
     * {@link #g}, and sets whether to store distances.
     * 
     * @param r       the relative width of the warping window (as a percentage of
     *                the length of the time series)
     * @param g       the value that is to be used to calculate the penalty for gaps
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaERPDistance(double r, double g, boolean storing) {
        super(r, storing);
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
        int scWidth = ConstraintUtils.getWarpingWindowWidth(series1, series2, this.getR(), this.getW()); 

        int len = series1.length();
        
        double g = this.getG();

        double curRow[] = new double[len + 1];
        double prevRow[] = new double[len + 1];

        // initialization
        prevRow[0] = 0;
        for (int i = 0; i < len; i++)
            prevRow[i + 1] = prevRow[i] + Math.abs(series2.getY(i) - g);

        double tmp[];

        for (int i = 1; i <= len; i++) {

            int start = Math.max(1, i - scWidth);
            int end = Math.min(len, i + scWidth);

            double y1 = series1.getY(i - 1);
            double absy1 = Math.abs(y1 - g);

            curRow[start - 1] = (start - 1 == 0) ? prevRow[0] + absy1 : Double.POSITIVE_INFINITY;
            if (i > 1 && i + scWidth <= len)
                prevRow[end] = Double.POSITIVE_INFINITY;

            for (int j = start; j <= end; j++) {
                
                double y2 = series2.getY(j - 1);

                double E1 = prevRow[j - 1] + Math.abs(y1 - y2);
                double E2 = Math.min(E1, curRow[j - 1] + Math.abs(y2) - g);
                curRow[j] = Math.min(E2, prevRow[j] + absy1);
                
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
    
    /**
     * Initializes the specified distance measure with the common data structures of this
     * distance measure.
     * 
     * @param copy the distance measure whose data structures is to be initialized
     */
    protected void init(SakoeChibaERPDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setG(this.getG());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        SakoeChibaERPDistance copy = new SakoeChibaERPDistance();
        init(copy, deep);
        return copy;
    }

    @Override
    public String toString() {
        return super.toString() + ", g=" + getG();
    }
    
}

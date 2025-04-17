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

import fap.core.data.DataPoint;
import fap.core.data.TimeSeries;
import fap.distance.util.ConstraintUtils;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Sakoe-Chiba constrained {@link TWEDDistance TWED} (Time Warp Edit Distance)
 * distance measure. Time series must be the same length.
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
 * @see TWEDParameters
 * @see TWEDDistance
 */
public class SakoeChibaTWEDDistance extends AbstractConstrainedDistance implements TWEDParameters {

    private static final long serialVersionUID = 1L;

    /**
     * A constant value that controls the stiffness of the measure. Default value is
     * {@code 1}.
     */
    private double nu = 1;

    /**
     * A constant value used to calculate penalties for insert and delete
     * operations. Default value is {@code 0}.
     */
    private double lambda = 0;

    /**
     * Constructs a new Sakoe-Chiba constrained TWED distance measure with the default
     * warping-widnow width ({@link AbstractConstrainedDistance#r r}) and the
     * default values of the parameters {@link #nu} and {@link #lambda}.
     */
    public SakoeChibaTWEDDistance() {
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained TWED distance measure, with the default
     * warping-widnow width ({@link AbstractConstrainedDistance#r r}) and the
     * default values of the parameters {@link #nu} and {@link #lambda}, and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaTWEDDistance(boolean storing) {
        super(storing);
    }

    /**
     * Constructs a new Sakoe-Chiba constrained TWED distance measure with the specified
     * relative warping-window width and the default values of the parameters
     * {@link #nu} and {@link #lambda}.
     * 
     * @param r the relative width of the warping window (as a percentage of the
     *          length of the time series)
     */
    public SakoeChibaTWEDDistance(double r) {
        super(r);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained TWED distance measure with the specified
     * relative warping-window width and the default values of the parameters
     * {@link #nu} and {@link #lambda}, and sets whether to store distances.
     * 
     * @param r       the relative width of the warping window (as a percentage of
     *                the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaTWEDDistance(double r, boolean storing) {
        super(r, storing);
    }

    /**
     * Constructs a new Sakoe-Chiba constrained TWED distance measure with the specified
     * relative warping-window width and the specified values of {@link #nu} and
     * {@link #lambda}.
     * 
     * @param r      the relative width of the warping window (as a percentage of
     *               the length of the time series)
     * @param nu     the value that is to be used to control the stiffness of the
     *               measure
     * @param lambda the value used to calculate penalties for insert and delete
     *               operations
     */
    public SakoeChibaTWEDDistance(double r, double nu, double lambda) {
        super(r);
        this.setNu(nu);
        this.setLambda(lambda);
    }
    
    /**
     * Constructs a new Sakoe-Chiba constrained TWED distance measure with the specified
     * relative warping-window width and the specified values of {@link #nu} and
     * {@link #lambda}, and sets whether to store distances.
     * 
     * @param r       the relative width of the warping window (as a percentage of
     *                the length of the time series)
     * @param nu      the value that is to be used to control the stiffness of the
     *                measure
     * @param lambda  the value used to calculate penalties for insert and delete
     *                operations
     * @param storing {@code true} if storing distances should be enabled
     */
    public SakoeChibaTWEDDistance(double r, double nu, double lambda, boolean storing) {
        super(r, storing);
        this.setNu(nu);
        this.setLambda(lambda);
    }

    @Override
    public double getNu() {
        return nu;
    }

    /**
     * @throws IllegalArgumentException if {@code nu < 0}
     */
    @Override
    public void setNu(double nu) throws IllegalArgumentException {
        
        if (nu < 0)
            throw new IllegalArgumentException("Must be nu >= 0.");
        
        if (this.nu != nu) {
            this.clearStorage();
            this.nu = nu;
        }
        
    }

    @Override
    public double getLambda() {
        return lambda;
    }

    /**
     * @throws IllegalArgumentException if {@code lambda < 0}
     */
    @Override
    public void setLambda(double lambda) throws IllegalArgumentException {
        
        if (lambda < 0)
            throw new IllegalArgumentException("Must be lambda >= 0.");
        
        if (this.lambda != lambda) {
            this.clearStorage();
            this.lambda = lambda;
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
        int scWidth = ConstraintUtils.getWarpingWindowWidth(series1, series2, getR(), getW());

        int len = series1.length();
        
        double nu = this.getNu();
        double lambda = this.getLambda();

        // dynamic programming structures
        double curRow[] = new double[len + 1]; // current row of the matrix
        double prevRow[] = new double[len + 1]; // previous row of the matrix

        // auxiliary arrays
        double delta1[] = new double[len]; // auxiliary array for data1
        double delta2[] = new double[len]; // auxiliary array for data2

        DataPoint dp;

        // there is no previous point for data1: (0,0) by definition
        double prevy1 = 0; // value
        double prevx1 = 0; // time
        
        // there is no previous point for data2: (0,0) by definition
        double prevy2 = 0; // value
        double prevx2 = 0; // time

        // matrix and array initialization
        prevRow[0] = 0;
        for (int i = 0; i < len; i++) {
            
            // matrix
            prevRow[i + 1] = Double.POSITIVE_INFINITY;

            // delta1 auxiliary array
            dp = series1.get(i);
            double y = dp.getY();
            double x = dp.getX();
            delta1[i] = Math.abs(y - prevy1) + nu * Math.abs(x - prevx1) + lambda;
            prevy1 = y;
            prevx1 = x;

            // delta2 auxiliary array
            dp = series2.get(i);
            y = dp.getY();
            x = dp.getX();
            delta2[i] = Math.abs(y - prevy2) + nu * Math.abs(x - prevx2) + lambda;
            prevy2 = y;
            prevx2 = x;
            
        }

        double tmp[];

        // there is no previous point for data1: (0,0) by definition
        prevy1 = 0; // value
        prevx1 = 0; // time

        for (int i = 1; i <= len; i++) // i-th row
        {
            int start = Math.max(1, i - scWidth); // starting column
            int end = Math.min(len, i + scWidth); // ending column

            // initializing left and right side
            curRow[start - 1] = Double.POSITIVE_INFINITY; // left side
            if (i + scWidth <= len)
                prevRow[end] = Double.POSITIVE_INFINITY; // right side

            dp = series1.get(i - 1); // current point of data1
            double y1 = dp.getY(); // value
            double x1 = dp.getX(); // time

            // initialization
            if (start > 1) { // previous point exists for data2
                
                dp = series2.get(start - 2);
                prevy2 = dp.getY(); // value
                prevx2 = dp.getX(); // time
                
            } else { // there is no previous point for data2: (0,0) by definition
                
                prevy2 = 0; // value
                prevx2 = 0; // time
                
            }

            double delta1i = delta1[i - 1]; // optimization

            for (int j = start - 1; j < end; j++)  { // j-th column
                
                dp = series2.get(j); // current point of data2
                double y2 = dp.getY(); // value
                double x2 = dp.getX(); // time

                double T1 = prevRow[j + 1] + delta1i;
                double T2 = Math.min(T1, curRow[j] + delta2[j]);
                curRow[j + 1] = Math.min(T2, 
                                         prevRow[j] + Math.abs(y1 - y2) + Math.abs(prevy1 - prevy2)
                                             + nu * (Math.abs(x1 - x2) + Math.abs(prevx1 - prevx2))
                                        );

                prevy2 = y2;
                prevx2 = x2;
                
            }

            prevy1 = y1;
            prevx1 = x1;

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
    protected void init(SakoeChibaTWEDDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setNu(this.getNu());
        copy.setLambda(this.getLambda());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        SakoeChibaTWEDDistance copy = new SakoeChibaTWEDDistance();
        init(copy, deep);
        return copy;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", nu=" + nu + ", lambda=" + lambda;
    }

}

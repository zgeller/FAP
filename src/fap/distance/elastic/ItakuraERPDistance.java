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
import fap.distance.util.ItakuraParallelogram;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Itakura-constrained {@link ERPDistance ERP} (Edit distance with Real
 * Penalty) distance measure. Time series must be the same length.
 * 
 * <p>
 * References:
 * <ol>
 * <li> F. Itakura, Minimum prediction residual principle applied to speech
 *      recognition, IEEE Trans. Acoust. 23 (1975) 67–72. 
 *      <a href="https://doi.org/10.1109/TASSP.1975.1162641">
 *         https://doi.org/10.1109/TASSP.1975.1162641</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.08.12.
 * @see AbstractConstrainedDistance
 * @see ERPParameters
 * @see ERPDistance
 */
public class ItakuraERPDistance extends AbstractConstrainedDistance implements ERPParameters {

	private static final long serialVersionUID = 1L;

    /**
     * A constant value used to calculate the penalty for gaps. Default value is
     * {@code 0}.
     */
    private double g = 0;
	
	/**
	 * Auxiliary object for generating and storing Itakura parallelograms.
	 */
	private ItakuraParallelogram itPara = new ItakuraParallelogram();

    /**
     * Constructs a new Itakura constrained ERP distance measure with the
     * default width of the warping (editing) window
     * ({@link AbstractConstrainedDistance#r r}) and the default value of
     * {@link #g}.
     */
	public ItakuraERPDistance() {
	}
	
    /**
     * Constructs a new Itakura constrained ERP distance measure with the, default
     * width of the warping (editing) window ({@link AbstractConstrainedDistance#r
     * r}) and the default value of {@link #g}, and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
	public ItakuraERPDistance(boolean storing) {
	    super(storing);
	}
	
    /**
     * Constructs a new Itakura constrained ERP distance measure with the
     * specified relative width of the warping (editing) window ({@code r}) and the
     * default value of {@link #g}.
     * 
     * @param r the relative width of the warping window (as a percentage of the
     *          length of the time series)
     */
	public ItakuraERPDistance(double r) {
	    super(r);
	}
	
    /**
     * Constructs a new Itakura constrained ERP distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the default
     * value of {@link #g}, and sets whether to store distances.
     * 
     * @param r       the relative width of the warping window (as a percentage of
     *                the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
	public ItakuraERPDistance(double r, boolean storing) {
	    super(r, storing);
	}
	
    /**
     * Constructs a new Itakura constrained ERP distance measure with the
     * specified relative width of the warping (editing) window ({@code r}) and the
     * value of {@link #g}.
     * 
     * @param r the relative width of the warping window (as a percentage of the
     *          length of the time series)
     * @param g the value that is to be used to calculate the penalty for gaps
     */
    public ItakuraERPDistance(int r, double g) {
        super(r);
        this.setG(g);
    }
    
    /**
     * Constructs a new Itakura constrained ERP distance measure with the specified
     * relative width of the warping (editing) window ({@code r}) and the value of
     * {@link #g}, and sets whether to store distances.
     * 
     * @param r       the relative width of the warping window (as a percentage of
     *                the length of the time series)
     * @param g       the value that is to be used to calculate the penalty for gaps
     * @param storing {@code true} if storing distances should be enabled
     */
    public ItakuraERPDistance(int r, double g, boolean storing) {
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
        Double recall = this.recall(series1, series2);
        if (recall != null)
            return recall;

        // throws IncomparableTimeSeriesException if the time series are not the same
        // length
        int sei[][] = itPara.getSEI(series1, series2, getR(), getW()); 
                                                                   
        int len = series1.length();

        int startj[] = sei[0];
        int endj[] = sei[1];

        double curRow[] = new double[len + 1];
        double prevRow[] = new double[len + 1];

        // initialization
        prevRow[0] = 0;
        for (int i = 0; i < len; i++) // !! necessary !!
            prevRow[i + 1] = prevRow[i] + Math.abs(series2.getY(i));

        int prevEnd = len;

        double tmp[];

        for (int i = 1; i <= len; i++) {

            int start = startj[i];
            int end = endj[i];

            double y1 = series1.getY(i - 1);
            double absy1 = Math.abs(y1);

            // initializing left and right side
            
            curRow[start - 1] = (start - 1 == 0) ? prevRow[0] + absy1 : Double.POSITIVE_INFINITY; // left side

            if (i > 0 && prevEnd < len)
                for (int t = prevEnd + 1; t <= end; t++) // right side
                    prevRow[t] = Double.POSITIVE_INFINITY;
            prevEnd = end;

            for (int j = start; j <= end; j++) {
                
                int jm1 = j - 1;
                double y2 = series2.getY(jm1);

                double E1 = prevRow[jm1] + Math.abs(y1 - y2);
                double E2 = Math.min(E1, curRow[jm1] + Math.abs(y2));
                curRow[j] = Math.min(E2, prevRow[j] + absy1);
                
            }

            tmp = curRow;
            curRow = prevRow;
            prevRow = tmp;
            
        }
        
        double distance = prevRow[len];
        
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
    protected void init(ItakuraERPDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setG(this.getG());
    }
	
    @Override
    public Object makeACopy(boolean deep) {
        ItakuraERPDistance copy = new ItakuraERPDistance();
        init(copy, deep);
        return copy;
    }

}

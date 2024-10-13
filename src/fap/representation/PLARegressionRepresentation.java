/*   
 * Copyright 2024 Miklós Kálózi, Zoltán Gellér, Brankica Bratić
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

package fap.representation;

import java.util.ArrayList;
import java.util.List;

import fap.core.data.TimeSeries;
import fap.core.data.Representation;
import fap.util.ExplicitLine;
import fap.util.MathUtils;

/**
 * Piecewise Linear Approximation (PLA) with linearly regressed line segments
 * <p>
 * Representation of time series with piecewise linear segments.
 * <p>
 * Implements three algorithms:<br>
 * <ul>
 * <li>sliding window</li>
 * <li>top-down</li>
 * <li>bottom-up</li>
 * </ul>
 * with linear regression.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> E. Keogh, S. Chu, D. Hart, M. Pazzani, Segmenting Time Series: A Survey
 *       and Novel Approach, in: 2004: pp. 1–21. 
 *       <a href="https://doi.org/10.1142/9789812565402_0001">
 *          https://doi.org/10.1142/9789812565402_0001</a>
 * </ol>
 * 
 * @author Miklós Kálózi, Zoltán Gellér, Brankica Bratić
 * @version 2024.09.14.
 * @see Representation
 */
public class PLARegressionRepresentation extends AbstractPLARepresentation<PLARegressionRepresentation.PLASegment> {

    private static final long serialVersionUID = 1L;
    
    /**
     * The Piecewise Linear Approximation (PLA) representation. Line segments are
     * created with linear regression. Each element of this list represents one
     * line. Default value of this array is null.
     */
    private ArrayList<PLASegment> pla;

    /**
     * Creates a new {@code PLA} representation.
     * 
     * @param series    time series
     * @param maxError  maximum error bound (must be non-negative)
     * @param algorithm type of the algorithm
     */
    public PLARegressionRepresentation(final TimeSeries series, double maxError, int algorithm) {
        super(series, maxError, algorithm);
    }

    /**
     * Creates a new {@code PLA} representation.
     * 
     * @param values    time series values
     * @param maxError  maximum error bound (must be non-negative)
     * @param algorithm type of the algorithm
     */
    public PLARegressionRepresentation(final double[] values, double maxError, int algorithm) {
        super(values, maxError, algorithm);
    }

    /**
     * Creates a new {@code PLA} representation for parameters given in string
     * {@code argsStr}.
     * 
     * @param argsStr arguments for PLA instantiation
     * @throws IOException
     */
    /*
     * public PLARegressionRepresentation(String argsStr) throws IOException { super(argsStr); }
     */

    /**
     * Class that stores information about one segment of PLA representation.
     * 
     * @author Brankica Bratic
     * @version 2017.15.03.
     */
    public static class PLASegment {

        /**
         * Line that represents current segment.
         */
        private ExplicitLine line;

        /**
         * The index of the right end point of this segment.
         */
        private int rIndex;

        public PLASegment(ExplicitLine line, int rIndex) {
            this.line = line;
            this.rIndex = rIndex;
        }

        /**
         * Returns line of this segment.
         * 
         * @return line of this segment
         */
        public ExplicitLine getLine() {
            return line;
        }

        /**
         * Sets the line of this segment.
         * 
         * @param line line of this segment
         */
        public void setLine(ExplicitLine line) {
            this.line = line;
        }

        /**
         * Returns the index of the right end point of this segment.
         * 
         * @return the index of the right end point of this segment
         */
        public int getrIndex() {
            return rIndex;
        }

        /**
         * Sets the index of the right end point of this segment.
         * 
         * @param rIndex the index of the right end point of this segment
         */
        public void setrIndex(int rIndex) {
            this.rIndex = rIndex;
        }
    }

    /**
     * Returns the {@code PLA} representation in which line segments are created by
     * using linear regression.
     * 
     * @return the {@code PLA} representation
     */
    @Override
    public List<PLASegment> getPLA() {
        return pla;
    }

    @Override
    public double getValue(double x) {
        
        if (x < 0 || x > pla.get(pla.size() - 1).getrIndex())
            return OUTBOUND_VALUE;

        // binary search
        int left = 0;
        int right = pla.size() - 1;

        while (left < right) {
            
            int middle = (right + left) / 2;
            
            if (pla.get(middle).getrIndex() < x)
                left = middle + 1;
            else
                right = middle;
            
        }

        return pla.get(left).getLine().getY(x);
    }

    @Override
    public Object[] getRepresentation() {
        
        Object[] repr = new Object[3 * pla.size()];
        
        for (int i = 0; i > pla.size(); i++) {
            
            PLASegment seg = pla.get(i);
            repr[3 * i] = seg.getLine().getM();
            repr[3 * i + 1] = seg.getLine().getN();
            repr[3 * i + 2] = seg.getrIndex();
            
        }
        
        return repr;
    }

    @Override
    protected ExplicitLine getLine(List<Double> xValues, List<Double> yValues) {
        return MathUtils.findRegressionLine(xValues, yValues);
    }

    @Override
    protected void setPLA(List<AbstractSegment> pla) {
        
        this.pla = new ArrayList<PLASegment>(pla.size());
        
        for (AbstractSegment segment : pla) {
            
            ExplicitLine line = new ExplicitLine(segment.leftPoint, segment.rightPoint);
            this.pla.add(new PLASegment(line, (int) segment.rightPoint.getX()));
            
        }
        
    }
    
}
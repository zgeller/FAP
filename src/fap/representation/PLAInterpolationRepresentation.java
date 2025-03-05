package fap.representation;

import java.util.ArrayList;
import java.util.List;

import fap.core.data.DataPoint;
import fap.core.data.TimeSeries;
import fap.core.data.Representation;
import fap.util.ExplicitLine;

/**
 * Piecewise Linear Approximation (PLA) with linearly interpolated line segments
 * <p>Representation of time series with piecewise linear segments.
 * <p>
 * Implements three algorithms:<br>
 * <ul>
 * 		<li>sliding window</li>
 * 		<li>top-down</li>
 * 		<li>bottom-up</li>
 * </ul>
 * with linear interpolation.
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
 * @author 	Miklos Kalozi, Zoltan Geller, Brankica Bratic
 * @version 2025.03.05.
 * @see 	Representation
 */
public class PLAInterpolationRepresentation extends AbstractPLARepresentation<DataPoint> {
	
    private static final long serialVersionUID = 1L;
    
    /**
     * The Piecewise Linear Approximation (PLA) representation. Line segments are
     * created with linear interpolation. Each line is defined with two successive
     * points of this list. Default value of this list is null.
     */
	private List<DataPoint> pla;
	
    /**
     * Creates a new {@code PLA} representation.
     * 
     * @param series    time series
     * @param maxError  maximum error bound (must be non-negative)
     * @param algorithm type of the algorithm
     */
    public PLAInterpolationRepresentation(final TimeSeries series, double maxError, int algorithm) {
        super(series, maxError, algorithm);
    }
	
    /**
     * Creates a new {@code PLA} representation.
     * 
     * @param values    time series values
     * @param maxError  maximum error bound (must be non-negative)
     * @param algorithm type of the algorithm
     */
    public PLAInterpolationRepresentation(final double[] values, double maxError, int algorithm) {
        super(values, maxError, algorithm);
    }
	
	/**
	 * Creates a new {@code PLA} representation for parameters given in string {@code argsStr}.
	 * @param argsStr arguments for PLA instantiation
	 * @throws IOException 
	 */
/*
	public PLAInterpolationRepresentation(String argsStr) throws IOException {
		super(argsStr);		
	}
*/
	
    /**
     * Returns the {@code PLA} representation in which line segments are presented
     * with two successive DataPoints.
     * 
     * @return the {@code PLA} representation
     */
    @Override
    public List<DataPoint> getPLA() {
        return pla;
    }

    @Override
    public double getValue(double x) {

        if (x < 0 || x > pla.get(pla.size() - 1).getX())
            return OUTBOUND_VALUE;

        // binary search
        int left = 0;
        int right = pla.size() - 1;

        while (left < right) {
            
            int middle = (right + left) / 2;
            
            if (pla.get(middle).getX() < x)
                left = middle + 1;
            else
                right = middle;
            
        }

        DataPoint leftPoint = right == 0 ? pla.get(0) : pla.get(right - 1);
        DataPoint rightPoint = right == 0 ? pla.get(1) : pla.get(right);
        ExplicitLine line = new ExplicitLine(leftPoint, rightPoint);

        return line.getY(x);
    }
	
    @Override
    public Object[] getRepresentation() {
        
        Object[] repr = new Object[2 * pla.size()];
        
        for (int i = 0; i < pla.size(); i++) {
            
            DataPoint point = pla.get(i);
            repr[2 * i] = point.getX();
            repr[2 * i + 1] = point.getY();
            
        }
        
        return repr;
    }
	
    @Override
    protected ExplicitLine getLine(List<Double> xValues, List<Double> yValues) {
        
        // In case when there is only one point, method creates line that crosses that point, and that is parallel with x-axis.
        if (xValues.size() == 1) 
            return new ExplicitLine(xValues.get(0), yValues.get(0), xValues.get(0) + 1, yValues.get(0));
        else
            return new ExplicitLine(xValues.get(0), yValues.get(0), xValues.get(xValues.size() - 1),
                    yValues.get(yValues.size() - 1));

    }
	
    @Override
    protected void setPLA(List<AbstractSegment> pla) {
        
        this.pla = new ArrayList<DataPoint>(pla.size() + 1);
        this.pla.add(pla.get(0).leftPoint);
        
        for (int i = 0; i < pla.size(); i++)
            this.pla.add(pla.get(i).rightPoint);
        
    }
    
}


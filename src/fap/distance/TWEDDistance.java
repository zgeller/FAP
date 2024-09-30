package fap.distance;

import fap.core.data.DataPoint;
import fap.core.data.TimeSeries;

/**
 * TWED (Time Warp Edit Distance) distance measure.
 * 
 * <p>
 * Let <code>A = (a<sub>1</sub>, a<sub>2</sub>, …, a<sub>n</sub>)</code> and
 * <code>B = (b<sub>1</sub>, b<sub>2</sub>, …, b<sub>m</sub>)</code> be two time
 * series. Then:
 * 
 * <blockquote> <img src="doc-files/TWEDDistance-1.png"> </blockquote>
 * 
 * where {@code a.x} is the time component and {@code a.y} is the value of the
 * data point {@code a}.
 * 
 * <p>
 * Then:
 * 
 * <blockquote> <img src="doc-files/TWEDDistance-2.png"> </blockquote>
 * 
 * <p>
 * Parameters:
 * <ul>
 *  <li> {@link #nu} - a (non-negative) constant value that controls the stiffness
 *       of the measure
 *  <li> {@link #lambda} - a (non-negative) constant value used to calculate
 *       penalties for insert and delete
 * </ul>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> P.-F. Marteau, Time Warp Edit Distance with Stiffness Adjustment for Time
 *       Series Matching, IEEE Trans. Pattern Anal. Mach. Intell. 31 (2009) 306–318.
 *       <a href="https://doi.org/10.1109/TPAMI.2008.76">
 *          https://doi.org/10.1109/TPAMI.2008.76</a>.
 * </ol>
 * 
 * @author Zoltan Geller
 * @version 2024.09.24.
 * @see AbstractCopyableDistance
 * @see TWEDParameters
 */
public class TWEDDistance extends AbstractCopyableDistance implements TWEDParameters {

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
     * Constructs a new TWED distance measure with the default values of the
     * parameters {@link #nu} and {@link #lambda}.
     */
    public TWEDDistance() {
    }
    
    /**
     * Constructs a new TWED distance measure and with the default values of the
     * parameters {@link #nu} and {@link #lambda}, and sets whether to store
     * distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public TWEDDistance(boolean storing) {
        super(storing);
    }

    /***
     * Constructs a new TWED distance measure with the specified values of
     * {@link #nu} and {@link #lambda}.
     * 
     * @param nu     the value that is to be used to control the stiffness of the
     *               measure
     * @param lambda the value used to calculate penalties for insert and delete
     *               operations
     */
    public TWEDDistance(double nu, double lambda) {
        this.setNu(nu);
        this.setLambda(lambda);
    }
    
    /***
     * Constructs a new TWED distance measure with the specified values of
     * {@link #nu} and {@link #lambda}, and sets whether to store distances.
     * 
     * @param nu      the value that is to be used to control the stiffness of the
     *                measure
     * @param lambda  the value used to calculate penalties for insert and delete
     *                operations
     * @param storing {@code true} if storing distances should be enabled
     */
    public TWEDDistance(double nu, double lambda, boolean storing) {
        super(storing);
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

        double nu = this.getNu();
        double lambda = this.getLambda();

        // dynamic programming structures
        double curRow[] = new double[slen + 1]; // current row of the matrix
        double prevRow[] = new double[slen + 1]; // previous row of the matrix

        // auxiliary arrays
        double gdelta[] = new double[glen]; // auxiliary array for gdata
        double sdelta[] = new double[slen]; // auxiliary array for sdata

        DataPoint dp;

        // there is no previous point for gdata: (0,0) by definition
        double prevgy = 0; // value
        double prevgx = 0; // time

        // there is no previous point for sdata: (0,0) by definition
        double prevsy = 0; // value
        double prevsx = 0; // time

        // matrix and array initialization
        prevRow[0] = 0;
        for (int i = 0; i < slen; i++) {

            // matrix
            prevRow[i + 1] = Double.POSITIVE_INFINITY;

            // sdelta auxiliary array
            dp = sdata.get(i);
            double y = dp.getY();
            double x = dp.getX();
            sdelta[i] = Math.abs(y - prevsy) + nu * (x - prevsx) + lambda;
            prevsy = y;
            prevsx = x;

            // gdelta auxiliary array
            dp = gdata.get(i);
            y = dp.getY();
            x = dp.getX();
            gdelta[i] = Math.abs(y - prevgy) + nu * (x - prevgx) + lambda;
            prevgy = y;
            prevgx = x;
            
        }

        // if the time series aren't the same length, we must continue with the
        // initialization of the longer one
        if (slen < glen)

            for (int i = slen; i < glen; i++) {

                dp = gdata.get(i);
                double y = dp.getY();
                double x = dp.getX();
                gdelta[i] = Math.abs(y - prevgy) + nu * (x - prevgx) + lambda;
                prevgy = y;
                prevgx = x;
            }

        double tmp[];

        // there is no previous point for gdata: (0,0) by definition
        prevgy = 0; // value
        prevgx = 0; // time

        for (int i = 0; i < glen; i++) { // i-th row

            curRow[0] = Double.POSITIVE_INFINITY; // initializing by definition

            dp = gdata.get(i); // current point of gdata
            double gy = dp.getY(); // value
            double gx = dp.getX(); // time

            // there is no previous point for sdata: (0,0) by definition
            prevsy = 0; // value
            prevsx = 0; // time

            double gdeltai = gdelta[i]; // optimization

            for (int j = 0; j < slen; j++) { // j-th column

                dp = sdata.get(j); // current point of sdata
                double sy = dp.getY(); // value
                double sx = dp.getX(); // time

                double T1 = prevRow[j + 1] + gdeltai;
                double T2 = Math.min(T1, curRow[j] + sdelta[j]);
                curRow[j + 1] = Math.min(T2, 
                                         prevRow[j] + Math.abs(sy - gy) + Math.abs(prevsy - prevgy)
                                             + nu * (Math.abs(sx - gx) + Math.abs(prevsx - prevgx))
                                        );

                prevsy = sy;
                prevsx = sx;

            }

            prevgy = gy;
            prevgx = gx;

            tmp = curRow;
            curRow = prevRow;
            prevRow = tmp;

        }
        
        distance = prevRow[slen];
        
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
    protected void init(TWEDDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setNu(this.getNu());
        copy.setLambda(this.getLambda());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        TWEDDistance copy = new TWEDDistance();
        init(copy, deep);
        return copy;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", nu=" + getNu() + ", lambda=" + getLambda();
    }
    
}

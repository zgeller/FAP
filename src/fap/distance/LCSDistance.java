package fap.distance;

import fap.core.data.TimeSeries;

/**
 * LCS (Longest Common Subsequence) distance measure.
 * 
 * Two data points are considered to match if their distance is not greater than
 * the {@link AbstractThresholdDistance#epsilon matching threshold}.
 * 
 * <p>
 * Let <code>A = (a<sub>1</sub>, a<sub>2</sub>, …, a<sub>n</sub>)</code> and
 * <code>B = (b<sub>1</sub>, b<sub>2</sub>, …, b<sub>m</sub>)</code> be two time
 * series. Then:
 * 
 * <blockquote> <img src="doc-files/LCSDistance-1.png"> </blockquote>
 * 
 * and 
 * 
 * <blockquote> <img src="doc-files/LCSDistance-2.png"> </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> M. Vlachos, G. Kollios, D. Gunopulos, Discovering similar
 *       multidimensional trajectories, in: Proc. 18th Int. Conf. Data Eng., IEEE
 *       Comput. Soc, 2002: pp. 673–684. 
 *       <a href="https://doi.org/10.1109/ICDE.2002.994784">
 *          https://doi.org/10.1109/ICDE.2002.994784</a>.
 * </ol>
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractThresholdDistance
 */
public class LCSDistance extends AbstractThresholdDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new LCS distance measure with the default value of the matching threshold
     * ({@link AbstractThresholdDistance#epsilon}).
     */
    public LCSDistance() {
    }

    /**
     * Constructs a new LCS distance measure with the default value of the
     * matching threshold ({@link AbstractThresholdDistance#epsilon}) and sets
     * whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public LCSDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Constructs a new LCS distance measure with the specified matching threshold
     * value ({@code epsilon}).
     * 
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     */
    public LCSDistance(double epsilon) {
        setEpsilon(epsilon);
    }
    
    /**
     * Constructs a new LCS distance measure with the specified matching threshold
     * value ({@code epsilon}) and sets whether to store distances.
     * 
     * @param epsilon the value of the matching threshold, it must be {@code >= 0}
     * @param storing {@code true} if storing distances should be enabled
     */
    public LCSDistance(double epsilon, boolean storing) {
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
        for (int i = 0; i <= slen; i++)
            prevRow[i] = 0;
        curRow[0] = 0;

        long tmp[];

        for (int i = 0; i < glen; i++) {

            double y1 = gdata.getY(i);

            for (int j = 1; j <= slen; j++) {
                
                double y2 = sdata.getY(j - 1);

                if (Math.abs(y1 - y2) <= epsilon)
                    curRow[j] = 1 + prevRow[j - 1];
                else
                    curRow[j] = Math.max(prevRow[j], curRow[j - 1]);
                
            }

            tmp = curRow;
            curRow = prevRow;
            prevRow = tmp;
        }

        int len = slen + glen;

        distance = 0;
        if (len > 0)
            distance = (double) (len - 2 * prevRow[slen]) / (double) len;
        
        // save the distance into the memory
        this.store(series1, series2, distance);
        
        return distance;

    }

    @Override
    public Object makeACopy(boolean deep) {
        LCSDistance copy = new LCSDistance();
        init(copy, deep);
        return copy;
    }

}
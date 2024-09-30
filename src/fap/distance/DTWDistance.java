package fap.distance;

import fap.core.data.TimeSeries;

/**
 * DTW (Dynamic Time Warping) distance measure.
 * 
 * <p>
 * Let <code>A = (a<sub>1</sub>, a<sub>2</sub>, …, a<sub>n</sub>)</code> and
 * <code>B = (b<sub>1</sub>, b<sub>2</sub>, …, b<sub>m</sub>)</code> be two time
 * series. Then:
 * 
 * <blockquote> <img src="doc-files/DTWDistance-1.png"> </blockquote>
 * 
 * where
 * 
 * <blockquote> <img src="doc-files/DTWDistance-2.png"> </blockquote>
 * 
 * and
 * 
 * <blockquote> <img src="doc-files/DTWDistance-3.png"> </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> D. Berndt, J. Clifford, Using dynamic time warping to find patterns in
 *       time series, in: R.U. Usama M. Fayyad (Ed.), Knowl. Discov. Databases Pap.
 *       from 1994 AAAI Work., AAAI Press, Seattle, Washington, 1994: pp. 359–370.
 *       <a href="http://dblp.uni-trier.de/rec/bib/conf/kdd/BerndtC94">
 *          http://dblp.uni-trier.de/rec/bib/conf/kdd/BerndtC94</a>.
 * </ol>
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 */
public class DTWDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new DTW distance measure.
     */
    public DTWDistance() {
    }
    
    /**
     * Constructs a new DTW distance measure and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public DTWDistance(boolean storing) {
        super(storing);
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

        double curRow[] = new double[slen + 1];
        double prevRow[] = new double[slen + 1];

        // initialization
        prevRow[0] = 0;
        for (int i = 1; i <= slen; i++)
            prevRow[i] = Double.POSITIVE_INFINITY;

        double tmp[];

        for (int i = 0; i < glen; i++) {
            
            curRow[0] = Double.POSITIVE_INFINITY;
            double y1 = gdata.getY(i);

            for (int j = 1; j <= slen; j++) {
                
                double delta = y1 - sdata.getY(j - 1);

                curRow[j] = delta * delta + // Math.abs(delta) +
                        Math.min(prevRow[j], Math.min(prevRow[j - 1], curRow[j - 1]));
                
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
        DTWDistance copy = new DTWDistance();
        init(copy, deep);
        return copy;
    }

}

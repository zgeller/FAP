package fap.representation;

import java.util.Arrays;
import java.util.stream.Collectors;

import fap.core.data.TimeSeries;
import fap.core.data.Representation;

/**
 * Piecewise Aggregate Appoximation (PAA)
 * 
 * <p>
 * Representation of time series with piecewise aggregate segments.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> E. Keogh, K. Chakrabarti, M. Pazzani, S. Mehrotra, Dimensionality
 *       Reduction for Fast Similarity Search in Large Time Series Databases, Knowl.
 *       Inf. Syst. 3 (2001) 263–286. 
 *       <a href="https://doi.org/10.1007/PL00011669">
 *          https://doi.org/10.1007/PL00011669</a>.
 *  <li> B.-K. Yi, C. Faloutsos, Fast Time Sequence Indexing for Arbitrary Lp
 *       Norms, in: Proc. 26th Int. Conf. Very Large Data Bases, Morgan Kaufmann
 *       Publishers Inc., San Francisco, CA, USA, 2000: pp. 385–394.
 *  <li> E.J. Keogh, M.J. Pazzani, Scaling up dynamic time warping for datamining
 *       applications, in: Proc. Sixth ACM SIGKDD Int. Conf. Knowl. Discov. Data Min.,
 *       ACM, New York, NY, USA, 2000: pp. 285–289. 
 *       <a href="https://doi.org/10.1145/347090.347153">
 *          https://doi.org/10.1145/347090.347153</a>.
 *   <li> J. Lin, E. Keogh, L. Wei, S. Lonardi, Experiencing SAX: a novel symbolic
 *        representation of time series, Data Min. Knowl. Discov. 15 (2007) 107–144.
 *        <a href="https://doi.org/10.1007/s10618-007-0064-z">
 *           https://doi.org/10.1007/s10618-007-0064-z</a>.
 * </ol>
 * 
 * @author Miklos Kalozi, Zoltan Geller, Brankica Bratic
 * @version 2024.09.14.
 * @see Representation
 */
public class PAARepresentation implements Representation {
	
    private static final long serialVersionUID = 1L;

    /**
	 * Original size of data.
	 */
	private int originalSize;
	
    /**
     * The Piecewise Aggregate Approximation (PAA) representation. Default value is
     * an empty list.
     */
    private double[] paa;

    /**
     * Creates a new {@code PAA} representation.
     * 
     * @param series time series
     * @param d      number of segments, must be in range
     *               {@code [1..number of data points]} (representation
     *               dimensionality)
     */
    public PAARepresentation(final TimeSeries series, int d) {
        this(series.getYValues(), d);
    }
	
    /**
     * Creates a new {@code PAA} representation.
     * 
     * @param values time series values
     * @param d      number of segments, must be in range
     *               {@code [1..number of data points]} (representation
     *               dimensionality)
     */
    public PAARepresentation(final double[] values, int d) {
        createPAA(values, d);
    }
	
	/**
	 * Creates a new {@code PAA} representation for parameters given in string {@code argsStr}.
	 * @param argsStr arguments for PAA instantiation
	 * @throws IOException 
	 */
/*
	public PAARepresentation(String argsStr) throws IOException {
		
		String[] args = argsStr.split(Console.ARGS_DELIMITER);
		
		String dataSet = null;
		char dataSetSeparator = ' ';
		boolean dataSetHasXValue = false;
		int d = -1;
		int ts = -1;
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(Console.ARG_PREFIX+"ds")) {
				dataSet = args[i+1];
			} else if (args[i].equals(Console.ARG_PREFIX+"d")) {
				try {
					d = Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					throw new IllegalArgumentException("Representation dimensionality must be integer number.");
				}
			} else if (args[i].equals(Console.ARG_PREFIX+"ts")) {
				try {
					ts = Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					throw new IllegalArgumentException("Index of time series must be integer number.");
				}
			} else if (args[i].equals(Console.ARG_PREFIX+"sep")) {
				if (args[i+1].length() > 1)
					throw new IllegalArgumentException("Dataset separator cannot contain more than one character.");
				dataSetSeparator = args[i+1].charAt(0);
			} else if (args[i].equals(Console.ARG_PREFIX+"hasX")) {
				dataSetHasXValue = true;
			}
		}
	
		if (dataSet == null || d < 0 || ts < 0) {
			throw new IllegalArgumentException("Illegal arguments. PAARepresentation representation arguments:\n" +
											   "   " + Console.ARGS_DELIMITER + "ds   (required argument) - url of dataset file\n" +
											   "   " + Console.ARGS_DELIMITER + "ts   (required argument) - index of time series inside of dataset\n" +
											   "   " + Console.ARGS_DELIMITER + "d    (required argument) - representation dimensionality\n" +
											   "   " + Console.ARGS_DELIMITER + "sep  (optional argument) - dataset separator (default separator is space)\n" +
											   "   " + Console.ARGS_DELIMITER + "hasX (optional argument) - only if dataset has x values, this argument should be present (this argument should not have value)");
		}
		
		TimeSeriesTextFileReader tsfr = new TimeSeriesTextFileReader(dataSet, dataSetSeparator, dataSetHasXValue);
		Dataset<TimeSeries> timeSeriesList;
		try {
			timeSeriesList = tsfr.load();
		} catch (FileNotFoundException fe) {
			throw new IllegalArgumentException("File " + dataSet + " does not exist.");
		} catch (IOException e) {
			throw e;
		}
		
		if (timeSeriesList.size() - 1 < ts) {
			throw new IllegalArgumentException("Dataset does not have " + ts + " time series.");
		}
		
		TimeSeries timeSerie = timeSeriesList.get(ts);
		createPAA(timeSerie.getData().getYValues(), d);
	}
*/

    /**
     * Returns the number of segments (representation dimensionality).
     * 
     * @return the number of segments
     */
    public int getD() {
        return paa.length;
    }
	
    @Override
    public String toString() {
        return String.join(" ",
                Arrays.asList(getRepresentation()).stream().map(x -> x.toString()).collect(Collectors.toList()));
    }
	
    /**
     * Creates a new {@code PAA} representation of given dimensionality.
     * 
     * @param values time series values
     * @param d      representation dimensionality (must be even)
     */
    private void createPAA(final double[] values, int d) {
        
        if (d < 1 || d > values.length)
            throw new IllegalArgumentException(
                    "Representation dimensionality must be between 1 and " + values.length + ".");

        originalSize = values.length;
        
        // initialization of paa - it will be of length d (representation dimensionality)
        paa = new double[d];
        
        // IMPORTANT - this must be placed after initialization of fields originalSize and paa
        int segmentSize = getSegmentSize();
        
        int vIndex = 0;
        
        // one loop per each paa segment
        for (int i = 0; i < d; i++) {
            
            float segmentValue = 0;
            int segmentCount = 0;
            
            // calculation of average value for current segment
            for (int j = 0; j < segmentSize; j++) {
                
                // this can happen only for the last segment
                if (vIndex >= values.length) 
                    break;
                
                segmentValue += values[vIndex++];
                segmentCount++;
                
            }
            
            paa[i] = segmentValue / segmentCount;
            
        }
        
    }
	
    @Override
    public double getValue(double x) {

        if (x < 0 || x >= originalSize)
            return OUTBOUND_VALUE;

        int segmentSize = getSegmentSize();
        int segment = (int) (x / segmentSize);

        return paa[segment];
    }
	
    @Override
    public Object[] getRepresentation() {
        Object[] repr = new Object[paa.length];
        for (int i = 0; i < paa.length; i++)
            repr[i] = paa[i];
        return repr;
    }
	
    /**
     * Returns the {@code PAA} representation.
     * 
     * @return the {@code PAA} representation
     */
    public double[] getPAA() {
        return paa;
    }
	
    /**
     * Returns the size of the segments.
     * 
     * @return the size of the segments
     */
    public int getSegmentSize() {
        return (int) Math.ceil((double) originalSize / paa.length);
    }
    
}

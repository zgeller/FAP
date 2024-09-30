package fap.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import fap.core.data.TimeSeries;
import fap.core.data.Representation;
import fap.util.MathUtils;

/**
 * Discrete Haar Wavelet Transform representation.
 * 
 * <p>
 * References:
 * <ol>
 * <li> Kin-Pong Chan, Ada Wai-Chee Fu, Efficient time series matching by
 *      wavelets, in: Proc. 15th Int. Conf. Data Eng. (Cat. No.99CB36337), IEEE,
 *      1999: pp. 126–133. 
 *      <a href="https://doi.org/10.1109/ICDE.1999.754915">
 *         https://doi.org/10.1109/ICDE.1999.754915</a>.
 *  <li> A.W. Fu, O.T.-W. Leung, E. Keogh, J. Lin, Finding Time Series Discords
 *       Based on Haar Transform, in: 2006: pp. 31–41. 
 *       <a href="https://doi.org/10.1007/11811305_3">
 *          https://doi.org/10.1007/11811305_3</a>.
 *  <li> J. Abonyi, Adatbányászat a hatékonyság eszköze, 1st ed., ComputerBooks,
 *       Budapest, 2006.
 *  <li> M. Vlachos, J. Lin, E.J. Keogh, D. Gunopulos, A Wavelet-Based Anytime
 *       Algorithm for K-Means Clustering of Time Series, in: Work. Clust. High
 *       Dimens. Data Its Appl. 3rd SIAM Int. Conf. Data Min., San Francisco, CA, USA,
 *       2003. 
 *       <a href="https://api.semanticscholar.org/CorpusID:18338443">
 *          https://api.semanticscholar.org/CorpusID:18338443</a>.
 *  <li> P. Chaovalit, A. Gangopadhyay, G. Karabatis, Z. Chen, Discrete wavelet
 *       transform-based time series analysis and mining, ACM Comput. Surv. 43 (2011)
 *       1–37. 
 *       <a href=https://doi.org/10.1145/1883612.1883613">
 *          https://doi.org/10.1145/1883612.1883613</a>.
 * </ol>
 * 
 * @author Zoltan Geller, Brankica Bratic
 * @version 2024.09.14.
 * @see Representation
 */
public class HaarRepresentation implements Representation {
	
    private static final long serialVersionUID = 1L;

    public final static int PRESERVE_ALL_COEFFICIENTS = -1;

    /**
     * The coeficients of the Discrete Haar Wavelet Transform representation.
     */
    private double haar[];

    /**
     * Length of the original time series.
     */
    int originalTimeSeriesLength;

    /**
     * Creates a new {@code Haar} representation preserving all {@code Haar}
     * coefficients. If the length of time series is not power of two, time series
     * is padded with zeros in order to satisfy this criteria.
     * 
     * @param series time series
     */
    public HaarRepresentation(final TimeSeries series) {
        this(series, PRESERVE_ALL_COEFFICIENTS);
    }
	
    /**
     * Creates a new {@code Haar} representation of a given dimensionality. If the
     * length of time series is not power of two, time series is padded with zeros
     * in order to satisfy this criteria.
     * 
     * @param series time series,
     * @param d      number of {@code Haar} coefficients that will be preserved in
     *               {@code Haar Wavelet} representation (representation
     *               dimensionality)
     */
    public HaarRepresentation(final TimeSeries series, int d) {
        this(series.getYValues(), d);
    }
	
    /**
     * Creates a new {@code Haar} representation preserving all {@code Haar}
     * coefficients. If the length of time series is not power of two, time series
     * is padded with zeros in order to satisfy this criteria.
     * 
     * @param values time series values
     */
    public HaarRepresentation(double[] values) {
        this(values, PRESERVE_ALL_COEFFICIENTS);
    }
	
    /**
     * Creates a new {@code Haar} representation of a given dimensionality. If the
     * length of time series is not power of two, time series is padded with zeros
     * in order to satisfy this criteria.
     * 
     * @param values time series values
     * @param d      number of {@code Haar} coefficients that will be preserved in
     *               {@code Haar Wavelet} representation (representation
     *               dimensionality)
     */
    public HaarRepresentation(double[] values, int d) {
        createHaar(values, d);
    }
	
	/**
	 * Creates a new {@code Haar} representation for parameters given in string {@code argsStr}.
	 * @param argsStr arguments for Haar instantiation
	 * @throws IOException 
	 */
/*
	public HaarRepresentation(String argsStr) throws IOException {
		
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
	
		if (dataSet == null || ts < 0) {
			throw new IllegalArgumentException("Illegal arguments. HaarRepresentation representation arguments:\n" +
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
		createHaar(timeSerie.getData().getYValues(), d);
	}
*/
	
    /**
     * Returns the {@code Haar} representation.
     * 
     * @return the {@code Haar} representation.
     */
    public double[] getHaar() {
        return this.haar;
    }
    
    @Override
    public double getValue(double x) {
    
        if (x < 0 || x >= originalTimeSeriesLength)
            return OUTBOUND_VALUE;
    
        ArrayList<Double> values = new ArrayList<Double>(originalTimeSeriesLength);
        double[] haarCoeffsWithZeros = getHaarCoefficientsInOriginalDimensionality();
        values.add(haarCoeffsWithZeros[0]);
    
        // reconstruction of original time series
        int currentHaarIndex = 1;
        
        while (values.size() < originalTimeSeriesLength) {
            
            ArrayList<Double> tempValues = new ArrayList<Double>();
            
            for (double value : values) {
                
                double haarCoeff = haarCoeffsWithZeros[currentHaarIndex++];
                tempValues.add(value + haarCoeff);
                tempValues.add(value - haarCoeff);
                
            }
            
            values = tempValues;
            
        }
    
        // at the end, value at position x is returned
        return values.get((int) x);
        
    }

    @Override
    public Object[] getRepresentation() {
        
        Object[] repr = new Object[haar.length];
        
        for (int i = 0; i < haar.length; i++)
            repr[i] = haar[i];
        
        return repr;
    }
	
    @Override
    public String toString() {
        return String.join(" ",
                Arrays.asList(getRepresentation()).stream().map(x -> x.toString()).collect(Collectors.toList()));
    }
	
    /**
     * Creates a new {@code Haar} representation of given dimensionality.
     * 
     * @param values time series values
     * @param d      representation dimensionality (must be even). If d has value of
     *               -1, all Haar coefficients are preserved.
     */
    private void createHaar(final double[] values, int d) {
        
        originalTimeSeriesLength = values.length;
        if (d == PRESERVE_ALL_COEFFICIENTS)
            d = originalTimeSeriesLength;

        if (d < 1 || d > originalTimeSeriesLength)
            throw new IllegalArgumentException(
                    "Representation dimensionality must be between 1 and " + values.length + ".");

        ArrayList<Double> coefficients = new ArrayList<Double>(originalTimeSeriesLength);
        ArrayList<Double> currentValues = new ArrayList<Double>(originalTimeSeriesLength);

        for (int i = 0; i < MathUtils.integralPowerOfTwoCeil(originalTimeSeriesLength); i++)
            
            if (i < originalTimeSeriesLength)
                currentValues.add(values[i]);
            else
                // if time series length is not power of 2, time series is padded with zeroes
                currentValues.add(0d);

        while (currentValues.size() > 1) {

            ArrayList<Double> tempValues = new ArrayList<Double>();
            
            for (int i = currentValues.size() - 1; i > 0; i -= 2) {
                
                double value1 = currentValues.get(i - 1);
                double value2 = currentValues.get(i);
                double average = (value1 + value2) / 2;
                double coeff = (value1 - value2) / 2;

                tempValues.add(0, average);
                coefficients.add(0, coeff);
                
            }

            currentValues = tempValues;
            
        }

        haar = new double[d];
        haar[0] = currentValues.get(0);
        for (int i = 0; i < d - 1; i++)
            haar[i + 1] = coefficients.get(i);
    }
	
    /**
     * Returns array of Haar coefficients. The length of this array is equal to the
     * integral power of two ceiling of the original dimensionality. In order to
     * reach this length, array of preserved Haar coefficients is padded with zeros.
     * 
     * @return
     */
    private double[] getHaarCoefficientsInOriginalDimensionality() {
        
        int integralPowerOfTwoDimensionality = MathUtils.integralPowerOfTwoCeil(originalTimeSeriesLength);
        double[] haarCoeffsWithZeros = new double[integralPowerOfTwoDimensionality];

        for (int i = 0; i < integralPowerOfTwoDimensionality; i++) {
            
            if (i < haar.length)
                haarCoeffsWithZeros[i] = haar[i];
            else
                haarCoeffsWithZeros[i] = 0;
            
        }

        return haarCoeffsWithZeros;
        
    }
    
}

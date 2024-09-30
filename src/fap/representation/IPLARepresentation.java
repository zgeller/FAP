package fap.representation;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import fap.core.data.TimeSeries;
import fap.core.data.Representation;
import fap.util.ExplicitLine;

/**
 * Indexable Piecewise Linear Approximation (IPLA)
 * 
 * <p>
 * Representation of time series with equal size piecewise aggregate segments.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> Q. Chen, L. Chen, X. Lian, Y. Liu, J.X. Yu, Indexable PLA for
 *       Efficient Similarity Search, in: Proc. 33rd Int. Conf. Very Large Data Bases,
 *       VLDB Endowment, 2007: pp. 435–446. 
 *       <a href="http://dl.acm.org/citation.cfm?id=1325851.1325903">
 *          http://dl.acm.org/citation.cfm?id=1325851.1325903</a>.
 * </ol>
 * 
 * @author Brankica Bratic, Zoltan Geller
 * @version 2024.09.14.
 * @see Representation
 */
public class IPLARepresentation implements Representation {

    private static final long serialVersionUID = 1L;

    /**
	 * IPLA representation.
	 */
	private ArrayList<ExplicitLine> ipla;
	
	/**
	 * Original size of data.
	 */
	private int originalSize;
	
	/**
	 * Number of points in each line segment.
	 */
	private int segmentSize;
	
    /**
     * Creates a new {@code IPLA} representation of given dimensionality.
     * 
     * @param series time series
     * @param d      representation dimensionality (must be even)
     */
    public IPLARepresentation(final TimeSeries series, int d) {
        this(series.getYValues(), d);
    }
	
    /**
     * Creates a new {@code IPLA} representation of given dimensionality.
     * 
     * @param values time series values
     * @param d      representation dimensionality (must be even)
     */
    public IPLARepresentation(final double[] values, int d) {
        createIPLA(values, d);
    }
	
	/**
	 * Creates a new {@code IPLA} representation for parameters given in string {@code argsStr}.
	 * @param argsStr arguments for IPLA instantiation
	 * @throws IOException 
	 */
/*
	public IPLARepresentation(String argsStr) throws IOException {
		
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
			throw new IllegalArgumentException("Illegal arguments. IPLARepresentation representation arguments:\n" +
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
		createIPLA(timeSerie.getData().getYValues(), d);
	}
*/
	
    @Override
    public double getValue(double x) {
        
        if (x < 0 || x >= originalSize)
            return OUTBOUND_VALUE;
    
        int lineIndex = (int) x / segmentSize;
        int lineX = (int) x % segmentSize + 1;
    
        
        return ipla.get(lineIndex).getY(lineX);
    }
	
    @Override
    public Object[] getRepresentation() {
        
        Object[] repr = new Object[2 * ipla.size()];
        
        for (int i = 0; i < ipla.size(); i++) {
            
            ExplicitLine seg = ipla.get(i);
            repr[2 * i] = seg.getM();
            repr[2 * i + 1] = seg.getN();
            
        }
        
        return repr;
    }
	
    /**
     * Returns explicit line that is positioned on given index.
     * 
     * @param index index of explicit line that will be returned
     * @return explicit line that is positioned on given index
     */
    public ExplicitLine getLine(int index) {
        
        if (ipla == null || index < 0 || index >= ipla.size())
            return null;

        return ipla.get(index);
        
    }
	
    @Override
    public String toString() {
        return String.join(" ",
                Arrays.asList(getRepresentation()).stream().map(x -> x.toString()).collect(Collectors.toList()));
    }
	
    /**
     * Computes the {@code IPLA} representation.
     * 
     * @param values original time series
     * @param d      representation dimensionality
     */
    private void createIPLA(final double[] values, int d) {

        if (d < 1 || d > 2 * values.length)
            throw new IllegalArgumentException(
                    "Representation dimensionality must be between 1 and " + (2 * values.length) + ".");

        if (d % 2 != 0)
            throw new IllegalArgumentException("Representation dimensionality must be even");

        if (values.length % (d / 2) != 0)
            throw new InvalidParameterException("Original time series dimensionality must be divisible by " + (d / 2)
                    + " (the number of IPLA segments).");

        int m = d / 2;

        originalSize = values.length;

        ipla = new ArrayList<ExplicitLine>(m);

        segmentSize = originalSize / m;

        double xMean = 0;

        for (int t = 1; t <= segmentSize; t++)
            xMean += t;
        
        xMean /= segmentSize;

        int currentIndex = 0;
        for (int i = 0; i < m; i++) {

            double yMean = 0;
            for (int t = 1; t <= segmentSize; t++) 
                yMean += values[currentIndex + t - 1];
            yMean /= segmentSize;

            double upperSum = 0;
            double lowerSum = 0;

            for (int t = 1; t <= segmentSize; t++) {
                
                double dataValue = values[currentIndex + t - 1];

                double temp = t - xMean;
                upperSum += temp * (dataValue - yMean);
                lowerSum += temp * temp;
                
            }

            double b = upperSum / lowerSum;
            double a = yMean - b * xMean;

            ipla.add(new ExplicitLine(b, a));

            currentIndex += segmentSize;
            
        }
        
    }
    
}

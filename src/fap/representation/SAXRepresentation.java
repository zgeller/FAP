/*   
 * Copyright 2024-2025 Miklós Kálózi, Zoltán Gellér, Brankica Bratić
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

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fap.core.data.TimeSeries;
import fap.core.data.Representation;

/**
 * Symbolic Aggregate Approximation (SAX)
 * 
 * <p>
 * Representation of time series with symbolic aggregate segments. The time
 * series has to be normalized before calculating SAX.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> J. Lin, E. Keogh, L. Wei, S. Lonardi, Experiencing SAX: a novel symbolic
 *       representation of time series, Data Min. Knowl. Discov. 15 (2007) 107–144.
 *       <a href="https://doi.org/10.1007/s10618-007-0064-z">
 *          https://doi.org/10.1007/s10618-007-0064-z</a>.
 *  <li> J. Lin, E. Keogh, S. Lonardi, B. Chiu, A symbolic representation of time
 *       series, with implications for streaming algorithms, in: Proc. 8th ACM SIGMOD
 *       Work. Res. Issues Data Min. Knowl. Discov., ACM, New York, NY, USA, 2003: pp.
 *       2–11. 
 *       <a href="https://doi.org/10.1145/882082.882086">
 *          https://doi.org/10.1145/882082.882086</a>.
 * </ol>
 * 
 * @author Miklós Kálózi, Zoltán Gellér, Brankica Bratić
 * @version 2025.03.05.
 * @see Representation
 */
public class SAXRepresentation implements Representation {

    private static final long serialVersionUID = 1L;

    /**
	 * Maximum number of symbols in the representation.
	 */
	public static final int maxAlphabetSize;
	
	private static final double betaValuesCalculationStep = 0.01;
	private static final double betaValuesCalculationInitialValue = -5;
	
	/**
	 * The alphabet.
	 */
	private char alphabet[];
	
	/**
     * Map of the symbols of the alphabet. The values are the indexes of the
     * symbols inside the alphabet.
     */
	private Map<Character,Integer> symbolIndex;
	
	/**
	 * Values of normal distribution for symbol mapping.
	 */
	private double beta[];
	
	/**
	 * Distances between the symbols of the alphabet.
	 */
	public double cellDist[][];
		
	/**
	 * Size of the alphabet. Must be in range {@code [2..maxAlphabetSize]}.
	 */
	private int alphabetSize;
	
	/**
	 * The Symbolic Aggregate Representation (SAX). Default value is {@code null}.
	 */
	private String sax;
	
	/**
	 * Original size of data
	 */
	private int originalSize;

	/**
	 * Mean of the original data
	 */
	private double mean;
	
	/**
	 * Standard deviation of the original data
	 */
	private double std;
	
	static {		
		maxAlphabetSize = Character.MAX_VALUE - Character.MIN_VALUE;
	}
	
    /**
     * Creates a new {@code SAX} representation using the default alphabet.
     * 
     * @param series       time series
     * @param d            number of segments, must be in range
     *                     {@code [1..number of data points]} (representation
     *                     dimensionality)
     * @param alphabetSize the size of the alphabet, must be in range
     *                     {@code [2..maxAlphabetSize]}
     */
    public SAXRepresentation(final TimeSeries series, int d, int alphabetSize) {
        this(series.getYValues(), d, alphabetSize);
	}
	
    /**
     * Creates a new {@code SAX} representation using the given alphabet.
     * 
     * @param series   time series
     * @param d        number of segments, must be in range
     *                 {@code [1..number of data points]} (representation
     *                 dimensionality)
     * @param alphabet the alphabet, the length of the alphabet must be in range
     *                 {@code [2..maxAlphabetSize]}
     */
    public SAXRepresentation(final TimeSeries series, int d, char alphabet[]) {
        this(series.getYValues(), d, alphabet);
    }
	
    /**
     * Creates a new {@code SAX} representation using the default alphabet.
     * 
     * @param values       time series values
     * @param d            number of segments, must be in range
     *                     {@code [1..number of data points]} (representation
     *                     dimensionality)
     * @param alphabetSize the size of the alphabet, must be in range
     *                     {@code [2..maxAlphabetSize]}
     */
	public SAXRepresentation(final double[] values, int d, int alphabetSize) {
	    
		double[] normalizedValues = normalizeData(values);
		initialize(normalizedValues, d, alphabetSize);
		this.alphabet = createAlphabet(alphabetSize);		
		initIndexes();
		initDistances();
		findSAX(normalizedValues, d);
		
	}
	
    /**
     * Creates a new {@code SAX} representation using the given alphabet.
     * 
     * @param values   time series values
     * @param d        number of segments, must be in range
     *                 {@code [1..number of data points]} (representation
     *                 dimensionality)
     * @param alphabet the alphabet, the length of the alphabet must be in range
     *                 {@code [2..maxAlphabetSize]}
     */
	public SAXRepresentation(final double[] values, int d, char alphabet[]) {
	    
		double[] normalizedValues = normalizeData(values);
		initialize(normalizedValues, d, alphabet.length);
		this.alphabet = alphabet;
		initIndexes();
		initDistances();
		findSAX(normalizedValues, d);
		
	}
	
	/**
	 * Creates a new {@code SAX} representation for parameters given in string {@code argsStr}.
	 * @param argsStr arguments for SAX instantiation
	 * @throws IOException 
	 */
/*
	public SAX(String argsStr) throws IOException {
		
		String[] args = argsStr.split(Console.ARGS_DELIMITER);
		
		String dataSet = null;
		char dataSetSeparator = ' ';
		boolean dataSetHasXValue = false;
		int d = -1;
		int ts = -1;
		int alphabetSize = -1;
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(Console.ARG_PREFIX+"ds")) {
				dataSet = args[i+1];
			} else if (args[i].equals(Console.ARG_PREFIX+"d")) {
				try {
					d = Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					throw new IllegalArgumentException("Representation dimensionality must be integer number.");
				}
			} else if (args[i].equals(Console.ARG_PREFIX+"alph")) {
				try {
					alphabetSize = Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					throw new IllegalArgumentException("Alphabet size must be integer number.");
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
	
		if (dataSet == null || d < 0 || alphabetSize < 0 || ts < 0) {
			throw new IllegalArgumentException("Illegal arguments. SAX representation arguments:\n" +
											   "   " + Console.ARGS_DELIMITER + "ds   (required argument) - url of dataset file\n" +
											   "   " + Console.ARGS_DELIMITER + "ts   (required argument) - index of time series inside of dataset\n" +
											   "   " + Console.ARGS_DELIMITER + "d    (required argument) - representation dimensionality\n" +
											   "   " + Console.ARGS_DELIMITER + "alph (required argument) - alphabet size\n" +
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
		double[] normalizedValues = normalizeData(timeSerie.getData().getYValues());
		initialize(normalizedValues, d, alphabetSize);
		this.alphabet = createAlphabet(alphabetSize);		
		initIndexes();
		initDistances();
		findSAX(normalizedValues, d);
	}
*/
	
    /**
     * Returns the size of the alphabet.
     * 
     * @return the size of the alphabet
     */
    public int getAlphabetSize() {
        return alphabetSize;
    }
	
	@Override
	public String toString() {
		return String.join(" ", Arrays.asList(getRepresentation()).stream().map(x->x.toString()).collect(Collectors.toList()));
	}
	
    /**
     * Initializes the SAX representation.
     * 
     * @param data         data point series
     * @param d            number of segments, must be in range
     *                     {@code [1..number of data points]} (representation
     *                     dimensionality)
     * @param alphabetSize the size of the alphabet, must be in range
     *                     {@code [2..maxAlphabetSize]}
     */
    private void initialize(final double[] values, int d, int alphabetSize) {
        
        if (d < 1 || d > values.length)
            throw new IllegalArgumentException(
                    "Representation dimensionality must be between 1 and " + values.length + ".");
        
        if (alphabetSize < 2 || alphabetSize > maxAlphabetSize)
            throw new IllegalArgumentException("AlphabetSize must be in range [2.." + maxAlphabetSize + "].");
        
        this.alphabetSize = alphabetSize;
        
    }
	
    /**
     * Initializes the symbolIndex structure. The symbols of the alphabet must be
     * mutually different.
     */
    private void initIndexes() {
        
        symbolIndex = new HashMap<Character, Integer>();
        
        for (int i = 0; i < alphabetSize; i++) {
            
            char c = alphabet[i];
            
            if (symbolIndex.containsKey(c))
                throw new IllegalArgumentException("The symbols of the alphabet must be mutually different.");
            else
                symbolIndex.put(c, i);
            
        }
        
    }
	
    /**
     * Calculates distances between the symbols of the alphabet.
     */
    private void initDistances() {
        
        calculateBetaValues(alphabetSize);
        cellDist = new double[alphabetSize][alphabetSize];
        
        for (int row = 0; row < alphabetSize; row++)
            
            for (int col = 0; col < alphabetSize; col++)
                
                if (Math.abs(row - col) <= 1)
                    cellDist[row][col] = 0;
        
                else {
                    int right = Math.max(row, col) - 1;
                    int left = Math.min(row, col);
                    cellDist[row][col] = beta[right] - beta[left];
                }
        
    }
	
    /**
     * Returns the length of the SAX representation (number of segments).
     * 
     * @return the length of the SAX representation (number of segments)
     */
    public int getD() {
        return sax.length();
    }

    /**
     * Computes the {@code SAX} representation.
     */
    private void findSAX(double[] values, int d) {
        
        originalSize = values.length;
        PAARepresentation paa = new PAARepresentation(values, d); // create a PAA representation
        double[] paaSegments = paa.getPAA(); // get the PAA representation
        char c[] = new char[d];
        
        for (int i = 0; i < paaSegments.length; i++) // for every segment of the PAA
            c[i] = getSymbol(paaSegments[i]); // find the symbolic representation
        
        sax = new String(c);
        
    }
	
    /**
     * Returns corresponding value of the PAA representation.
     */
    @Override
    public double getValue(double x) {

        if (x < 0 || x >= originalSize)
            return OUTBOUND_VALUE;

        char chVal = getSAXValue(x);
        return getValue(chVal) * std + mean;
        
    }
	
    /**
     * Returns the value (symbol) of the SAX representation corresponding to the
     * given x coordinate.
     * 
     * @param x value of x for searching point
     * @return the value (symbol) of the SAX representation
     */
    public char getSAXValue(double x) {

        if (x < 0 || x >= originalSize)
            throw new IllegalArgumentException("x must be in range [0," + originalSize + ").");

        int segmentSize = getSegmentSize();
        int segment = (int) (x / segmentSize);

        return sax.charAt(segment);
    }

    @Override
    public Object[] getRepresentation() {
        
        Object[] repr = new Object[sax.length()];
        
        for (int i = 0; i < sax.length(); i++)
            repr[i] = sax.charAt(i);
        
        return repr;
        
    }
	
    /**
     * Returns the {@code SAX} representation.
     * 
     * @return the {@code SAX} representation
     */
    public String getSAX() {
        return sax;
    }

    /**
     * Returns the symbol corresponding to the given mean value.
     * 
     * @param mean the mean value
     * @return the corresponding symbol
     */
    private char getSymbol(double mean) {
        
        int index = 0;
        
        while (index < beta.length && mean >= beta[index])
            index++;
        
        return alphabet[index];
        
    }
	
    /**
     * Returns the mean value of the corresponding symbol.
     * 
     * @param symbol symbol
     * @return mean value of the corresponding symbol
     */
    private double getValue(char symbol) {
        
        int index = 0;
        while (index < alphabet.length && alphabet[index] != symbol)
            index++;
        
        if (index >= alphabet.length)
            return OUTBOUND_VALUE;
        
        else if (index == 0)
            return beta[0] - (beta[1] - beta[0]) / 2;
        
        else if (index == alphabet.length - 1)
            return beta[index - 1] + (beta[index - 1] - beta[index - 2]) / 2;
        
        else
            return (beta[index - 1] + beta[index]) / 2;
        
    }

    /**
     * Returns the distance between two characters.
     * 
     * @param a the first character
     * @param b the second character
     * @return the distance
     */
    public double getDistance(char a, char b) {
        return getDistance(symbolIndex.get(a), symbolIndex.get(b));
    }
	
    /**
     * Returns the distance between two characters.
     * 
     * @param i the index of the first character within the alphabet
     * @param j the index of the second character within the alphabet
     * @return the distance
     */
    public double getDistance(int i, int j) {
        return cellDist[i][j];
    }

    /**
     * Returns the distance between the current SAX representation and
     * {@code other}.
     * 
     * @param other the other SAX representation
     * @param other
     * @return
     */
    public double getDistance(String other) {
        
        double dist = 0;
        for (int i = 0; i < sax.length(); i++) {
            double diff = getDistance(sax.charAt(i), other.charAt(i));
            dist += diff * diff;
        }
        
        return Math.sqrt(getSegmentSize()) * Math.sqrt(dist);
        
    }
	
    /**
     * Creates alphabet of the given size.
     * 
     * @param alphabetSize size of the alphabet that will be created
     * @return alphabet of the given size
     */
    private char[] createAlphabet(int alphabetSize) {
        
        if (alphabetSize > maxAlphabetSize)
            throw new InvalidParameterException("Alphabet cannot be grater than " + maxAlphabetSize + ".");

        int currentChar = 48; // If possible, force alphabet to start from character 48 (number 0)
        int remainingChars = Character.MAX_VALUE - currentChar + 1 - alphabetSize;

        if (remainingChars < 0)
            currentChar += remainingChars;

        char[] alphabet = new char[alphabetSize];

        for (int i = 0; i < alphabetSize; i++) {
            
            alphabet[i] = (char) currentChar;
            currentChar++;
            
        }

        return alphabet;
        
    }
	
    /**
     * Initializes normal distribution of the alphabet letters.
     * 
     * @param alphabetSize size of the alphabet
     */
    private void calculateBetaValues(int alphabetSize) {

        beta = new double[alphabetSize - 1];
        double probability = 1.0 / alphabetSize;

        double currentDesiredProbability = probability;
        double currentProbability = 0;

        double currentValue = betaValuesCalculationInitialValue;
        double sqrt2pi = Math.sqrt(2 * Math.PI);

        for (int i = 0; i < alphabetSize - 1; i++) {
            
            while (currentProbability < currentDesiredProbability) {
                double temp = currentValue + betaValuesCalculationStep / 2;
                currentProbability += betaValuesCalculationStep * Math.exp(-temp * temp / 2) / sqrt2pi;
                currentValue += betaValuesCalculationStep;
            }
            
            currentDesiredProbability += probability;
            beta[i] = currentValue;
            
        }
        
    }
	
    /**
     * Normalizes given values in order to obtain mean 0 and standard deviation 1.
     * 
     * @param values values that will be normalized
     * @return normalized values
     */
    private double[] normalizeData(double[] values) {

        if (values.length == 0)
            return new double[0];

        double sum = 0;

        for (double value : values)
            sum += value;

        mean = sum / values.length;

        double stdSum = 0;

        for (double value : values)
            stdSum += Math.pow(value - mean, 2);

        std = Math.sqrt(stdSum / values.length);

        double[] normalizedValues = new double[values.length];
        for (int i = 0; i < values.length; i++) 
            normalizedValues[i] = (values[i] - mean) / std;

        return normalizedValues;
    }

    /**
     * Returns the size of one SAX segment.
     * 
     * @return the size of one SAX segment
     */
    private int getSegmentSize() {
        return (int) Math.ceil((double) originalSize / sax.length());
    }
    
}

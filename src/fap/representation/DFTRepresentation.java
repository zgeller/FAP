/*   
 * Copyright 2024-2025 Brankica Bratić, Zoltán Gellér
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

import java.util.Arrays;
import java.util.stream.Collectors;

import fap.core.data.TimeSeries;
import fap.core.data.Representation;

/**
 * Discrete Fourier Transform (DFT) representation.
 * <p>
 * 
 * Representation of time series based on DFT.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> R. Agrawal, C. Faloutsos, A. Swami, Efficient similarity search in
 *       sequence databases, in: David B. Lomet (Ed.), Proc. 4th Int. Conf. Found.
 *       Data Organ. Algorithms (FODO ’93), Springer Berlin Heidelberg, 1993: pp.
 *       69–84. 
 *       <a href="https://doi.org/10.1007/3-540-57301-1_5">
 *          https://doi.org/10.1007/3-540-57301-1_5</a>.
 *  <li> C. Faloutsos, M. Ranganathan, Y. Manolopoulos, Fast subsequence matching
 *       in time-series databases, ACM SIGMOD Rec. 23 (1994) 419–429. 
 *       <a href="https://doi.org/10.1145/191843.191925">
 *          https://doi.org/10.1145/191843.191925</a>.
 *  <li> D. Rafiei, On similarity-based queries for time series data, in: Proc.
 *       15th Int. Conf. Data Eng. (Cat. No.99CB36337), IEEE, 1999: pp. 410–417.
 *       <a href="https://doi.org/10.1109/ICDE.1999.754957">
 *          https://doi.org/10.1109/ICDE.1999.754957</a>.
 *  <li> D. Rafiei, A. Mendelzon, Similarity-based queries for time series data,
 *       ACM SIGMOD Rec. 26 (1997) 13–25. 
 *       <a href="https://doi.org/10.1145/253262.253264">
 *          https://doi.org/10.1145/253262.253264</a>.
 *  <li> B.-K. Yi, H. V Jagadish, C. Faloutsos, Efficient retrieval of similar
 *       time sequences under time warping, in: Data Eng. 1998. Proceedings., 14th
 *       Int. Conf., 1998: pp. 201–208. 
 *       <a href="https://doi.org/10.1109/ICDE.1998.655778">
 *          https://doi.org/10.1109/ICDE.1998.655778</a>.
 * </ol>
 * 
 * @author Brankica Bratić, Zoltán Gellér
 * @version 2025.03.05.
 * @see Representation
 */
public class DFTRepresentation implements Representation {
	
    private static final long serialVersionUID = 1L;

    /**
     * DFT coefficients.
     */
    private DFTCoefficient[] dft;

    /**
     * Original time series dimensionality.
     */
    private int originalSize;
	
    /**
     * Creates a new {@code DFT} representation of given dimensionality.
     * 
     * @param series time series
     * @param d      representation dimensionality (must be even)
     */
    public DFTRepresentation(final TimeSeries series, int d) {
        this(series.getYValues(), d);
    }

    /**
     * Creates a new {@code DFT} representation of given dimensionality.
     * 
     * @param values time series values
     * @param d      representation dimensionality (must be even)
     */
    public DFTRepresentation(final double[] values, int d) {
        createDFT(values, d);
    }
	
	/**
	 * Creates a new {@code DFT} representation for parameters given in string {@code argsStr}.
	 * @param argsStr arguments for DFT instantiation
	 * @throws IOException 
	 */
/*
	public DFTRepresentation(String argsStr) throws IOException {
		
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
			throw new IllegalArgumentException("Illegal arguments. DFTRepresentation representation arguments:\n" +
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
		createDFT(timeSerie.getData().getYValues(), d);
	}
*/
	
    /**
     * Class that stores information about one DFT coefficient.
     * 
     * @author Brankica Bratic
     * @version 04.03.2017.
     */
    public static class DFTCoefficient {
    
        /**
         * Real component of DFTRepresentation coefficient.
         */
        private double real;
    
        /**
         * Imaginary component of DFTRepresentation coefficient.
         */
        private double imag;
    
        public DFTCoefficient(double real, double imag) {
            this.real = real;
            this.imag = imag;
        }
    
        /**
         * Returns real component of DFTRepresentation coefficient.
         * 
         * @return real component of DFTRepresentation coefficient
         */
        public double getReal() {
            return real;
        }
    
        /**
         * Sets real component of DFTRepresentation coefficient.
         * 
         * @param real real component of DFTRepresentation coefficient
         */
        public void setReal(double real) {
            this.real = real;
        }
    
        /**
         * Returns imaginary component of DFTRepresentation coefficient.
         * 
         * @return imaginary component of DFTRepresentation coefficient
         */
        public double getImag() {
            return imag;
        }
    
        /**
         * Sets imaginary component of DFTRepresentation coefficient.
         * 
         * @param imag imaginary component of DFTRepresentation coefficient
         */
        public void setImag(double imag) {
            this.imag = imag;
        }
    }
	
    /**
     * Returns {@code DFT} coefficients.
     * 
     * @return {@code DFT} coefficients
     */
    public DFTCoefficient[] getDFT() {
        return dft;
    }

    @Override
    public double getValue(double x) {
        
        double value = 0;
        double coeffLength = dft.length;

        for (int k = 0; k < coeffLength; k++) {

            DFTCoefficient coeff = dft[k];

            double real = coeff.getReal();
            double imag = k == 0 ? 0 : coeff.getImag();

            double angle = 2 * Math.PI * k * x / originalSize;
            value += real * Math.cos(angle) - imag * Math.sin(angle);

            // Conjugate symmetry.

            int k2;
            
            // If k is zero and length of original time series is even,
            // then the coefficient N/2 is stored in imaginary part of coefficient 0.
            if (k == 0 && originalSize % 2 == 0) {
                
                k2 = originalSize / 2;
                real = coeff.getImag();
                imag = 0;
                
            } else {
                
                k2 = originalSize - k;
                real = coeff.getReal();
                imag = -coeff.getImag();
                
            }
            
            if (k != 0 || originalSize % 2 == 0) {
                double angle2 = 2 * Math.PI * k2 * x / originalSize;
                value += real * Math.cos(angle2) - imag * Math.sin(angle2);
            }
        }

        return value / Math.sqrt(originalSize);
        
    }

    @Override
    public Object[] getRepresentation() {
        
        Object[] repr = new Object[2 * dft.length];
        
        for (int i = 0; i < dft.length; i++) {
            
            DFTCoefficient coeff = dft[i];
            repr[2 * i] = coeff.getReal();
            repr[2 * i + 1] = coeff.getImag();
            
        }
        
        return repr;
    }
	
    @Override
    public String toString() {
        return String.join(" ",
                Arrays.asList(getRepresentation()).stream().map(x -> x.toString()).collect(Collectors.toList()));
    }
	
    /**
     * Creates a new {@code DFT} representation of given dimensionality.
     * 
     * @param values time series values
     * @param d      representation dimensionality (must be even)
     */
    private void createDFT(final double[] values, int d) {
        
        if (d < 1 || d > values.length) {
            throw new IllegalArgumentException(
                    "Representation dimensionality must be between 1 and " + values.length + ".");
        }

        if (d % 2 != 0)
            throw new IllegalArgumentException("Representation dimensionality must be even.");

        int m = d / 2; // DFT coefficients count

        dft = new DFTCoefficient[m];
        originalSize = values.length;

        double factor = 1 / Math.sqrt(values.length);

        for (int k = 0; k < m; k++) {
            
            double realCoeff = 0;
            double imagCoeff = 0;

            for (int t = 0; t < values.length; t++) {
                
                double angle = 2 * Math.PI * t * k / values.length;
                double pointValue = values[t];
                realCoeff += pointValue * Math.cos(angle);
                imagCoeff += -pointValue * Math.sin(angle);
                
            }

            dft[k] = new DFTCoefficient(factor * realCoeff, factor * imagCoeff);
            
        }

        /*
         * If length of time series is even, coefficient 0 and coefficient N/2 are
         * real-valued.In order to increase representation precision, we will store real
         * part of coefficient N/2 in a imaginary part of coefficient 0 (since we
         * already know that imaginary part of coefficient 0 is 0, we do not have to use
         * this space just to store 0).
         */
        if (originalSize % 2 == 0) {
            
            int k = originalSize / 2; // N/2 element
            double realCoeff = 0;
            
            for (int t = 0; t < values.length; t++) {
                
                double angle = 2 * Math.PI * t * k / values.length;
                double pointValue = values[t];
                realCoeff += pointValue * Math.cos(angle);
                
            }

            // Store real part of coefficient N/2 in imaginary part of coefficient 0.
            dft[0].setImag(factor * realCoeff); 
        }
        
    }
    
}

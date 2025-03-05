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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//import fap.core.console.Console;
import fap.core.data.DataPoint;
import fap.core.data.TimeSeries;
import fap.core.data.Representation;
import fap.util.ExplicitLine;

/**
 * Piecewise Linear Approximation (PLA)
 * <p>
 * Representation of time series with piecewise linear segments.
 * <p>
 * Implements three algorithms:<br>
 * <ul>
 * <li>sliding window</li>
 * <li>top-down</li>
 * <li>bottom-up</li>
 * </ul>
 * with and without linear regression.
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
 * @version 2025.03.05.
 * @see Representation
 */
public abstract class AbstractPLARepresentation<SegmentType> implements Representation {
	
    private static final long serialVersionUID = 1L;
    
    /**
     * Different algorithms to compute the PLA representation.
     */
    public static final int SLIDING_WINDOW = 0;
    public static final int TOP_DOWN = 1;
    public static final int BOTTOM_UP = 2;
	
    /**
     * Creates a new {@code PLA} representation.
     * 
     * @param series    time series
     * @param maxError  maximum error bound (must be non-negative)
     * @param algorithm type of the algorithm
     */
    public AbstractPLARepresentation(final TimeSeries series, double maxError, int algorithm) {
        this(series.getYValues(), maxError, algorithm);
    }
	
    /**
     * Creates a new {@code PLA} representation.
     * 
     * @param values    time series values
     * @param maxError  maximum error bound (must be non-negative)
     * @param algorithm type of the algorithm
     */
    public AbstractPLARepresentation(final double[] values, double maxError, int algorithm) {
        createPLA(values, maxError, algorithm);
    }
	
	/**
	 * Creates a new {@code PLA} representation for parameters given in string {@code argsStr}.
	 * @param argsStr arguments for AbstractPLARepresentation instantiation
	 * @throws IOException 
	 */
/*
	public AbstractPLARepresentation(String argsStr) throws IOException {
		
		String[] args = argsStr.split(Console.ARGS_DELIMITER);
		
		String dataSet = null;
		char dataSetSeparator = ' ';
		boolean dataSetHasXValue = false;
		double maxError = -1;
		int algorithm = -1;
		
		int ts = -1;
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(Console.ARG_PREFIX+"ds")) {
				dataSet = args[i+1];
			} else if (args[i].equals(Console.ARG_PREFIX+"err")) {
				try {
					maxError = Double.parseDouble(args[i+1]);
				} catch (Exception e) {
					throw new IllegalArgumentException("Maximal error must be real number.");
				}
			} else if (args[i].equals(Console.ARG_PREFIX+"alg")) {
				try {
					algorithm = Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					throw new IllegalArgumentException("Algorithm value must be 0, 1 or 2 (0 - sliding window, 1 - top down, 2 - bottom up).");
				}
				if (algorithm < 0 && algorithm > 2) {
					throw new IllegalArgumentException("Algorithm value must be 0, 1 or 2 (0 - sliding window, 1 - top down, 2 - bottom up).");
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
	
		if (dataSet == null || maxError < 0 || algorithm < 0 || ts < 0) {
			throw new IllegalArgumentException("Illegal arguments. PLA representation arguments:\n" +
											   "   " + Console.ARGS_DELIMITER + "ds   (required argument) - url of dataset file\n" +
											   "   " + Console.ARGS_DELIMITER + "ts   (required argument) - index of time series inside of dataset\n" +
											   "   " + Console.ARGS_DELIMITER + "err  (required argument) - maximal error\n" +
											   "   " + Console.ARGS_DELIMITER + "alg  (required argument) - algorithm (0 - sliding window, 1 - top down, 2 - bottom up)\n" +
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
		createPLA(timeSerie.getData().getYValues(), maxError, algorithm);
	}
*/
	
    protected static class AbstractSegment {
    
        public DataPoint leftPoint;
        public DataPoint rightPoint;
    
        public AbstractSegment(DataPoint leftPoint, DataPoint rightPoint) {
            this.leftPoint = leftPoint;
            this.rightPoint = rightPoint;
        }
        
    }
	
    /**
     * Approximation error of the segment [a,b] calculated with Euclidean distance.
     * 
     * @param values     array of original time series values
     * @param approxLine approximation line
     * @param a          index of the start point
     * @param b          index of the end point
     * @return approximation error calculated with Euclidean distance of the segment
     *         [a,b]
     */
    private double calcError(final double[] values, ExplicitLine approxLine, int a, int b) {
        
        double error = 0.0;
        
        for (int i = a; i <= b; i++)
            error += Math.pow(values[i] - approxLine.getY(i), 2);
        
        return Math.sqrt(error);
        
    }
	
    @Override
    public String toString() {
        return String.join(" ",
                Arrays.asList(getRepresentation()).stream().map(x -> x.toString()).collect(Collectors.toList()));
    }
	
    /**
     * Creates a new {@code PLA} representation.
     * 
     * @param values    time series values
     * @param maxError  maximum error bound (must be non-negative)
     * @param algorithm type of the algorithm
     */
    private void createPLA(final double[] values, double maxError, int algorithm) {
        
        if (maxError < 0) // maxError must be non-negative
            throw new IllegalArgumentException("maxError must be >=0" + ".");

        List<AbstractSegment> pla;
        
        switch (algorithm) {
        
        case SLIDING_WINDOW:
            pla = slidingWindow(values, maxError);
            break;
            
        case TOP_DOWN:
            List<Double> xValues = new ArrayList<Double>(values.length);
            List<Double> yValues = new ArrayList<Double>(values.length);
            for (int i = 0; i < values.length; i++) {
                xValues.add((double) i);
                yValues.add(values[i]);
            }
            pla = topDown(xValues, yValues, maxError);
            break;
            
        case BOTTOM_UP:
            if (values.length % 2 != 0)
                throw new IllegalArgumentException("In case of bottom-up algorithm, time series length cannot be odd.");
            pla = bottomUp(values, maxError);
            break;
            
        default:
            throw new IllegalArgumentException("Algorithm type is invalid.");
            
        }

        setPLA(pla);
    }
	
    /**
     * Computes the {@code PLA} representation using the sliding window algorithm.
     * 
     * @param values   array of values upon which representation will be created
     * @param maxError upper bound of the segment error
     * @return {@code PLA} representation
     */
    private List<AbstractSegment> slidingWindow(final double[] values, double maxError) {

        List<AbstractSegment> pla = new ArrayList<AbstractSegment>();

        List<Double> xValues = new ArrayList<Double>(values.length);
        List<Double> yValues = new ArrayList<Double>(values.length);

        for (int i = 0; i < values.length; i++) {
            xValues.add((double) i);
            yValues.add(values[i]);
        }

        int i = 0;
        double mean = 1;
        while (i < values.length) {

            int segLen = (int) Math.min(mean, values.length - i);
            ExplicitLine line = getLine(xValues, yValues, i, i + segLen - 1);

            if (calcError(values, line, i, i + segLen - 1) > maxError)
                
                do {
                    segLen--;
                    line = getLine(xValues, yValues, i, i + segLen - 1);
                } while (calcError(values, line, i, i + segLen - 1) > maxError);
            
            else
                
                if (i + segLen < values.length) {
                    
                    ExplicitLine prevLine = line;
                    
                    do {
                        prevLine = line;
                        segLen++;
                        line = getLine(xValues, yValues, i, i + segLen - 1);
                    } while (calcError(values, line, i, i + segLen - 1) < maxError && i + segLen < values.length);

                    if (i + segLen < values.length) {
                        line = prevLine;
                        segLen--;
                    }
                    
                }

            AbstractSegment segment = new AbstractSegment(new DataPoint(i, line.getY(i)),
                    new DataPoint(i + segLen - 1, line.getY(i + segLen - 1)));
            pla.add(segment);
            i += segLen;

            int prevRIndex = pla.size() <= 1 ? -1 : (int) pla.get(pla.size() - 2).rightPoint.getX();
            int rIndex = (int) pla.get(pla.size() - 1).rightPoint.getX();
            mean = (mean * (pla.size() - 1) + (rIndex - prevRIndex)) / pla.size(); // update mean
        }

        return pla;
    }
	
    /**
     * Computes the {@code PLA} representation using the top-down algorithm.
     * 
     * @param xValues  array of x values of the points upon which representation
     *                 will be created
     * @param yValues  array of y values of the points upon which representation
     *                 will be created
     * @param maxError upper bound of the segment error
     * @return {@code PLA} representation
     */
    private List<AbstractSegment> topDown(List<Double> xValues, List<Double> yValues, double maxError) {

        List<AbstractSegment> pla = new ArrayList<AbstractSegment>();

        double[] yValuesArray = new double[yValues.size()];
        List<Double> leftXValues = new ArrayList<Double>(xValues.size());
        List<Double> leftYValues = new ArrayList<Double>(xValues.size());
        List<Double> rightXValues = new ArrayList<Double>(xValues.size());
        List<Double> rightYValues = new ArrayList<Double>(xValues.size());

        for (int i = 0; i < xValues.size(); i++) {
            rightXValues.add(xValues.get(i));
            rightYValues.add(yValues.get(i));
            yValuesArray[i] = yValues.get(i);
        }

        double bestError = Double.MAX_VALUE;
        double bestLeftError = Double.MAX_VALUE;
        double bestRightError = Double.MAX_VALUE;
        List<Double> bestLeftXValues = new ArrayList<Double>(xValues.size());
        List<Double> bestLeftYValues = new ArrayList<Double>(xValues.size());
        List<Double> bestRightXValues = new ArrayList<Double>(xValues.size());
        List<Double> bestRightYValues = new ArrayList<Double>(xValues.size());

        for (int i = 0; i < xValues.size() - 1; i++) {
            
            leftXValues.add(rightXValues.remove(0));
            leftYValues.add(rightYValues.remove(0));

            ExplicitLine leftLine = getLine(leftXValues, leftYValues);
            ExplicitLine rightLine = getLine(rightXValues, rightYValues);

            double leftError = calcError(yValuesArray, leftLine, 0, i);
            double rightError = calcError(yValuesArray, rightLine, i + 1, yValuesArray.length - 1);
            double error = Math.sqrt(Math.pow(leftError, 2) + Math.pow(rightError, 2));

            if (error < bestError) {
                
                bestError = error;
                bestLeftError = leftError;
                bestRightError = rightError;
                bestLeftXValues.clear();
                bestLeftXValues.addAll(leftXValues);
                bestLeftYValues.clear();
                bestLeftYValues.addAll(leftYValues);
                bestRightXValues.clear();
                bestRightXValues.addAll(rightXValues);
                bestRightYValues.clear();
                bestRightYValues.addAll(rightYValues);
                
            }
            
        }

        if (bestLeftError < maxError) {
            
            int leftX = bestLeftXValues.get(0).intValue();
            int rightX = bestLeftXValues.get(bestLeftXValues.size() - 1).intValue();
            ExplicitLine line = getLine(bestLeftXValues, bestLeftYValues);
            AbstractSegment segment = new AbstractSegment(new DataPoint(leftX, line.getY(leftX)),
                    new DataPoint(rightX, line.getY(rightX)));
            pla.add(segment);
            
        } else
            pla.addAll(topDown(bestLeftXValues, bestLeftYValues, maxError));

        if (bestRightError < maxError) {
            
            int leftX = bestRightXValues.get(0).intValue();
            int rightX = bestRightXValues.get(bestRightXValues.size() - 1).intValue();
            ExplicitLine line = getLine(bestRightXValues, bestRightYValues);
            AbstractSegment segment = new AbstractSegment(new DataPoint(leftX, line.getY(leftX)),
                    new DataPoint(rightX, line.getY(rightX)));
            pla.add(segment);
            
        } else
            pla.addAll(topDown(bestRightXValues, bestRightYValues, maxError));

        return pla;
    }
	
    /**
     * Computes the {@code PLA} representation using the bottom-up window algorithm.
     * 
     * @param values   array of values upon which representation will be created
     * @param maxError upper bound of the segment error
     * @return {@code PLA} representation
     */
    private List<AbstractSegment> bottomUp(final double[] values, double maxError) {

        List<AbstractSegment> pla = new ArrayList<AbstractSegment>();

        List<Double> errors = new ArrayList<Double>(values.length / 2 - 1);
        List<Double> xValues = new ArrayList<Double>();
        List<Double> yValues = new ArrayList<Double>();
        for (int i = 0; i < values.length; i++) {
            xValues.add((double) i);
            yValues.add(values[i]);
        }

        for (int i = 0; i < values.length; i += 2) {
            
            ExplicitLine line = getLine(xValues, yValues, i, i + 1);
            AbstractSegment segment = new AbstractSegment(new DataPoint(i, line.getY(i)),
                    new DataPoint(i + 1, line.getY(i + 1)));
            pla.add(segment);
            
            if (i < values.length - 2)
                errors.add(calcError(values, getLine(xValues, yValues, i, i + 3), i, i + 3));
            
        }

        while (true) {
            
            double minError = Double.MAX_VALUE;
            int minErrorIndex = -1;
            for (int i = 0; i < errors.size(); i++) {
                
                double error = errors.get(i);
                
                if (error < minError) {
                    minError = error;
                    minErrorIndex = i;
                }
                
            }

            if (minError >= maxError)
                break;

            pla.remove(minErrorIndex);
            int startX = (int) pla.get(minErrorIndex).leftPoint.getX();
            int endX = (int) pla.get(minErrorIndex).rightPoint.getY();
            ExplicitLine newLine = getLine(xValues, yValues, startX, endX);
            AbstractSegment segment = pla.get(minErrorIndex);
            segment.leftPoint = new DataPoint(startX, newLine.getY(startX));
            segment.rightPoint = new DataPoint(endX, newLine.getY(endX));

            errors.remove(minErrorIndex);
            
            if (minErrorIndex > 0) {
                int startXOfPrevious = (int) pla.get(minErrorIndex - 1).leftPoint.getX();
                errors.set(minErrorIndex - 1,
                        calcError(values, getLine(xValues, yValues, startXOfPrevious, endX), startXOfPrevious, endX));
            }
            
            if (minErrorIndex < errors.size()) {
                int endXOfNext = (int) pla.get(minErrorIndex + 1).rightPoint.getX();
                errors.set(minErrorIndex,
                        calcError(values, getLine(xValues, yValues, startX, endXOfNext), startX, endXOfNext));
            }
            
            if (pla.size() == 1)
                break;
            
        }

        return pla;
    }
		
    /**
     * Calculates explicit line equation based on given points.
     * 
     * @param xValues x values of the points
     * @param yValues y values of the points
     * @return explicit line that is created in accordance with given points
     */
    protected abstract ExplicitLine getLine(List<Double> xValues, List<Double> yValues);

    /**
     * Returns PLA representation.
     * 
     * @return PLA representation
     */
    public abstract List<SegmentType> getPLA();

    /**
     * Sets PLA representation.
     * 
     * @param pla PLA representation to be set
     */
    protected abstract void setPLA(List<AbstractSegment> pla);
	
    /**
     * Calculates explicit line equation based on the points defined by the given
     * start and end index.
     * 
     * @param xValues    x values of the points
     * @param yValues    y values of the points
     * @param startIndex start of the range
     * @param endIndex   end of the range
     * @return explicit line that is created in accordance with given points, start
     *         and end index
     */
    private ExplicitLine getLine(List<Double> xValues, List<Double> yValues, int startIndex, int endIndex) {
        return getLine(xValues.subList(startIndex, endIndex + 1), yValues.subList(startIndex, endIndex + 1));
    }
    
}

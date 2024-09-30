package fap.representation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fap.core.data.TimeSeries;
import fap.core.data.Representation;

/**
 * Adaptive Piecewise Constant Approximation (APCA) representation.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> E. Keogh, K. Chakrabarti, M. Pazzani, S. Mehrotra, Locally adaptive
 *       dimensionality reduction for indexing large time series databases, in: Proc.
 *       2001 ACM SIGMOD Int. Conf. Manag. Data, ACM, New York, NY, USA, 2001: pp.
 *       151–162. 
 *       <a href="https://doi.org/10.1145/375663.375680">
 *          https://doi.org/10.1145/375663.375680</a>.
 * </ol>
 * 
 * @author Miklos Kalozi, Zoltan Geller, Brankica Bratic
 * @version 2024.09.14.
 * @see Representation
 */
public class APCARepresentation implements Representation {
	
    private static final long serialVersionUID = 1L;
    
    /**
     * The Adaptive Piecewise Constant Approximation (APCA) representation. Default
     * value is an empty list.
     */
    private ArrayList<APCASegment> apca = new ArrayList<APCASegment>();

    /**
     * Creates a new {@code APCA} representation of given dimensionality.
     * 
     * @param series time series
     * @param d      representation dimensionality (must be even)
     */
    public APCARepresentation(final TimeSeries series, int d) {
        this(series.getYValues(), d);
    }
	
    /**
     * Creates a new {@code APCA} representation of given dimensionality.
     * 
     * @param values time series values
     * @param d      representation dimensionality (must be even)
     */
    public APCARepresentation(final double[] values, int d) {
        createAPCA(values, d);
    }
	
	/**
	 * Creates a new {@code APCA} representation for parameters given in string {@code argsStr}.
	 * @param argsStr arguments for APCA instantiation
	 * @throws IOException 
	 */
/*
	public APCARepresentation(String argsStr) throws IOException {
		
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
			throw new IllegalArgumentException("Illegal arguments. APCARepresentation representation arguments:\n" +
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
		createAPCA(timeSerie.getData().getYValues(), d);
	}
*/
	
    /**
     * Class that stores information about one segment of APCA representation.
     * 
     * @author Brankica Bratic
     * @version 04.03.2017.
     */
    public static class APCASegment {
    
        /**
         * Mean value of data points in this segment.
         */
        private float cv;
		
        /**
         * The index of the right end point of this segment.
         */
        private int cr;

        public APCASegment(float cv, int cr) {
            this.cv = cv;
            this.cr = cr;
        }
		
        /**
         * Returns mean value of data points in this segment.
         * 
         * @return mean value of data points in this segment
         */
        public float getCv() {
            return cv;
        }
		
        /**
         * Sets mean value of data points in this segment.
         * 
         * @param cv mean value of data points in this segment.
         */
        public void setCv(float cv) {
            this.cv = cv;
        }
		
        /**
         * Returns index of the right end point of this segment.
         * 
         * @return index of the right end point of this segment
         */
        public int getCr() {
            return cr;
        }
		
        /**
         * Sets the index of the right end point of this segment.
         * 
         * @param cr index of the right end point of this segment
         */
        public void setCr(int cr) {
            this.cr = cr;
        }
    }
	
    /**
     * Class that stores value and index of one Haar coefficient.
     * 
     * @author Zoltan Geller, Brankica Bratic
     * @version 2017.04.03.
     */
	private static class HaarCoefficient implements Comparable<HaarCoefficient> {
		
        /**
         * Value of the Haar coefficient.
         */
        double hcoef;

        /**
         * Index of the corresponding Haar coefficient.
         */
        int index;

        public HaarCoefficient(double hcoef, int index) {
            this.hcoef = hcoef;
            this.index = index;
        }

        @Override
        public int compareTo(HaarCoefficient o) {
            return Double.compare(o.hcoef, hcoef);
        }
    }
	
    /**
     * Returns APCA representation.
     * 
     * @return APCA representation
     */
    public List<APCASegment> getAPCA() {
        return apca;
    }
	
    /**
     * Returns the number of segments.
     * 
     * @return the number of segments
     */
    public int getSegmentsCount() {
        return apca.size();
    }

    @Override
    public double getValue(double x) {

        if (x < 0 || x > apca.get(apca.size() - 1).getCr())
            return OUTBOUND_VALUE;

        // binary search
        int left = 0;
        int right = apca.size() - 1;

        while (left < right) {
            
            int middle = (right + left) / 2;
            
            if (apca.get(middle).getCr() < x)
                left = middle + 1;
            else
                right = middle;
            
        }

        return apca.get(left).getCv();
    }

    @Override
    public Object[] getRepresentation() {
        
        Object[] repr = new Object[2 * apca.size()];
        
        for (int i = 0; i < apca.size(); i++) {
            APCASegment seg = apca.get(i);
            repr[2 * i] = seg.getCv();
            repr[2 * i + 1] = seg.getCr();
        }
        
        return repr;
    }
	
    @Override
    public String toString() {
        return String.join(" ",
                Arrays.asList(getRepresentation()).stream().map(x -> x.toString()).collect(Collectors.toList()));
    }
	
    /**
     * Creates a new {@code APCA} representation of given dimensionality.
     * 
     * @param values time series values
     * @param d      representation dimensionality (must be even)
     */
    private void createAPCA(final double[] values, int d) {
        
        if (d < 1 || d > 2 * values.length)
            throw new IllegalArgumentException(
                    "Representation dimensionality must be between 1 and " + (2 * values.length) + ".");

        if (d % 2 != 0)
            throw new IllegalArgumentException("Representation dimensionality must be even.");

        // calculating Haar coefficients
        double[] haar = createHaar(values); 

        // calculating normalized absolute Haar coefficients
        HaarCoefficient[] nHaar = normalizeHaar(haar);

        // sorting normalized Haar coefficients
        Arrays.sort(nHaar); 

        // number of APCA segments
        int m = d / 2; 

        int[] bestCoefsIndices = new int[m];
        for (int i = 0; i < m; i++)
            bestCoefsIndices[i] = nHaar[i].index;

        Arrays.sort(bestCoefsIndices);

        // create APCA segments based on best Haar coefficients
        apca.add(new APCASegment(Float.NaN, haar.length - 1));
        
        for (int i : bestCoefsIndices) {
            
            if (i == 0)
                continue;
            
            int resolution = (int) (Math.log10(i) / Math.log10(2));
            int firstSegmentIndex = (int) Math.pow(2, resolution);
            int segmentIndex = i - firstSegmentIndex;
            // firstSegmentIndex at the same time tells how much Haar coefficients exist in current resolution
            int segmentSize = haar.length / firstSegmentIndex; 
            int startIndex = segmentIndex * segmentSize;
            int endIndex = startIndex + segmentSize - 1;
            int prevCr = -1;
            int indexOfAPCASegment = 0;
            
            for (APCASegment segment : apca) {
                
                if (prevCr < startIndex && segment.getCr() >= endIndex)
                    break;
                
                indexOfAPCASegment++;
                prevCr = segment.getCr();
                
            }

            // corresponding APCA segment is found - so now the divisions are made
            if (indexOfAPCASegment > 0 && apca.get(indexOfAPCASegment - 1).getCr() + 1 != startIndex) { 
                apca.add(indexOfAPCASegment, new APCASegment(Float.NaN, startIndex - 1));
                indexOfAPCASegment++;
            }
            
            if (apca.get(indexOfAPCASegment).getCr() != endIndex)
                apca.add(indexOfAPCASegment, new APCASegment(Float.NaN, endIndex));
            
            apca.add(indexOfAPCASegment, new APCASegment(Float.NaN, startIndex + segmentSize / 2 - 1));
        }

        // fill APCA segments with their average values
        int startIndex = 0;
        
        for (APCASegment segment : apca) {
            
            float sum = 0;
            int count = 0;
            
            for (int i = startIndex; i <= segment.getCr(); i++) {

                // this is possible since we added zeros at the end of data in order to get
                // length that is integer power of two
                if (i >= values.length)
                    break; 

                sum += values[i];
                count++;
            }
            
            float avg = count == 0 ? 0 : sum / count;
            segment.setCv(avg);
            startIndex = segment.getCr() + 1;
            
        }

        // merge segmentCount minus m pairs of successive APCA segments with lowest mutual difference
        while (apca.size() - m > 0) {
            
            int lowestDiffIndex = 0;
            float lowestDiff = Float.MAX_VALUE;
            
            for (int i = 0; i < apca.size() - 1; i++) {
                
                float diff = Math.abs(apca.get(i).getCv() - apca.get(i + 1).getCv());
                
                if (diff < lowestDiff) {
                    lowestDiff = diff;
                    lowestDiffIndex = i;
                }
                
            }

            // calculate average value for merged interval
            int start = lowestDiffIndex > 0 ? apca.get(lowestDiffIndex - 1).getCr() + 1 : 0; 
            int end = apca.get(lowestDiffIndex + 1).getCr();
            float sum = 0;
            int count = 0;
            
            for (int i = start; i <= end; i++) {
                
                if (i >= values.length)
                    break;
                
                sum += values[i];
                count++;
                
            }
            
            apca.get(lowestDiffIndex + 1).setCv(count > 0 ? sum / count : 0);
            apca.remove(lowestDiffIndex);
            
        }
        
    }
	
    /**
     * Creates Haar coefficients for given data.
     * 
     * @param data data for which Haar coefficients are calculated
     * @return Haar coefficients
     */
    private double[] createHaar(double[] timeSeries) {
        // getting Haar coeficients
        return new HaarRepresentation(timeSeries).getHaar();
    }
	
    /**
     * Creates an array of normalized absolute Haar coefficients.
     * 
     * @param haar array of Haar coefficients
     * @return array of normalized absolute Haar coefficients.
     */
    private HaarCoefficient[] normalizeHaar(double[] haar) {

        HaarCoefficient[] nHaar = new HaarCoefficient[haar.length];

        // first two coefficients are untouched
        nHaar[0] = new HaarCoefficient(haar[0], 0);
        nHaar[1] = new HaarCoefficient(haar[1], 1);

        int index = 2;
        int resLimit = (int) (Math.log10(nHaar.length) / Math.log10(2));
        
        for (int resolution = 1; resolution < resLimit; resolution++) {
            
            double tmp = Math.pow(2, resolution / 2f);
            int limit = (int) Math.pow(2, resolution);
            
            for (int i = 0; i < limit; i++) {
                
                double hcoef = haar[index];
                nHaar[index] = new HaarCoefficient(Math.abs(hcoef / tmp), index);
                index++;
                
            }
            
        }

        return nHaar;
    }
    
}

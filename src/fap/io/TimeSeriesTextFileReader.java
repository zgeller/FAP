package fap.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import fap.core.data.DataPoint;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.input.CSVDataPointFactory;
import fap.core.input.CSVParser;

/**
 * Reads time series from a text file.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 * @see TimeSeriesTextIOHelper
 */
public class TimeSeriesTextFileReader extends TimeSeriesTextIOHelper {

    /**
     * Constructs a new text file reader without a filename, comma as the character
     * that separates values, and without x-coordinates.
     */
    public TimeSeriesTextFileReader() {
    }

    /**
     * Constructs a new text file reader with the specified filename and comma as
     * the character that separates values.
     * 
     * @param fname the filename
     */
    public TimeSeriesTextFileReader(String fname) {
        super(fname);
    }

    /**
     * Constructs a new text file reader with the specified filename, separator
     * character, and without x-coordinates.
     * 
     * @param fname     the filename
     * @param separator the character which separates the values
     */
    public TimeSeriesTextFileReader(String fname, char separator) {
        super(fname, separator);
    }

    /**
     * Constructs a new text file reader with the specified filename, separator
     * character, and indicating whether the input contains x-coordinates.
     * 
     * @param fname     the filename
     * @param separator the character which separates the values
     * @param hasXValue indicates if the input contains x-coordinates of points
     */
    public TimeSeriesTextFileReader(String fname, char separator, boolean hasXValue) {
        super(fname, separator, hasXValue);
    }

    /**
     * Loads the time series from a file.
     * 
     * @return a new {@link Dataset} containing the time series loaded from the file
     * @throws IOException
     */
    public Dataset load() throws IOException {

        Dataset dataset = new Dataset();

        try (BufferedReader input = new BufferedReader(new FileReader(new File(fname)))) {

            String line = input.readLine();

            while (line != null) {

                CSVParser parser = new CSVParser(line, separator);

                // label
                if (parser.hasNextValue()) {

                    String labelStr = parser.nextValue();

                    double label = 0;

                    try {
                        label = Double.parseDouble(labelStr);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid number format: " + labelStr);
                    }

                    // data
                    line = line.substring(parser.getPosition());

                    CSVDataPointFactory dpf = new CSVDataPointFactory(line, separator, hasXValue);

                    TimeSeries ts = new TimeSeries(label);
                    while (dpf.hasNextPoint()) {
                        DataPoint dp = dpf.nextPoint();
                        ts.add(dp);
                    }

                    dataset.add(ts);

                }

                line = input.readLine();

            }

        }

        return dataset;

    }

}

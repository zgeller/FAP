package fap.io;

import java.io.IOException;
import java.io.PrintWriter;

import fap.core.data.DataPoint;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;

/**
 * Writes time series into a text file.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 * @see TimeSeriesTextIOHelper
 */
public class TimeSeriesTextFileWriter extends TimeSeriesTextIOHelper {

    /**
     * Constructs a new text file writer without a filename, comma as the character
     * that separates values, and without x-coordinates.
     */
    public TimeSeriesTextFileWriter() {
    }

    /**
     * Constructs a new text file reader with the specified filename and comma as
     * the character that separates values.
     * 
     * @param fname the filename
     */
    public TimeSeriesTextFileWriter(String fname) {
        super(fname, ' ', false);
    }

    /**
     * Constructs a new text file writer with the specified filename, separator
     * character, and without x-coordinates.
     * 
     * @param fname     the filename
     * @param separator the character which separates the values
     */
    public TimeSeriesTextFileWriter(String fname, char separator) {
        super(fname, separator, false);
    }
    
    /**
     * Constructs a new text file writer with the specified filename, separator
     * character, and indicating whether the output should contain x-coordinates.
     * 
     * @param fname     the filename
     * @param separator the character which separates the values
     * @param hasXValue indicates whether the output should contain x-coordinates of
     *                  points
     */
    public TimeSeriesTextFileWriter(String fname, char separator, boolean hasXValue) {
        super(fname, separator, hasXValue);
    }

    /**
     * Writes time series to a text file.
     * 
     * @param dataset {@code ArrayList} of {@code TimeSeries}
     * @throws IOException
     */
    public void write(Dataset dataset) throws IOException {

        try (PrintWriter writer = new PrintWriter(fname)) {

            for (TimeSeries series : dataset) {

                String line = String.valueOf(series.getLabel());

                for (DataPoint dp : series) {

                    if (hasXValue)
                        line += separator + String.valueOf(dp.getX());

                    line += separator + String.valueOf(dp.getY());

                }

                writer.println(line);

            }

            writer.flush();
            writer.close();

        } catch (IOException e) {
            throw e;
        }

    }

}

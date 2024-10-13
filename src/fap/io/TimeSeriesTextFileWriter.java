/*   
 * Copyright 2024 Zoltán Gellér
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

package fap.io;

import java.io.IOException;
import java.io.PrintWriter;

import fap.core.data.DataPoint;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;

/**
 * Writes time series into a text file.
 * 
 * @author Zoltán Gellér
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

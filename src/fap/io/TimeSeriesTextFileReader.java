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
 * @author Zoltán Gellér
 * @version 2025.03.06.
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
                        throw new IllegalArgumentException("Invalid number format: " + labelStr, e);
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

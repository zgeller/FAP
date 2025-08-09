/*   
 * Copyright 2015 Zoltán Gellér
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

/**
 * Helper class for IO operations.
 * 
 * @author Zoltán Gellér
 * @version 2015.10.27.
 */
public class TimeSeriesTextIOHelper {

    /**
     * Filename.
     */
    protected String fname;

    /**
     * Character used to separate values. Default value is comma.
     */
    protected char separator = ',';

    /**
     * Tells if list of input values contain x-coordinates of points. Default value
     * is {@code false}.
     */
    protected boolean hasXValue = false;

    /**
     * Creates a new {@code TimeSeriesTextIOHelper} with default field values.
     */
    public TimeSeriesTextIOHelper() {
    }

    /**
     * Creates a new {@code TimeSeriesTextIOHelper} object with the given
     * parameters.
     * 
     * @param fname     - the filename
     * @param separator - the character which separates the values
     * @param hasXValue - tells if list of input values contains x-coordinates of
     *                  points
     */
    public TimeSeriesTextIOHelper(String fname, char separator, boolean hasXValue) {
        this.setFName(fname);
        this.setSeparator(separator);
        this.setHasXValue(hasXValue);
    }
	
    /**
     * Creates a new {@code TimeSeriesTextIOHelper} object with the given parameters
     * using the default separator.
     * 
     * @param fname     - the filename
     * @param hasXValue - tells if list of input values contains x-coordinates of
     *                  points
     */
    public TimeSeriesTextIOHelper(String fname, boolean hasXValue) {
        this.setFName(fname);
        this.setHasXValue(hasXValue);
    }

    /**
     * Creates a new {@code TimeSeriesTextIOHelper} object with the given parameters
     * using default parameter value {@code hasXValue}.
     * 
     * @param fname     - the filename
     * @param separator - the character which separates the values
     */
    public TimeSeriesTextIOHelper(String fname, char separator) {
        this.setFName(fname);
        this.setSeparator(separator);
    }
	
    /**
     * Creates a new {@code TimeSeriesTextIOHelper} object with the given parameters
     * using default parameter values for {@code separator} and {@code hasXValue}.
     * 
     * @param fname - the filename
     */
    public TimeSeriesTextIOHelper(String fname) {
        this.setFName(fname);
    }

    /**
     * Returns the filename.
     * 
     * @return the filename
     */
    public String getFName() {
        return fname;
    }
	
    /**
     * Sets the filename.
     * 
     * @param fname the fname to set
     */
    public void setFName(String fname) {
        this.fname = fname;
    }
	
    /**
     * Returns the separator character.
     * 
     * @return the separator character
     */
    public char getSeparator() {
        return separator;
    }
	
    /**
     * Sets the separator character.
     * 
     * @param separator the separator to set
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }
	
    /**
     * Returns the value of {@code hasXValue}.
     * 
     * @return the {@code hasXValue}
     */
    public boolean isHasXValue() {
        return hasXValue;
    }
	
    /**
     * Sets the value of {@code hasXValue}.
     * 
     * @param hasXValue the {@code hasXValue} to set.
     */
    public void setHasXValue(boolean hasXValue) {
        this.hasXValue = hasXValue;
    }

}

/*   
 * Copyright 2013-2024 Aleksa Todorović, Zoltán Gellér
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

package fap.core.input;

/**
 * Parses comma (or any other character)-separated values and produces values.
 * Has interface similar to Iterator.
 * 
 * @author Aleksa Todorović, Zoltán Gellér
 * @version 2013.04.04.
 */
public class CSVParser {

    /**
     * String with list of values which is parsed.
     */
    private String values;

    /**
     * Character used to separate values.
     */
    private char separator;

    /**
     * Current parsing position.
     */
    private int position;

    /**
     * Last values parsed.
     */
    private String lastValue;

    /**
     * New CSV parser for comma-separated list.
     * 
     * @param values list of comma-separated string
     */
    public CSVParser(String values) {
        this(values, ',');
    }

    /**
     * New CSV parser for separator-separated list.
     * 
     * @param values    list of separator-separated string
     * @param separator character used to separate values in list
     */
    public CSVParser(String values, char separator) {
        this.values = values;
        this.separator = separator;
        position = 0;
    }

    /**
     * Checks if there is another value available to read from values.
     * 
     * @return true if there is another value available, false otherwise
     */
    public boolean hasNextValue() {
        while ((position < values.length()) && (values.charAt(position) == separator)) {
            ++position;
        }

        int firstPosition = position;
        while ((position < values.length()) && (values.charAt(position) != separator)) {
            ++position;
        }

        if (position == firstPosition) {
            return false;
        } else {
            lastValue = values.substring(firstPosition, position);
            return true;
        }
    }

    /**
     * Returns last value successfully parsed from values.
     * 
     * @return last value successfully parsed
     */
    public String nextValue() {
        return lastValue;
    }

    /**
     * Returns the current parsing position.
     * 
     * @return the position
     */
    public int getPosition() {
        return position;
    }

}

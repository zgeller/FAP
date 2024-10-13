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

package fap.exception;

import fap.core.data.TimeSeries;
import fap.core.exception.CoreRuntimeException;

/**
 * Runtime exception thrown when the specified time series are not comparable
 * (e.g. if they are not the same length).
 * 
 * @author Zoltán Gellér
 * @version 2024.09.11.
 * @see CoreRuntimeException
 */
public class IncomparableTimeSeriesException extends CoreRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new IncomparableTimeSeries exception with {@code null} as its
     * message.
     */
    public IncomparableTimeSeriesException() {
    }

    /**
     * Constructs a new IncomparableTimeSeries exception with the specified message.
     * 
     * @param msg the message
     */
    public IncomparableTimeSeriesException(String msg) {
        super(msg);
    }

    /**
     * Checks if the specified time series are the same length and throws
     * {@code IncomparableTimeSeriesException} it they are not. Returns the length
     * of {@code series1}.
     * 
     * @param series1 the first time series
     * @param series2 the second time series
     * @return the length of {@code series1}
     */
    public static int checkLength(TimeSeries series1, TimeSeries series2) {
        return checkLength(series1.length(), series2.length());
    }

    /**
     * Checks if the specified lengths are equal and throws
     * {@code IncomparableTimeSeriesException} if they are not. Returns
     * {@code len1}.
     * 
     * @param len1 the first length
     * @param len2 the second length
     * @return {@code len1}
     */
    public static int checkLength(int len1, int len2) {

        if (len1 != len2)
            throw new IncomparableTimeSeriesException("Time series must be the same length.");

        return len1;

    }

}

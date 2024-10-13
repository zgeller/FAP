/*   
 * Copyright 2024 Aleksa Todorović, Brankica Bratić, Zoltán Gellér
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

package fap.core.data;

import java.io.Serializable;

/**
 * Declares common methods for time-series representations.
 * 
 * @author Aleksa Todorović, Brankica Bratić, Zoltán Gellér
 * @version 2024.09.14.
 * @see Serializable
 */
public interface Representation extends Serializable {

    public static final float OUTBOUND_VALUE = Float.NaN;

    /**
     * The value of the time series at timestamp {@code x} in this representation.
     * If point {@code x} is out of bounds, {@code OUTBOUND_VALUE} should returned.
     * 
     * @param x the timestamp of the data point
     * @return the value of the time series at timestamp {@code x} in this
     *         representation
     */
    public double getValue(double x);

    /**
     * Returns time series representation.
     * 
     * @return time series representation
     */
    public Object[] getRepresentation();
    
}

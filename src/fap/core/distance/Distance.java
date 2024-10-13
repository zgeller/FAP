/*   
 * Copyright 2024 Aleksa Todorović, Zoltán Gellér
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

package fap.core.distance;

import java.io.Serializable;

import fap.core.data.TimeSeries;

/**
 * Declares common methods for distance measures.
 * 
 * @author Aleksa Todorovic, Zoltán Gellér
 * @version 2024.08.17.
 * @see Serializable
 */
public interface Distance extends Serializable {

    /**
     * Computes the distance between {@code series1} and {@code series2}.
     * 
     * @param series1 the first time series
     * @param series2 the second time series
     * @return the distance between {@code series1} and {@code series2}
     */
    public double distance(TimeSeries series1, TimeSeries series2);

}

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

package fap.core.predictor;

import java.io.Serializable;

import fap.core.data.DataPoint;
import fap.core.data.Representation;

/**
 * Predictor is object used to predict some point or value of unknown series.
 * 
 * @author Aleksa Todorović, Zoltán Gellér
 * @version 2024.09.17.
 */
public interface Predictor extends Serializable {

    /**
     * Predicts value of unknown series in some point {@code x}.
     * 
     * @param x     x-coordinate for which value of series is being looked for
     * @param param used to send predictor-specific parameters to function
     * @return value of unknown series for {@code x}
     */
    public double predictValue(double x, Object param);

    /**
     * Predicts value of some special point inside unknown series. Semantics of this
     * point depends on concrete type of predictor.
     * 
     * @param param used to send predictor-specific parameters to function
     * @return unknown point, null if predictor cannot predict point
     */
    public DataPoint predictPoint(Object param);

    /**
     * Generates some representation of unknown series.
     * 
     * @param param used to send predictor-specific parameters to function
     * @return some representation of unknown series
     */
    public Representation predictRepr(Object param);

}

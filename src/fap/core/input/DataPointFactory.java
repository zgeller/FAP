/*   
 * Copyright 2024 Aleksa Todorović
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

import fap.core.data.DataPoint;

/**
 * Generic interface for classes which produce data points.
 * 
 * @author Aleksa Todorović
 * @version 1.0
 */
public interface DataPointFactory {

    /**
     * Evaluator if there is another data point which can be read.
     * 
     * @return {@code true} if there is data point available, {@code false}
     *         otherwise
     */
    public boolean hasNextPoint() throws IllegalArgumentException;

    /**
     * Returns next available data point.
     * 
     * @return next available data point.
     */
    public DataPoint nextPoint();

}

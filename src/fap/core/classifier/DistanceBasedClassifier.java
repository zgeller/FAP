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

package fap.core.classifier;

import fap.core.distance.Distance;

/**
 * Declares common methods for distance-based classifiers.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.08.
 * @see Classifier
 */
public interface DistanceBasedClassifier extends Classifier {

    /**
     * Sets the distance measure. It can be ignored by non distance-based classifiers.
     * 
     * @param distance the distance measure to set
     */
    public void setDistance(Distance distance);

    /**
     * Returns the distance measure. It can be ignored by non distance-based classifiers.
     * 
     * @return the distance measure
     */
    public Distance getDistance();
    
}

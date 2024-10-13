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

package fap.trainer;

import fap.core.classifier.Classifier;

/**
 * Declares methods for parameter setter classes.
 * 
 * @param <T> the type of the parameter to be set by this parameter setter
 * 
 * @author Zoltán Gellér
 * @version 2024.09.22.
 */
public interface Modifier<T> {
    
    /**
     * Sets the specified {@code value} of the parameter of the {@code classifier}.
     * 
     * @param classifier the classifier whose parameter is to be set
     * @param value      the value to set the parameter to
     */
    public void set(Classifier classifier, T value);
    
    /**
     * Should return {@code true} if this parameter setter affects the distance
     * measure.
     * 
     * @return {@code true} if this parameter setter affects the distance measure
     */
    public boolean affectsDistance();

}

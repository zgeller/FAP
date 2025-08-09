/*   
 * Copyright 2024-2025 Zoltán Gellér
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

package fap.core.tuner;

import java.io.Serializable;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;

/**
 * Declares common methods for classifier hyperparameter tuners.
 * 
 * @author Zoltán Gellér
 * @version 2025.04.21.
 * @see Serializable
 */
public interface Tuner extends Serializable  {

    /**
     * Tunes the hyperparameters of the specified classifier using the given
     * dataset. It should return the expected error rate.
     * 
     * @param classifier the classifier whose hyperparameters are to be tuned
     * @param dataset    the dataset
     * @return the expected error rate
     * @throws Exception if an error occurs
     */
    public double tune(Classifier classifier, Dataset dataset) throws Exception;

    /**
     * Returns the expected error rate.
     * 
     * @return the expected error rate
     */
    public double getExpectedError();
    
    /**
     * Should return {@code true} if this tuner or any of its sutuners affects the
     * distance measure.
     * 
     * @return {@code true} if this tuner or any of its subtuners affects the
     *         distance measure
     */
    public default boolean affectsDistance() {
        return false;
    }
    
}

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

package fap.core.trainer;

import java.io.Serializable;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;

/**
 * Declares common methods for classifier trainers.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.22.
 * @see Serializable
 */
public interface Trainer extends Serializable  {

    /**
     * Tunes/trains the specified classifier using the given training dataset. It
     * should return the expected error rate.
     * 
     * @param classifier the classifier that is to be trained
     * @param trainset   the training dataset
     * @return the expected error rate
     * @throws Exception if an error occurs
     */
    public double train(Classifier classifier, Dataset trainset) throws Exception;

    /**
     * Returns the expected error rate.
     * 
     * @return the expected error rate
     */
    public double getExpectedError();
    
    /**
     * Should return {@code true} if this trainer or its subtrainer affects the
     * distance measure.
     * 
     * @return {@code true} if this trainer or its subtrainer affects the distance
     *         measure
     */
    public default boolean affectsDistance() {
        return false;
    }
    
}

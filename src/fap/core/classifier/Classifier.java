/*   
 * Copyright [2024] Zoltán Gellér
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

import java.io.Serializable;

import fap.core.data.Dataset;
import fap.core.data.TimeSeries;

/**
 * Declares common methods for classifiers.
 * 
 * @author Zoltán Gellér
 * @version 2024.08.26.
 * @see Serializable
 */
public interface Classifier extends Serializable {

    /**
     * Initializes the classifier using the given training dataset.
     * 
     * <p>
     * <b>It is not intended for training the classifier. An appropriate
     * {@link fap.core.trainer.Trainer Trainer} should be used for training.</b>
     * 
     * @param trainset the training dataset
     * @throws Exception if an error occurs
     */
    public void initialize(Dataset trainset) throws Exception;

    /**
     * Classifies the given time series.
     * 
     * @param series the time series to be classified
     * @return the predicted class (label) of the time series
     * @throws Exception if an error occurs
     */
    public double classify(TimeSeries series) throws Exception;
    
}

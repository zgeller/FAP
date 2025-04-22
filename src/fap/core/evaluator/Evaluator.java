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

package fap.core.evaluator;

import java.io.Serializable;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;
import fap.core.tuner.Tuner;

/**
 * Declares basic methods for evaluating the performance of classifiers.
 * 
 * @author Zoltán Gellér
 * @version 2025.04.22.
 * @see Serializable
 */
public interface Evaluator extends Serializable {

    /**
     * Returns the error rate of the tested classifier.
     * 
     * @return the error rate of the tested classifier
     */
    public double getError();

    /**
     * Returns the number of misclassified time series.
     * 
     * @return the number of misclassified time series
     */
    public int getMisclassified();

    /**
     * Evaluates the error rate of the specified classifier using the given dataset.
     * 
     * @param tuner      the tuner that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @return the error rate of the classifier
     * @throws Exception if an error occurs
     */
    public double evaluate(Tuner tuner, Classifier classifier, Dataset dataset) throws Exception;

}

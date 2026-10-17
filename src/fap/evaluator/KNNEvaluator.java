/*   
 * Copyright 2026 Zoltán Gellér
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

package fap.evaluator;

import java.io.Serializable;

import fap.classifier.nn.KNNClassifier;
import fap.data.Dataset;

/**
 * Declares basic methods for evaluating the performance of kNN classifiers
 * leveraging their ability to classify the given time series for each neighbor
 * count from {@code 1} to {@code k}, where {@code k} denotes the number of
 * nearest neighbors.
 * 
 * <p>
 * <em>Note:</em>
 * Since classification is performed in a single pass for all neighbor counts
 * from {@code 1} to {@code k}, tuning another hyperparameter individually for
 * each number of nearest neighbors is not supported.
 * 
 * @author Zoltán Gellér
 * @version 2026.10.13.
 * @see Serializable
 */
public interface KNNEvaluator extends Serializable {

    /**
     * Returns the error rates of the evaluated kNN classifier for each neighbor
     * count from {@code 1} to {@code k}.
     *
     * @return an array containing the error rates of the evaluated kNN classifier
     *         for each neighbor count from {@code 1} to {@code k}
     */
    public double[] getError();

    /**
     * Returns the number of misclassified time series for each neighbor count from
     * {@code 1} to {@code k}.
     * 
     * @return an array containing the number of misclassified time series for each
     *         neighbor count from {@code 1} to {@code k}
     */
    public int[] getMisclassified();

    /**
     * Evaluates the error rates of the specified kNN classifier using the given
     * dataset for each neighbor count from {@code 1} to {@code k}.
     * 
     * @param classifier the kNN classifier to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @return an array containing the error rates of the evaluated kNN classifier
     *         for each neighbor count from {@code 1} to {@code k}
     * @throws Exception if an error occurs during evaluation
     */
    public double[] evaluate(KNNClassifier classifier, Dataset dataset) throws Exception;
    
}

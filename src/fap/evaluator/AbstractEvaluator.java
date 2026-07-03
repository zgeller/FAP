/*   
 * Copyright 2024-2026 Zoltán Gellér
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

import java.util.Objects;

import fap.classifier.Classifier;
import fap.data.Dataset;
import fap.exception.EmptyDatasetException;

/**
 * Defines common methods and fields for classifier evaluators.
 * 
 * @author Zoltán Gellér
 * @version 2026.07.03.
 * @see Evaluator
 */
public abstract class AbstractEvaluator implements Evaluator {

    private static final long serialVersionUID = 1L;

    /**
     * Average error rate. Default value is {@code 0.0}.
     */
    protected double error;

    /**
     * Number of misclassified time series. Default value is {@code 0}.
     */
    protected int misclassified;

    /**
     * Empty constructor.
     */
    public AbstractEvaluator() {
    }
    
    /**
     * Checks if conditions are met for evaluating.
     * 
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @throws NullPointerException  if the classifier or dataset is {@code null}
     * @throws EmptyDatasetException if the dataset is empty
     */
    protected void checkConditions(Classifier classifier, Dataset dataset) {
        Objects.requireNonNull(classifier, "The classifier cannot be null.");
        Objects.requireNonNull(dataset, "The dataset cannot be null.");
        EmptyDatasetException.check(dataset);
    }
    
    @Override
    public double getError() {
        return error;
    }

    @Override
    public int getMisclassified() {
        return misclassified;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

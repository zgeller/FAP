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

/**
 * Defines common methods and fields for evaluators of kNN classifiers,
 * leveraging the ability of kNN classifiers to classify the given time series
 * for each neighbor count from {@code 1} to {@code k}, where {@code k} denotes
 * the number of nearest neighbors.
 * 
 * @author Zoltán Gellér
 * @version 2026.10.13.
 * @see KNNEvaluator
 */
public abstract class AbstractKNNEvaluator implements KNNEvaluator {

    private static final long serialVersionUID = 1L;

    /**
     * The average error rates for each neighbor count from {@code 1} to {@code k}.
     */
    protected double[] error;

    /**
     * The number of misclassified time series for each neighbor count from {@code 1} to {@code k}.
     */
    protected int[] misclassified;
    
    /**
     * Empty constructor.
     */
    public AbstractKNNEvaluator() {
    }

    @Override
    public double[] getError() {
        return error != null ? error.clone() : null;
    }

    @Override
    public int[] getMisclassified() {
        return misclassified != null ? misclassified.clone() : null;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

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

import fap.data.Dataset;

/**
 * Auxiliary class used by {@link KNNEvaluator} implementations for storing fold-related data.
 * 
 * @author Zoltán Gellér
 * @version 2026.10.13.
 */
public class KNNFoldResult {

    /**
     * The test set.
     */
    public transient Dataset testset;
    
    /**
     * The training set.
     */
    public transient Dataset trainset;
    
    /**
     * The error rates of the evaluated kNN classifier for each neighbor count from
     * {@code 1} to {@code k}.
     */
    public double[] error;
    
    /**
     * The number of misclassified time series for each neighbor count from {@code 1} to {@code k}.
     */
    public int[] misclassified;

}

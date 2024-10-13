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

package fap.evaluator;

import java.util.List;

import fap.core.data.Dataset;

/**
 * Auxiliary class for storing fold-related data.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.11.
 */
public class FoldResult {

    /**
     * The test set.
     */
    public transient Dataset testset;
    
    /**
     * The training set.
     */
    public transient Dataset trainset;
    
    /**
     * The error rate.
     */
    public double error;
    
    /**
     * The expected error rate (the lowest error rate on the training set). Default
     * value is {@code NaN}.
     */
    public double expectedError = Double.NaN;
    
    /**
     * The list of the values of the parameters that produced the smallest
     * classification error on the training set.
     */
    public List<? extends Comparable<?>> bestParams;
    
    /**
     * The number of misclassified time series.
     */
    public int misclassified;
    

}
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

package fap.tuner;

import java.util.List;

import fap.core.classifier.Classifier;
import fap.core.evaluator.Evaluator;
import fap.core.tuner.Tuner;

/**
 * Declares common methods for classifier hyperparameter tuners that tune a single parameter.
 * 
 * 
 * @param <T> the type of the parameter that is to be tuned, it should implement
 *            the {@link Comparable} interface
 *            
 * @author Zoltán Gellér
 * @version 2025.04.21.
 * @see Tuner
 * @see Comparable
 */
public interface ParameterTuner<T extends Comparable<T>> extends Tuner {

    /**
     * Sets the list of parameter values ​​to be evaluated by this tuner.
     * 
     * @param values the list of parameter values ​​to be evaluated by this tuner
     */
    public void setValues(List<T> values);

    /**
     * Sets the list of parameter values ​​to be evaluated by this tuner.
     * 
     * @param values the array of parameter values ​​to be evaluated by this
     *               tuner.
     */
    public void setValues(T[] values);

    /**
     * Initializes the list of parameter values ​​to be evaluated by this tuner
     * with the values from {@code first} to {@code last}, in unit increment.
     * 
     * @param first the first value to be evaluated
     * @param last  the first value to be evaluated
     */
    public void setValues(T first, T last);

    /**
     * Initializes the list of parameter values ​​to be evaluated by this tuner
     * with the values from {@code first} to {@code last}, in the specified
     * {@code increment}.
     * 
     * @param first     the first value to be evaluated
     * @param last      the first value to be evaluated
     * @param increment the increment
     */
    public void setValues(T first, T last, T increment);

    /**
     * Returns the list parameter values evaluated by this tuner.
     * 
     * @return the list parameter values evaluated by this tuner
     */
    public List<T> getValues();
    
    /**
     * Returns the parameter value that produced the smallest classification error.
     * 
     * @return the parameter value that produced the smallest classification error
     */
    public T getBestValue();

    /**
     * Returns the list of the values of the parameters that produced the smallest
     * classification error.
     * 
     * @return the list of the values of the parameters that produced the smallest
     *         classification error
     */
    public List<Comparable<?>> getParameters();
    
    /**
     * It should initialize the parameter with the first element of the list and
     * forward the rest of the list to the subtuner.
     * 
     * @param classifier the classifier whose parameters are to be set
     * @param parameters the values of the parameters to be set
     */
    public void setParameters(Classifier classifier, List<Comparable<?>> parameters);
    
    /**
     * Sets the evaluator that is to be used to evaluate the classifier.
     * 
     * @param evaluator the evaluator that is to be used to evaluate the classifier
     */
    public void setEvaluator(Evaluator evaluator);

    /**
     * Returns the evaluator that is used to evaluate the classifier.
     * 
     * @return the evaluator the evaluator that is used to evaluate the classifier
     */
    public Evaluator getEvaluator();

    /**
     * Sets the subtuner that is to be used to tune another hyperparameter of the
     * classifier.
     * 
     * @param subtuner the subtuner that is to be used to tune another
     *                 hyperparameter of the classifier
     */
    public void setSubtuner(ParameterTuner<?> subtuner);

    /**
     * Returns the subtuner that is used to tune another hyperparameter of the
     * classifier.
     * 
     * @return the subtuner that is used to tune another hyperparameter of the
     *         classifier
     */
    public ParameterTuner<?> getSubtuner();

}

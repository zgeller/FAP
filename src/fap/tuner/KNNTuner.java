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

package fap.tuner;

import java.util.LinkedList;
import java.util.List;

import fap.classifier.Classifier;
import fap.classifier.nn.KNNClassifier;
import fap.data.Dataset;
import fap.evaluator.KNNEvaluator;
import fap.util.Copyable;
import fap.util.Resumable;

/**
 * A tuner of the parameter {@code k} of kNN classifiers leveraging their
 * ability to classify the given time series for each neighbor count from
 * {@code 1} to {@code k}, where {@code k} denotes the number of nearest
 * neighbors.
 *
 * <p>
 * Requirements:
 *
 * <ul>
 *  <li>{@link Modifier}<br>
 *      Because it is designed to tune the {@code k} parameter of kNN classifiers,
 *      specifying a modifier is not required, and any provided modifier is
 *      ignored.</li><br>
 *  <li>{@link ParameterTuner}<br> 
 *      Since classification is performed in a single pass for all neighbor counts
 *      from {@code 1} to {@code k}, tuning another hyperparameter individually for
 *      each number of nearest neighbors is not supported. Consequently, the
 *      subtuner is ignored.</li><br>
 *  <li>{@link Evaluator}<br>
 *      Because a {@link KNNEvaluator} is required, setting one is mandatory, and any
 *      provided general {@code evaluator} is ignored.</li>
 * </ul>
 * 
 * @author Zoltán Gellér
 * @version 2026.10.16.
 * @see IntegerTuner
 */
public class KNNTuner extends IntegerTuner {

    private static final long serialVersionUID = 1L;
    
    /**
     * The KNNEvaluator that is used to evaluate the classifier.
     */
    protected KNNEvaluator kNNEvaluator;
    
    /**
     * Constructs a new uninitialized {@code KNNTuner}.
     */
    public KNNTuner() {
    }

    /**
     * Constructs a new {@code KNNTuner} with parameter values between {@code first}
     * and {@code last}, in increments of 1.
     * 
     * @param first the first value to be evaluated, must be {@code first <= last}
     * @param last  the first value to be evaluated, must be {@code first <= last}
     */
    public KNNTuner(Integer first, Integer last) {
        super(first, last);
    }

    /**
     * Constructs a new {@code KNNTuner} with parameter values between {@code first}
     * and {@code last}, in increments of {@code increment}.
     * 
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     */
    public KNNTuner(Integer first, Integer last, Integer increment) {
        super(first, last, increment);
    }

    /**
     * Constructs a new {@code KNNTuner} with the specified parameter values.
     * 
     * @param values the list of values to be evaluated
     */
    public KNNTuner(List<Integer> values) {
        super(values);
    }

    /**
     * Constructs a new {@code KNNTuner} with the specified parameter values.
     * 
     * @param values the list of values to be evaluated
     */
    public KNNTuner(Integer[] values) {
        super(values);
    }

    /**
     * Sets the {@code KNNEvaluator} that is to be used to evaluate the kNN
     * classifier.
     * 
     * @param kNNEvaluator the {@code KNNEvaluator} that is to be used to evaluate
     *                     the kNN classifier
     */
    public void setKNNEvaluator(KNNEvaluator kNNEvaluator) {
        this.kNNEvaluator = kNNEvaluator;
    }

    /**
     * Returns the {@code KNNEvaluator} that is used to evaluate the kNN classifier.
     * 
     * @return the {@code KNNEvaluator} that is to used to evaluate the kNN
     *         classifier
     */
    public KNNEvaluator getKNNEvaluator() {
        return kNNEvaluator;
    }
    
    @Override
    public double tune(Classifier classifier, Dataset dataset) throws Exception {

        checkConditions(classifier, dataset, false);
        
        if (kNNEvaluator == null)
            throw new IllegalStateException("Evaluator cannot be null.");
        
        done = false;

        KNNClassifier kNNClassifier = (KNNClassifier) classifier;
        
        int maxk = 0;
        for (Integer value : values)
            if (value > maxk)
                maxk = value;
        
        kNNClassifier.setK(maxk);
        
        double[] error = kNNEvaluator.evaluate(kNNClassifier, dataset);
        
        // reset the evaluator
        if (kNNEvaluator instanceof Resumable re)
            re.reset();

        expectedError = Double.POSITIVE_INFINITY;
        bestValue = null;
        for (Integer value: values)
            if (error[value - 1] < expectedError) {
                expectedError = error[value - 1];
                bestValue = value;
            }
            
        parameters = new LinkedList<>();
        parameters.add(bestValue);
        
        // reset the classifier
        if (classifier instanceof Resumable rc)
            rc.reset();

        // set the best values of the parameters
        if (bestValue != null)
            this.setParameters(classifier, parameters);

        insideLoop = false;
        done = true;
        
        return expectedError;
        
    }
    
    @Override
    public void setParameters(Classifier classifier, List<Comparable<?>> bvalues) {
        this.parameters = bvalues;
        this.bestValue = (Integer) bvalues.get(0);
        ((KNNClassifier) classifier).setK(this.bestValue);
    }
    
    @Override
    public boolean affectsDistance() {
        return false;
    }
    
    /**
     * Initializes the specified tuner with the common data structures of this
     * tuner.
     * 
     * @param copy the classifier whose data structures is to be initialized
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(KNNTuner copy, boolean deep) {
        
        super.init(copy, deep);
        
        KNNEvaluator kNNEvaluatorCopy = kNNEvaluator;
        
        if (deep && kNNEvaluator != null)
            kNNEvaluatorCopy = (KNNEvaluator) ((Copyable) kNNEvaluator).makeACopy();
        
        copy.setKNNEvaluator(kNNEvaluatorCopy);
        
    }
        
    @Override
    public Object makeACopy(boolean deep) {
        KNNTuner copy = new KNNTuner();
        this.init(copy, deep);
        return copy;
    }
    
}

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;
import fap.core.tuner.Tuner;
import fap.exception.EmptyDatasetException;
import fap.util.Copyable;

/***
 * Holdout classifier evaluator.
 * 
 * <p>
 * If the number of threads is greater than 1, parallel tuning is applied by
 * default. This means that copies of the classifier will be tuned in parallel
 * using copies of the tuner, and then the time series of all the test subsets
 * are classified in parallel as well. This requires that both the tuner and
 * the classifier implement the {@link Copyable} interface (or that there is no
 * tuner and the classifier implements it). If this condition is not met (or
 * the number of seeds is less than 2), it reverts to sequential tuning of the
 * classifier and parallel classification only the time series belonging to
 * individual test subsets.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> J. Abonyi, Adatbányászat a hatékonyság eszköze, 1st ed., ComputerBooks,
 *       Budapest, 2006.
 *  <li> P.-N. Tan, M. Steinbach, V. Kumar, A. Karpatne, Introduction to Data
 *       Mining, 2nd ed., Pearson Education, 2019.
 *  <li> J. Han, J. Pei, H. Tong, Data Mining: Concepts and Techniques, 4th ed.,
 *       Morgan Kaufmann, 2022.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.04.22.
 * @see AbstractExtendedEvaluator
 */
public class HoldoutEvaluator extends AbstractSeedsEvaluator implements Copyable {

    private static final long serialVersionUID = 1L;

    /**
     * The percentage of the dataset that makes up the training set. Must be in the
     * range {@code [0..100]} Default value is {@code 50}.
     */
    private double percentage = 50;

    /**
     * Size of the test set.
     */
    private int tsize = -1;
    
    /**
     * Constructs a new single-threaded stratified Holdout evaluator that will use
     * 50% of the dataset for tuning and training, and 50% for testing.
     */
    public HoldoutEvaluator() {
    }

    /**
     * Constructs a new stratified Holdout evaluator with the specified number of
     * threads ({@code tnumber}) that will use 50% of the dataset for tuning and
     * training, and 50% for testing.
     * 
     * @param tnumber number of threads
     */
    public HoldoutEvaluator(int tnumber) {
        super(tnumber);
    }
    
    /**
     * Constructs a new stratified Holdout evaluator with the specified number of
     * threads ({@code tnumber}) that will use 50% of the dataset for tuning and
     * training, and 50% for testing.
     * 
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public HoldoutEvaluator(int tnumber, boolean fullParallel) {
        super(tnumber, fullParallel);
    }
    
    
    /**
     * Constructs a new single-threaded stratified Holdout evaluator with the
     * specified random {@code seeds} that will use 50% of the dataset for tuning
     * and training, and 50% for testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param seeds random seeds used to shuffle the dataset
     */
    public HoldoutEvaluator(long[] seeds) {
        super(seeds);
    }
    
    /**
     * Constructs a new stratified Holdout evaluator with the specified number of
     * threads ({@code tnumber}) and the specified random {@code seeds} that will
     * use 50% of the dataset for tuning and training, and 50% for testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param seeds   random seeds used to shuffle the dataset
     * @param tnumber number of threads
     */
    public HoldoutEvaluator(long[] seeds, int tnumber) {
        super(seeds, tnumber);
    }

    /**
     * Constructs a new stratified Holdout evaluator with the specified number of
     * threads ({@code tnumber}) and the specified random {@code seeds} that will
     * use 50% of the dataset for tuning and training, and 50% for testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param seeds        random seeds used to shuffle the dataset
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public HoldoutEvaluator(long[] seeds, int tnumber, boolean fullParallel) {
        super(seeds, tnumber, fullParallel);
    }
    
    /**
     * Constructs a new single-threaded stratified Holdout evaluator that will use
     * the specified {@code percentage} of the dataset for tuning and training the
     * classifier, and the remaining portion for testing.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     */
    public HoldoutEvaluator(double percentage) {
        this(percentage, null);
    }
    
    /**
     * Constructs a new single-threaded stratified Holdout evaluator with the
     * specified random {@code seeds} that will use the specified {@code percentage}
     * of the dataset for tuning and training the classifier, and the remaining
     * portion for testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     * @param seeds      random seeds used to shuffle the dataset
     */
    public HoldoutEvaluator(double percentage, long[] seeds) {
        super(seeds);
        this.setPercentage(percentage);
    }

    /**
     * Constructs a new stratified Holdout evaluator with the specified number of
     * threads ({@code tnumber}) that will use the specified {@code percentage} of
     * the dataset for tuning and training the classifier, and the remaining portion
     * for testing.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     * @param tnumber    number of threads
     */
    public HoldoutEvaluator(double percentage, int tnumber) {
        this(percentage, null, tnumber);
    }

    /**
     * Constructs a new stratified Holdout evaluator with the specified number of
     * threads ({@code tnumber}) that will use the specified {@code percentage} of
     * the dataset for tuning and training the classifier, and the remaining portion
     * for testing.
     * 
     * @param percentage   the percentage of the dataset that makes up the training
     *                     set, must be in the range {@code [0..100]}
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public HoldoutEvaluator(double percentage, int tnumber, boolean fullParallel) {
        this(percentage, null, tnumber, fullParallel);
    }
    
    /**
     * Constructs a new stratified Holdout evaluator with the specified number of
     * threads ({@code tnumber}) and the specified random {@code seeds} that will
     * use the specified {@code percentage} of the dataset for tuning and training
     * the classifier, and the remaining portion for testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     * @param seeds      random seeds used to shuffle the dataset
     * @param tnumber    number of threads
     */
    public HoldoutEvaluator(double percentage, long[] seeds, int tnumber) {
        super(seeds, tnumber);
        this.setPercentage(percentage);
    }

    /**
     * Constructs a new stratified Holdout evaluator with the specified number of
     * threads ({@code tnumber}) and the specified random {@code seeds} that will
     * use the specified {@code percentage} of the dataset for tuning and training
     * the classifier, and the remaining portion for testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param percentage   the percentage of the dataset that makes up the training
     *                     set, must be in the range {@code [0..100]}
     * @param seeds        random seeds used to shuffle the dataset
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public HoldoutEvaluator(double percentage, long[] seeds, int tnumber, boolean fullParallel) {
        super(seeds, tnumber, fullParallel);
        this.setPercentage(percentage);
    }
    
    /**
     * Constructs a new single-threaded Holdout evaluator that will use the
     * specified {@code percentage} of the dataset for tuning and training the
     * classifier, and the remaining portion for testing.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) holdout
     */
    public HoldoutEvaluator(double percentage, boolean stratified) {
        this(percentage, stratified, null);
    }
    
    /**
     * Constructs a new single-threaded Holdout evaluator with the specified random
     * {@code seeds} that will use the specified {@code percentage} of the dataset
     * for tuning and training the classifier, and the remaining portion for
     * testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) holdout
     * @param seeds      random seeds used to shuffle the dataset
     */
    public HoldoutEvaluator(double percentage, boolean stratified, long[] seeds) {
        super(stratified, seeds);
        this.setPercentage(percentage);
    }

    /**
     * Constructs a new Holdout evaluator with the specified number of threads
     * ({@code tnumber}) that will use the specified {@code percentage} of the
     * dataset for tuning and training the classifier, and the remaining portion for
     * testing.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) holdout
     * @param tnumber    number of threads
     */
    public HoldoutEvaluator(double percentage, boolean stratified, int tnumber) {
        this(percentage, stratified, null, tnumber);
    }

    /**
     * Constructs a new Holdout evaluator with the specified number of threads
     * ({@code tnumber}) that will use the specified {@code percentage} of the
     * dataset for tuning and training the classifier, and the remaining portion for
     * testing.
     * 
     * @param percentage   the percentage of the dataset that makes up the training
     *                     set, must be in the range {@code [0..100]}
     * @param stratified   indicates whether to use stratified ({@code true}) or
     *                     random ({@code false}) holdout
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public HoldoutEvaluator(double percentage, boolean stratified, int tnumber, boolean fullParallel) {
        this(percentage, stratified, null, tnumber, fullParallel);
    }
    
    /**
     * Constructs a new Holdout evaluator with the specified number of threads
     * ({@code tnumber}) and the specified random {@code seeds} that will use the
     * specified {@code percentage} of the dataset for tuning and training the
     * classifier, and the remaining portion for testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) holdout
     * @param seeds      random seeds used to shuffle the dataset
     * @param tnumber    number of threads
     */
    public HoldoutEvaluator(double percentage, boolean stratified, long[] seeds, int tnumber) {
        super(stratified, seeds, tnumber);
        this.setPercentage(percentage);
    }

    /**
     * Constructs a new Holdout evaluator with the specified number of threads
     * ({@code tnumber}) and the specified random {@code seeds} that will use the
     * specified {@code percentage} of the dataset for tuning and training the
     * classifier, and the remaining portion for testing.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param percentage   the percentage of the dataset that makes up the training
     *                     set, must be in the range {@code [0..100]}
     * @param stratified   indicates whether to use stratified ({@code true}) or
     *                     random ({@code false}) holdout
     * @param seeds        random seeds used to shuffle the dataset
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public HoldoutEvaluator(double percentage, boolean stratified, long[] seeds, int tnumber, boolean fullParallel) {
        super(stratified, seeds, tnumber, fullParallel);
        this.setPercentage(percentage);
    }
    
    /**
     * Sets the percentage of the dataset that makes up the training set. Must be in
     * the range {@code [0..100]}.
     * 
     * @param percentage the percentage of the dataset that makes up the training
     *                   set, must be in the range {@code [0..100]}
     * @throws IllegalArgumentException if {@code percentage < 0} or
     *                                  {@code percentage > 100}
     */
    public void setPercentage(double percentage) throws IllegalArgumentException {
        
        if (percentage < 0 || percentage > 100)
            throw new IllegalArgumentException("Percentage must be in the range [0..100].");
        
        this.percentage = percentage;
        
    }

    /**
     * Returns the percentage of the dataset that makes up the training set.
     * 
     * @return the percentage of the dataset that makes up the training set
     */
    public double getPercentage() {
        return this.percentage;
    }

    /***
     * @exception InterruptedException when the interrupted flag is set
     */
    @Override
    public double evaluate(Tuner tuner, Classifier classifier, Dataset dataset) throws Exception {

        if (done)
            return error;

        EmptyDatasetException.check(dataset);

        // determining the size of the test set
        if (tsize < 0) {
            List<Dataset> list = dataset.divide(percentage, stratified);
            tsize = list.get(1).size();
        }

        int runs = seeds == null ? 1 : seeds.length;

        int iterations = runs;
        
        int cbCount = runs * tsize;

        return super.evaluate(tuner, classifier, dataset, iterations, cbCount);

    }
    
    @Override
    protected List<Dataset> splitDataset(Dataset dataset, int iteration) {
        
        Dataset ds = dataset;
        
        if (seeds != null) {
            ds = new Dataset(dataset);
            Collections.shuffle(ds, new Random(seeds[iteration]));
        }
        
        // dividing the dataset into test and train subsets
        List<Dataset> list = ds.divide(percentage, stratified);

        return list;
        
    }
    
    /**
     * Sets the parameters of the specified evaluator to be equal to the parameters
     * of this evaluator.
     * 
     * @param copy the evaluator whose parameters are to be set
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(HoldoutEvaluator copy, boolean deep) {
        super.init(copy, deep);
        copy.setPercentage(this.getPercentage());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        HoldoutEvaluator copy = new HoldoutEvaluator();
        init(copy, deep);
        return copy;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", percentage=" + getPercentage() 
                                + ", stratified=" + isStratified()
                                + ", seeds=" + Arrays.toString(getSeeds());
    }
    
}

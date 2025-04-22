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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;
import fap.core.tuner.Tuner;
import fap.exception.EmptyDatasetException;
import fap.util.Copyable;

/**
 * Cross-Validation classifier evaluator.
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
 *  <li> M. Mohri, A. Rostamizadeh, A. Talwalkar, Foundations of Machine Learning,
 *       The MIT Press, 2012.
 *  <li> J. Abonyi, Adatbányászat a hatékonyság eszköze, 1st ed., ComputerBooks,
 *       Budapest, 2006.
 *  <li> P.-N. Tan, M. Steinbach, V. Kumar, A. Karpatne, Introduction to Data
 *       Mining, 2nd ed., Pearson Education, 2019.
 *  <li> J. Han, J. Pei, H. Tong, Data Mining: Concepts and Techniques, 4th ed.,
 *       Morgan Kaufmann, 2022.
 *  <li> T.M. Mitchell, Machine Learning, McGraw-Hill, Inc., New York, NY, USA,
 *       1997.
 *  <li> E. Alpaydin, Introduction to Machine Learning, MIT Press, 2004.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.04.22.
 * @see AbstractExtendedEvaluator
 */
public class CrossValidationEvaluator extends AbstractSeedsEvaluator implements Copyable {

    private static final long serialVersionUID = 1L;

    /**
     * Number of folds. Default value is {@code 10}.
     */
    private int fnumber = 10;
    
    /**
     * List containing folds for every seed.
     */
    private transient List<List<Dataset>> listOfFolds;

    /**
     * Constructs a new single-threaded stratified 10-fold CrossValidation
     * evaluator.
     */
    public CrossValidationEvaluator() {
    }
    
    /**
     * Constructs a new single-threaded stratified 10-fold CrossValidation evaluator
     * with the specified random {@code seeds}.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param seeds random seeds used to shuffle the dataset
     */
    public CrossValidationEvaluator(long[] seeds) {
        super(seeds);
    }

    /**
     * Constructs a new stratified CrossValidation evaluator with the specified
     * number of threads ({@code tnumber}), the specified random {@code seeds}, and
     * parallel tuning.
     * 
     * @param seeds   random seeds used to shuffle the dataset
     * @param tnumber number of threads
     */
    public CrossValidationEvaluator(long[] seeds, int tnumber) {
        super(seeds, tnumber);
    }

    /**
     * Constructs a new stratified CrossValidation evaluator with the specified
     * number of threads ({@code tnumber}) and the specified random {@code seeds}.
     * 
     * @param seeds        random seeds used to shuffle the dataset
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public CrossValidationEvaluator(long[] seeds, int tnumber, boolean fullParallel) {
        super(seeds, tnumber, fullParallel);
    }
    
    /**
     * Constructs a new single-threaded stratified CrossValidation evaluator with
     * the specified number of folds ({@code fnumber}).
     * 
     * @param fnumber number of folds, must be {@code > 1}
     */
    public CrossValidationEvaluator(int fnumber) {
        this(fnumber, null);
    }

    /**
     * Constructs a new stratified CrossValidation evaluator with the specified
     * number of folds ({@code fnumber}) and threads ({@code tnumber}), and parallel
     * tuning.
     * 
     * @param fnumber number of folds, must be {@code > 1}
     * @param tnumber number of threads
     */
    public CrossValidationEvaluator(int fnumber, int tnumber) {
        super(tnumber);
        this.setNumberOfFolds(fnumber);
    }

    /**
     * Constructs a new stratified CrossValidation evaluator with the specified
     * number of folds ({@code fnumber}) and threads ({@code tnumber}).
     * 
     * @param fnumber      number of folds, must be {@code > 1}
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public CrossValidationEvaluator(int fnumber, int tnumber, boolean fullParallel) {
        super(tnumber, fullParallel);
        this.setNumberOfFolds(fnumber);
    }
    
    /**
     * Constructs a new single-threaded stratified CrossValidation evaluator with
     * the specified number of folds ({@code fnumber}) and the specified random
     * {@code seeds}.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param fnumber number of folds, must be {@code > 1}
     * @param seeds   random seeds used to shuffle the dataset
     */
    public CrossValidationEvaluator(int fnumber, long[] seeds) {
        super(seeds);
        this.setNumberOfFolds(fnumber);
    }

    /**
     * Constructs a new stratified CrossValidation evaluator with the specified
     * number of folds ({@code fnumber}) and threads ({@code tnumber}), the
     * specified random {@code seeds}, and parallel tuning.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param fnumber number of folds, must be {@code > 1}
     * @param seeds   random seeds used to shuffle the dataset
     * @param tnumber number of threads
     */
    public CrossValidationEvaluator(int fnumber, long[] seeds, int tnumber) {
        super(seeds, tnumber);
        this.setNumberOfFolds(fnumber);
    }

    /**
     * Constructs a new stratified CrossValidation evaluator with the specified
     * number of folds ({@code fnumber}) and threads ({@code tnumber}), the
     * specified random {@code seeds}.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param fnumber      number of folds, must be {@code > 1}
     * @param seeds        random seeds used to shuffle the dataset
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public CrossValidationEvaluator(int fnumber, long[] seeds, int tnumber, boolean fullParallel) {
        super(seeds, tnumber, fullParallel);
        this.setNumberOfFolds(fnumber);
    }
    
    /**
     * Constructs a new single-threaded CrossValidation evaluator with the specified
     * number of folds ({@code fnumber}).
     * 
     * @param fnumber    number of folds, must be {@code > 1}
     * @param stratified indicates whether the folds should be stratified
     *                   {@code true}
     * 
     */
    public CrossValidationEvaluator(int fnumber, boolean stratified) {
        this(fnumber, stratified, null);
    }

    /**
     * Constructs a new single-threaded CrossValidation evaluator with the specified
     * number of folds ({@code fnumber}) and the specified random {@code seeds}.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param fnumber    number of folds, must be {@code > 1}
     * @param stratified indicates whether the folds should be stratified
     *                   {@code true}
     * @param seeds      random seeds used to shuffle the dataset
     */
    public CrossValidationEvaluator(int fnumber, boolean stratified, long[] seeds) {
        super(stratified, seeds);
        this.setNumberOfFolds(fnumber);
    }

    /**
     * Constructs a new CrossValidation evaluator with the specified number of folds
     * ({@code fnumber}) and threads ({@code tnumber}), and parallel tuning.
     * 
     * @param fnumber    number of folds, must be {@code > 1}
     * @param stratified indicates whether the folds should be stratified
     *                   {@code true}
     * @param tnumber    number of threads
     */
    public CrossValidationEvaluator(int fnumber, boolean stratified, int tnumber) {
        this(fnumber, stratified, null, tnumber);
    }

    /**
     * Constructs a new CrossValidation evaluator with the specified number of folds
     * ({@code fnumber}) and threads ({@code tnumber}).
     * 
     * @param fnumber      number of folds, must be {@code > 1}
     * @param stratified   indicates whether the folds should be stratified
     *                     {@code true}
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public CrossValidationEvaluator(int fnumber, boolean stratified, int tnumber, boolean fullParallel) {
        this(fnumber, stratified, null, tnumber, fullParallel);
    }
    
    /**
     * Constructs a new CrossValidation evaluator with the specified number of folds
     * ({@code fnumber}) and threads ({@code tnumber}), and the specified random
     * {@code seeds}, and parallel tuning.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param fnumber    number of folds, must be {@code > 1}
     * @param stratified indicates whether the folds should be stratified
     *                   {@code true}
     * @param seeds      random seeds used to shuffle the dataset
     * @param tnumber    number of threads
     */
    public CrossValidationEvaluator(int fnumber, boolean stratified, long[] seeds, int tnumber) {
        super(stratified, seeds, tnumber);
        this.setNumberOfFolds(fnumber);
    }

    /**
     * Constructs a new CrossValidation evaluator with the specified number of folds
     * ({@code fnumber}) and threads ({@code tnumber}), and the specified random
     * {@code seeds}.
     * 
     * <p>
     * The number of seeds determines how many times should the evaluation be
     * repeated.
     * 
     * @param fnumber      number of folds, must be {@code > 1}
     * @param stratified   indicates whether the folds should be stratified
     *                     {@code true}
     * @param seeds        random seeds used to shuffle the dataset
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    public CrossValidationEvaluator(int fnumber, boolean stratified, long[] seeds, int tnumber, boolean fullParallel) {
        super(stratified, seeds, tnumber, fullParallel);
        this.setNumberOfFolds(fnumber);
    }
    
    /**
     * Sets the number of folds. Must be {@code fnumber>1}.
     * 
     * @param fnumber the number of folds, must be {@code > 1}
     * @throws IllegalArgumentException if {@code fnumber <= 1}
     */
    public void setNumberOfFolds(int fnumber) {
        if (fnumber < 2)
            throw new IllegalArgumentException("The number of folds must be greater than 1.");
        this.fnumber = fnumber;
    }

    /**
     * Returns the number of folds.
     * 
     * @return the number of folds
     */
    public int getNumberOfFolds() {
        return this.fnumber;
    }

    /***
     * @throws InterruptedException when the interrupted flag is set
     */
    @Override
    public double evaluate(Tuner tuner, Classifier classifier, Dataset dataset) throws Exception {

        if (done)
            return error;

        EmptyDatasetException.check(dataset);

        int fnumber = this.getNumberOfFolds();

        int runs = seeds == null ? 1 : seeds.length;

        int iterations = runs * fnumber;
        
        int cbCount = runs * dataset.size();
        
        // creating folds
        listOfFolds = new ArrayList<List<Dataset>>(runs);
        if (seeds == null) {
            List<Dataset> folds = dataset.split(fnumber, stratified);
            for (int run = 0; run < runs; run++)
                listOfFolds.add(folds);
        }
        else
            for (int run = 0; run < runs; run++) {
                Dataset ds = new Dataset(dataset);
                Collections.shuffle(ds, new Random(seeds[run]));
                listOfFolds.add(ds.split(fnumber, stratified));
            }

        try {
            return super.evaluate(tuner, classifier, dataset, iterations, cbCount);
        }
        catch (Exception e) {
            listOfFolds = null;
            throw e;
        }

    }

    @Override
    protected List<Dataset> splitDataset(Dataset dataset, int iteration) {
        
        int fnumber = this.getNumberOfFolds();
        
        int run = iteration / fnumber;

        int fold = iteration % fnumber;
        
        List<Dataset> list = new ArrayList<>();
        
        // preparing the test and training sets
        List<Dataset> folds = listOfFolds.get(run);
        list.add(new Dataset(folds, fold));      // training set
        list.add(folds.get(fold));               // test set
        
        return list;
        
    }

    /**
     * Sets the parameters of the specified evaluator to be equal to the parameters
     * of this evaluator.
     * 
     * @param copy the evaluator whose parameters are to be set
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(CrossValidationEvaluator copy, boolean deep) {
        super.init(copy, deep);
        copy.setNumberOfFolds(this.getNumberOfFolds());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        CrossValidationEvaluator copy = new CrossValidationEvaluator();
        init(copy, deep);
        return copy;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", fnumber=" + getNumberOfFolds() 
                                + ", stratified=" + isStratified()
                                + ", seeds=" + Arrays.toString(getSeeds());
    }
    
}

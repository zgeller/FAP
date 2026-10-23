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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import fap.classifier.Classifier;
import fap.data.Dataset;
import fap.data.TimeSeries;
import fap.tuner.ParameterTuner;
import fap.tuner.Tuner;
import fap.util.Copier;
import fap.util.Copyable;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;
import fap.util.ThreadUtils.RunnableWrapper;

/**
 * Defines common methods and fields for classifier evaluators with seeds.
 * 
 * <p>
 * If the number of threads is greater than 1, parallel tuning and evaluation is
 * applied by default. This means that copies of the classifier are tuned in
 * parallel using copies of the tuner, and the time series across all test
 * subsets are classified in parallel as well. This requires that both the tuner
 * and the classifier implement the {@link Copyable} interface (or that there is
 * no tuner and the classifier implements it). If this condition is not met, the
 * evaluator reverts to sequential tuning of the classifier and parallel
 * classification of only the time series belonging to individual test subsets.
 * 
 * <p>
 * In parallel classification mode, the thread count of the classifier (or its
 * copies) is set to 1 (under the assumption that the executor used by the
 * classifier is instantiated once, rather than per {@code classify} method
 * call) and restored once the evaluation is complete.
 * 
 * @author Zoltán Gellér
 * @version 2026.10.23.
 * @see AbstractExtendedEvaluator
 * @see Evaluator
 */
public abstract class AbstractSeedsEvaluator extends AbstractExtendedEvaluator {

    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates whether to use stratified {@code true} or random {@code false}
     * split. Default value is {@code true}.
     */
    protected boolean stratified = true;
    
    /**
     * Random seeds used to shuffle the dataset. The number of seeds determines how
     * many times should the evaluation be repeated.
     */
    protected long[] seeds;
    
    /**
     * Indicates whether to apply parallel tuning and evaluation of the classifier,
     * which requires that both the tuner and the classifier implement the
     * {@link Copyable} interface (or that there is no tuner and the classifier
     * implements it). If this requirement is not met, the algorithm reverts to
     * sequential tuning of the classifier and parallel classification of only the
     * time series belonging to individual test subsets.
     * 
     * <p>
     * Default value is {@code true}.
     */
    protected boolean fullParallel = true;
    
    /**
     * An array containing the evaluation results obtained for each seed. A
     * {@code null} value indicates that the given seed has not yet been processed.
     */
    protected FoldResult[] results;
    
    /**
     * List of tuner copies used by the parallel implementation.
     */
    protected ConcurrentLinkedQueue<Tuner> tuners;
    
    /**
     * A tuned classifier for each iteration.
     */
    protected Classifier[] classifiers;
    
    /**
     * Indicates whether the specified time series has already been classified.
     */
    protected boolean[][] classified;
    
    /**
     * The number of the classified time series within each iteration.
     */
    protected int[] classifiedCount;
    
    /**
     * New futures created within the tuner tasks.
     */
    private transient ConcurrentLinkedQueue<Future<?>> newFutures;
    
    protected double stepSize = 0;
    protected double progress = 0;
    protected int steps = 1;
    
    /**
     * Emptyt constructor (single-threaded, stratified splitting of the dataset,
     * parallel tuning).
     */
    protected AbstractSeedsEvaluator() {
    }
    
    /**
     * Constructor with single-threaded execution.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     */
    protected AbstractSeedsEvaluator(boolean stratified) {
        this.setStratified(stratified);
    }
    
    /**
     * Constructor with the number of threads, stratified splitting of the
     * dataset, and parallel tuning.
     * 
     * @param tnumber number of threads.
     */
    protected AbstractSeedsEvaluator(int tnumber) {
        super(tnumber);
    }

    /**
     * Constructor with the number of threads, and parallel tuning.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     * @param tnumber    number of threads.
     */
    protected AbstractSeedsEvaluator(boolean stratified, int tnumber) {
        super(tnumber);
        this.setStratified(stratified);
    }
    

    /**
     * Constructor with the number of threads, and stratified splitting of the
     * dataset.
     * 
     * @param tnumber      number of threads.
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    protected AbstractSeedsEvaluator(int tnumber, boolean fullParallel) {
        this(null, tnumber, fullParallel);
    }

    /**
     * Constructor with the number of threads.
     * 
     * @param stratified   indicates whether to use stratified ({@code true}) or
     *                     random ({@code false}) splitting
     * @param tnumber      number of threads.
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    protected AbstractSeedsEvaluator(boolean stratified, int tnumber, boolean fullParallel) {
        this(stratified, null, tnumber, fullParallel);
    }
    
    /**
     * Constructor with the seeds, stratified splitting of the dataset, and parallel
     * tuning.
     * 
     * @param seeds random seeds used to shuffle the dataset
     */
    protected AbstractSeedsEvaluator(long[] seeds) {
        this.setSeeds(seeds);
    }

    /**
     * Constructor with the seeds, and parallel tuning.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     * @param seeds      random seeds used to shuffle the dataset
     */
    protected AbstractSeedsEvaluator(boolean stratified, long[] seeds) {
        this.setStratified(stratified);
        this.setSeeds(seeds);
    }
    
    /**
     * Constructor with the seeds and the number of threads, stratified splitting of
     * the dataset, and parallel tuning.
     * 
     * @param seeds   random seeds used to shuffle the dataset
     * @param tnumber number of threads
     */
    protected AbstractSeedsEvaluator(long[] seeds, int tnumber) {
        super(tnumber);
        this.setSeeds(seeds);
    }
    
    /**
     * Constructor with the seeds and the number of threads, and parallel tuning.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     * @param seeds      random seeds used to shuffle the dataset
     * @param tnumber    number of threads
     */
    protected AbstractSeedsEvaluator(boolean stratified, long[] seeds, int tnumber) {
        super(tnumber);
        this.setStratified(stratified);
        this.setSeeds(seeds);
    }
    
    
    /**
     * Constructor with the seeds and the number of threads, and stratified
     * splitting of the dataset.
     * 
     * @param seeds        random seeds used to shuffle the dataset
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    protected AbstractSeedsEvaluator(long[] seeds, int tnumber, boolean fullParallel) {
        super(tnumber);
        this.setSeeds(seeds);
        this.setFullParallel(fullParallel);
    }

    /**
     * Constructor with the seeds and the number of threads.
     * 
     * @param stratified   indicates whether to use stratified ({@code true}) or
     *                     random ({@code false}) splitting
     * @param seeds        random seeds used to shuffle the dataset
     * @param tnumber      number of threads
     * @param fullParallel indicates whether to apply full parallel tuning
     *                     ({@code true})
     */
    protected AbstractSeedsEvaluator(boolean stratified, long[] seeds, int tnumber, boolean fullParallel) {
        super(tnumber);
        this.setStratified(stratified);
        this.setSeeds(seeds);
        this.setFullParallel(fullParallel);
    }
    
    /**
     * Selects between stratified ({@code true}) and random ({@code false}) split.
     * 
     * @param stratified indicates whether to use stratified {@code true} or random
     *                   {@code false} split
     */
    public void setStratified(boolean stratified) {
        this.stratified = stratified;
    }

    /**
     * Returns {@code true} if stratified split is selected.
     * 
     * @return {@code true} if stratified split is selected
     */
    public boolean isStratified() {
        return this.stratified;
    }
    
    /**
     * Returns the random seeds used to shuffle the dataset.
     * 
     * @return the random seeds used to shuffle the dataset
     */
    public long[] getSeeds() {
        return seeds != null ? seeds.clone() : null;
    }

    /**
     * Sets the random seeds to be used to shuffle the dataset.
     * 
     * @param the random seeds to be used to shuffle the dataset
     */
    public void setSeeds(long[] seeds) {
        this.seeds = seeds != null ? seeds.clone() : null;
    }
    
    /**
     * Returns {@code true} if parallel tuning (and evaluating) should be applied.
     * 
     * @return {@code true} if parallel tuning (and evaluating) should be applied
     */
    public boolean isFullParallel() {
        return fullParallel;
    }
    
    /**
     * Indicates whether parallel tuning (and evaluating) should be applied.
     * 
     * @param fullParallel indicates whether parallel tuning (and evaluating)
     *                     should be applied
     */
    public void setFullParallel(boolean fullParallel) {
        this.fullParallel = fullParallel;
    }
    
    /**
     * Returns the results of the individual iterations of the evaluation process.
     * 
     * @return the results of the individual iterations of the evaluation process
     */
    public FoldResult[] getResults() {
        return results;
    }
    
    // initialization
    protected void initLoop(int iterations) {
        misclassified = 0;
        error = 0.0;
        progress = -1;
        steps = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
        results = new FoldResult[iterations];
        classifiers = new Classifier[iterations];
        classified = new boolean[iterations][];
        classifiedCount = new int[iterations];
    }
    
    /**
     * Evaluates the specified {@code classifier} using the specified {@code tuner}
     * and {@code dataset}.
     * 
     * @param tuner      the tuner that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations the number of iterations
     * @param cbCount    the number of possible callbacks
     * @return the error rate
     * @throws Exception if an error occurs during evaluation
     */
    protected double evaluate(Tuner tuner, Classifier classifier, Dataset dataset, int iterations, int cbCount) throws Exception {

        // initialize callback
        if (callback != null) {
            stepSize = (double) callback.getDesiredCallbackNumber() / cbCount;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(cbCount + 1);
                stepSize = 1;
            }
        }

        // initialize
        if (!insideLoop)
            initLoop(iterations);
        else if (callback != null)
            callback.setCallbackCount(steps);

        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

        // check the conditions for parallel tuning and evaluating multiple copies of the classifier 
        boolean parallelTuning = (tnumber > 1) && 
                                 this.isFullParallel() && 
                                 ((tuner == null) || (tuner instanceof Copyable)) && 
                                 (classifier instanceof Copyable);

        if (parallelTuning)
            evaluateParallelTuning(tuner, classifier, dataset, iterations, tnumber);
        else
            evaluateSequentialTuning(tuner, classifier, dataset, iterations, tnumber);

        // calculate error rates
        error = 0;
        for (FoldResult result : results) {
            result.error = (double) result.misclassified / result.testset.size();
            misclassified += result.misclassified;
            error += result.error;
        }

        // finalize
        error = error / iterations;
        insideLoop = false;
        done = true;
        if (callback != null)
            callback.setCallbackCount(-1);

        return error;

    }
    
    /**
     * Splits the dataset for the specified iteration into training and test subsets
     * in a thread-safe manner.
     * 
     * <p>
     * The zeroth element of the resulting list represents the training set, and the
     * first element represents the test set.
     * 
     * @param dataset   the dataset to be split
     * @param iteration the iteration sequence number
     * @return a list containing the training set and test set (in that order)
     */
    protected abstract List<Dataset> splitDataset(Dataset dataset, int iteration);
    
    /**
     * Tuning task.
     */
    protected class TuneTask implements Runnable {

        /**
         * The tuner.
         */
        Tuner tuner;
        
        /**
         * The classifier.
         */
        Classifier classifier;

        /**
         * The dataset.
         */
        Dataset dataset;
        
        /**
         * The sequence number of the iteration.
         */
        int iteration;
        
        /**
         * A copy of the tuner used to train the classifier.
         */
        Tuner tunerCopy;
        
        /**
         * A copy of the classifier that is to be tuned.
         */
        Classifier classifierCopy;
        
        /**
         * Constructs a new tuning task for the specified {@code index}.
         * 
         * @param index the index of this task
         */
        public TuneTask(int iteration, Tuner tuner, Classifier classifier, Dataset dataset) {
            this.iteration = iteration;
            this.tuner = tuner;
            this.classifier = classifier;
            this.dataset = dataset;
        }

        @Override
        public void run() {

            // create a new result object
            FoldResult result = new FoldResult();
            
            List<Dataset> list = splitDataset(dataset, iteration);
            
            result.trainset = list.get(0);
            result.testset = list.get(1);
            
            classified[iteration] = new boolean[result.testset.size()];
            
            // if there is no need for tuning
            if (tuner == null) {

                classifierCopy = (Classifier) ((Copyable)classifier).makeACopy(false);
                
                try {
                    classifierCopy.fit(result.trainset);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // only single-threaded classifiers are supported
                // - if the same classifier is used in multiple threads, the same executor will
                // deal with all the tasks generated in those threads
                if (classifierCopy instanceof Multithreaded mc)
                    mc.setNumberOfThreads(1);
                
                classifiers[iteration] = classifierCopy;
                results[iteration] = result;
                
                return;
                
            }
            
            // get a tuner
            tunerCopy = tuners.poll();
            
            // deep copy of the classifier is needed only if the tuner affects the
            // underlying distance measure
            classifierCopy = (Classifier) ((Copyable)classifier).makeACopy(tunerCopy.affectsDistance());
            
            // tune and update the result object 
            try {
                result.expectedError = tunerCopy.tune(classifierCopy, result.trainset);
                if (tunerCopy instanceof ParameterTuner<?> pt)
                    result.bestParams = pt.getParameters();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                classifierCopy.fit(result.trainset);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // only single-threaded classifiers are supported
            // - if the same classifier is used in multiple threads, the same executor will
            // deal with all the tasks generated in those threads
            if (classifierCopy instanceof Multithreaded mc)
                mc.setNumberOfThreads(1);
            
            classifiers[iteration] = classifierCopy;
            results[iteration] = result;
            
            // reset the tuner
            if (tunerCopy instanceof Resumable rt)
                rt.reset();

            // release the tuner
            tuners.add(tunerCopy);
            
            // create classifying tasks and executing them
            List<ClassifyTask> classifyTasks = new ArrayList<>();
            
            for (int index = 0; index < results[iteration].testset.size(); index++)
                if (!classified[iteration][index])
                    classifyTasks.add(new ClassifyTask(iteration, index));
            
            for (ClassifyTask task: classifyTasks)
                newFutures.add(executor.submit(new RunnableWrapper(task)));
            
        }
        
    }
    
    /**
     * Classification task.
     */
    protected class ClassifyTask implements Runnable {
        
        /**
         * Fold index.
         */
        int iteration;
        
        /**
         * Index of the time series to be classified within the test set of the
         * specified {@code iteration}.
         */
        int tsIndex;
        
        /**
         * Constructs a new classification task.
         * 
         * @param iteration iteration index
         * @param tsIndex   index of the time series to be classified within the test
         *                  set of the specified {@code iteration}
         */
        public ClassifyTask(int iteration, int tsIndex) {
            this.iteration = iteration;
            this.tsIndex = tsIndex;
        }

        @Override
        public void run() {
            
            TimeSeries ts = results[iteration].testset.get(tsIndex);
            
            Classifier classifier = classifiers[iteration];

            // classify
            double label = Double.NaN;
            try {
                label = classifier.classify(ts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            classified[iteration][tsIndex] = true;
            
            // update the results
            synchronized(results[iteration]) {
                classifiedCount[iteration]++;
                if (label != ts.getLabel())
                    results[iteration].misclassified++;
            }
            
            // call back
            if (callback != null)
                synchronized(callback) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        try {
                            callback.callback(AbstractSeedsEvaluator.this);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            
        }
        
    }
    
    /**
     * Performs multiple iterations of classifier evaluation with sequential tuning
     * and sequential or parallel (if {@code tnumber} is greater than 1)
     * classification.
     * 
     * @param tuner      the tuner used to tune the classifier
     * @param classifier the classifier to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations the number of iterations
     * @param tnumber    the number of threads
     * @throws Exception if an error occurs during evaluation
     */
    protected void evaluateSequentialTuning(Tuner tuner, 
                                            Classifier classifier, 
                                            Dataset dataset, 
                                            int iterations,
                                            int tnumber) throws Exception {
        
        if (tnumber > 1)
            this.initExecutor(tnumber);
        
        for (int iteration = 0; iteration < iterations; iteration++) {

            if (Thread.interrupted())
                throw new InterruptedException();
            
            FoldResult result;

            // if the classifier has already been tuned and evaluated on the test set
            if (results[iteration] != null && classifiedCount[iteration] == results[iteration].testset.size())
                    continue;
            
            else {

                result = new FoldResult();
                
                List<Dataset> list = splitDataset(dataset, iteration);
                
                // prepare the test and tuning sets
                result.trainset = list.get(0);
                result.testset = list.get(1);
                
                // tune the classifier
                double expectedError = Double.NaN;
                if (tuner != null)
                    expectedError = tuner.tune(classifier, result.trainset);
                
                classifier.fit(result.trainset);

                result.expectedError = expectedError;
                result.bestParams = null;
                if (tuner instanceof ParameterTuner<?> pt)
                    result.bestParams = pt.getParameters();

                results[iteration] = result;
                classifiers[iteration] = classifier;
                classified[iteration] = new boolean[result.testset.size()];
                
                // resetg the trainer
                if (tuner instanceof Resumable rt)
                    rt.reset();
                
                if (Thread.interrupted())
                    throw new InterruptedException();

            }
            
            result = results[iteration];

            int tsize = result.testset.size();
            
            // multi-threaded testing
            if (tnumber > 1) {

                // initialize 
                List<ClassifyTask> tasks = new ArrayList<>(tsize);

                for (int i = 0; i < tsize; i++)
                    if (!classified[iteration][i])
                        tasks.add(new ClassifyTask(iteration, i));
                
                // only single-threaded classifiers are supported
                // - if the same classifier is used in multiple threads, the same executor will
                // deal with all the tasks generated in those threads
                int ctnumber = 0;
                if (classifier instanceof Multithreaded mc) {
                    ctnumber = mc.getNumberOfThreads();
                    mc.setNumberOfThreads(1);
                }

                try {
                    ThreadUtils.startRunnables(executor, tasks);
                }
                finally {
                    // restoring the thread number of multithreaded classifiers
                    if (classifier instanceof Multithreaded mc)
                        mc.setNumberOfThreads(ctnumber);
                }
                
            }
            
            // single-threaded testing
            else {
                
                for (int i = 0; i < tsize; i++) {
                    
                    if (Thread.interrupted())
                        throw new InterruptedException();
                    
                    TimeSeries ts = result.testset.get(i);
                    
                    double label = classifier.classify(ts);
                    
                    classified[iteration][i] = true;
                    
                    classifiedCount[iteration]++;
                    if (label != ts.getLabel())
                        results[iteration].misclassified++;
                    
                    // call back
                    if (callback != null) {
                        progress += stepSize;
                        if (progress >= steps) {
                            steps++;
                            try {
                                callback.callback(this);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    
                }
                
            }
            
            result.error = (double) result.misclassified / tsize;

            // reset the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();
            
        }
        
    }
    
    /**
     * Performs multiple iterations of classifier evaluation with parallel tuning and
     * classification.
     * 
     * @param tuner      the tuner used to tune the classifier
     * @param classifier the classifier to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations the number of iterations
     * @param tnumber    the number of threads
     * @throws Exception if an error occurs during evaluation
     */
    protected void evaluateParallelTuning(Tuner tuner, 
                                          Classifier classifier, 
                                          Dataset dataset,
                                          int iterations,
                                          int tnumber) throws Exception {

        this.initExecutor(tnumber);
        
        // prepare the tuners
        tuners = new ConcurrentLinkedQueue<>();
        if (tuner != null)
            Copier.makeCopies(tuner, tuners, tnumber);
        
        // create tune tasks and executing them
        List<TuneTask> tuneTasks = new ArrayList<>();
        
        for (int iteration = 0; iteration < iterations; iteration++ )
            if (results[iteration] == null)
                tuneTasks.add(new TuneTask(iteration, tuner, classifier, dataset));
        
        newFutures = new ConcurrentLinkedQueue<>();
        
        ThreadUtils.startRunnables(executor, tuneTasks);
        
        ThreadUtils.waitForFutures(executor, newFutures);
        
        if (Thread.interrupted())
            throw new InterruptedException();
        
        // In case the evaluation is interrupted after all tuning tasks have been executed
        // but before all classification tasks have been completed, create the remaining
        // classification tasks and execute them.
        List<ClassifyTask> classifyTasks = new ArrayList<>();
        
        for (int iteration = 0; iteration < iterations; iteration++)
            for (int index = 0; index < results[iteration].testset.size(); index++)
                if (!classified[iteration][index])
                    classifyTasks.add(new ClassifyTask(iteration, index));
        
        if (Thread.interrupted())
            throw new InterruptedException();
        
        ThreadUtils.startRunnables(executor, classifyTasks);
        
        // shut down the copies of the classifier and the tuner if they are multi-threaded
        
        if (classifier instanceof Multithreaded)
            for (Classifier classifierCopy : classifiers)
                ((Multithreaded) classifierCopy).shutdown();                
        
        if (tuner instanceof Multithreaded)
            for (Tuner tunerCopy : tuners)
                ((Multithreaded) tunerCopy).shutdown();
        
        tuners = null;
        classifiers = null;
        classified = null;
        classifiedCount = null;

    }

    /**
     * Initializes the parameters of the specified evaluator ({@code copy}) to match
     * those of this evaluator.
     * 
     * @param copy the evaluator whose parameters are to be initialized
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(AbstractSeedsEvaluator copy, boolean deep) {
        super.init(copy, deep);
        copy.setStratified(this.isStratified());
        copy.setSeeds(this.getSeeds());
    }
    
}

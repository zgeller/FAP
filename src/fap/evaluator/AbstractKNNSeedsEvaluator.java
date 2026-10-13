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

import java.util.ArrayList;
import java.util.List;

import fap.classifier.nn.KNNClassifier;
import fap.data.Dataset;
import fap.data.TimeSeries;
import fap.util.Copyable;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;

/**
 * 
 * Defines common methods and fields for evaluators of kNN classifiers with
 * seeds, leveraging the ability of kNN classifiers to classify the given time
 * series for each neighbor count from {@code 1} to {@code k}, where {@code k}
 * denotes the number of nearest neighbors.
 * 
 * <p>
 * If the number of threads is greater than 1, parallel training and evaluation
 * are applied by default. This means that copies of the classifier are trained
 * in parallel, and the time series across all test subsets are classified in
 * parallel as well. This requires that the classifier implements the
 * {@link Copyable} interface. If this condition is not met, the evaluator
 * reverts to sequential training of the classifier and parallel classification
 * of only the time series belonging to individual test subsets.
 * 
 * @author Zoltán Gellér
 * @version 2026.10.13.
 * @see AbstractKNNExtendedEvaluator
 * @see KNNEvaluator
 */
public abstract class AbstractKNNSeedsEvaluator extends AbstractKNNExtendedEvaluator {

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
     * Indicates whether to apply parallel training and evaluation the classifier,
     * which requires that the classifier implements the {@link Copyable} interface.
     * If this requirement is not met, the algorithm reverts to sequential training
     * of the classifier and parallel classification of only the time series
     * belonging to individual test subsets.
     * 
     * <p>
     * Default value is {@code true}.
     */
    protected boolean fullParallel = true;
    
    
    /**
     * An array containing the evaluation results obtained for each seed. A
     * {@code null} value indicates that the given seed has not yet been processed.
     */
    protected KNNFoldResult[] results;
    
    /**
     * A trained classifier for each iteration.
     */
    protected KNNClassifier[] classifiers;
    
    /**
     * Indicates whether the specified time series has already been classified.
     */
    protected boolean[][] classified;
    
    /**
     * The number of the classified time series within each iteration.
     */
    protected int[] classifiedCount;
    
    protected double stepSize = 0;
    protected double progress = 0;
    protected int steps = 1;
    
    /**
     * Emptyt constructor (single-threaded, stratified splitting of the dataset).
     */
    public AbstractKNNSeedsEvaluator() {
    }

    /**
     * Constructor with single-threaded execution.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     */
    public AbstractKNNSeedsEvaluator(boolean stratified) {
        this.setStratified(stratified);
    }
    
    /**
     * Constructor with the number of threads, stratified splitting of the dataset.
     * 
     * @param tnumber number of threads.
     */
    public AbstractKNNSeedsEvaluator(int tnumber) {
        super(tnumber);
    }

    /**
     * Constructor with the number of threads, and parallel tuning.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     * @param tnumber    number of threads.
     */
    public AbstractKNNSeedsEvaluator(boolean stratified, int tnumber) {
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
    public AbstractKNNSeedsEvaluator(int tnumber, boolean fullParallel) {
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
    public AbstractKNNSeedsEvaluator(boolean stratified, int tnumber, boolean fullParallel) {
        this(stratified, null, tnumber, fullParallel);
    }
    
 
    /**
     * Constructor with the seeds, stratified splitting of the dataset.
     * 
     * @param seeds random seeds used to shuffle the dataset
     */
    public AbstractKNNSeedsEvaluator(long[] seeds) {
        this.setSeeds(seeds);
    }
    
    /**
     * Constructor with the seeds.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     * @param seeds      random seeds used to shuffle the dataset
     */
    public AbstractKNNSeedsEvaluator(boolean stratified, long[] seeds) {
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
    public AbstractKNNSeedsEvaluator(long[] seeds, int tnumber) {
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
    public AbstractKNNSeedsEvaluator(boolean stratified, long[] seeds, int tnumber) {
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
    public AbstractKNNSeedsEvaluator(long[] seeds, int tnumber, boolean fullParallel) {
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
    public AbstractKNNSeedsEvaluator(boolean stratified, long[] seeds, int tnumber, boolean fullParallel) {
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
        return seeds;
    }

    /**
     * Sets the random seeds to be used to shuffle the dataset.
     * 
     * @param the random seeds to be used to shuffle the dataset
     */
    public void setSeeds(long[] seeds) {
        this.seeds = seeds;
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
    public KNNFoldResult[] getResults() {
        return results;
    }

    // initialization
    protected void initLoop(int iterations, int k) {
        misclassified = new int[k];
        error = new double[k];
        progress = -1;
        steps = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
        results = new KNNFoldResult[iterations];
        classifiers = new KNNClassifier[iterations];
        classified = new boolean[iterations][];
        classifiedCount = new int[iterations];
    }
    
    /**
     * Evaluates the specified {@code classifier} using the specified
     * {@code dataset}.
     * 
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations number of iterations
     * @param cbCount    number of possible callbacks
     * @return the error rate
     * @throws Exception if an error occurs
     */
    protected double[] evaluate(KNNClassifier classifier, Dataset dataset, int iterations, int cbCount) throws Exception {

        // callback initialization
        if (callback != null) {
            stepSize = (double) callback.getDesiredCallbackNumber() / cbCount;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(cbCount + 1);
                stepSize = 1;
            }
        }
        
        final int k = classifier.getK();

        // initializing
        if (!insideLoop)
            initLoop(iterations, k);
        else if (callback != null)
            callback.setCallbackCount(steps);

        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

        // check the conditions for parallel training and evaluating multiple copies of the classifier
        boolean parallelTuning = (tnumber > 1) &&
                                 this.isFullParallel() &&
                                 (classifier instanceof Copyable);
        
        if (parallelTuning)
            evaluateParallelTraining(classifier, dataset, iterations, tnumber);
        else
            evaluateSequentialTraining(classifier, dataset, iterations, tnumber);

        // calculating error rates
        for (int i = 0; i < k; i++) {
            double err = 0;
            int mc = 0;
            for (KNNFoldResult result : results) {
                result.error[i] = (double) result.misclassified[i] / result.testset.size();
                mc += result.misclassified[i];
                err += result.error[i];
            }
            misclassified[i] = mc;
            error[i] = err / iterations;
        }

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
     * Training task.
     */
    protected class TrainTask implements Runnable {

        /**
         * The dataset.
         */
        static Dataset dataset;
        
        /**
         * The classifier.
         */
        static KNNClassifier classifier;
        
        /**
         * The sequence number of the iteration.
         */
        int iteration;
        
        /**
         * A copy of the classifier that is to be tuned.
         */
        KNNClassifier classifierCopy;
        
        /**
         * Constructs a new tuning task for the specified {@code index}.
         * 
         * @param index the index of this task
         */
        public TrainTask(int iteration) {
            this.iteration = iteration;
        }

        @Override
        public void run() {

            final int k = classifier.getK();
            
            // creating a new result object
            KNNFoldResult result = new KNNFoldResult();
            result.error = new double[k];
            result.misclassified = new int[k];
            
            List<Dataset> list = splitDataset(TrainTask.dataset, iteration);
            
            result.trainset = list.get(0);
            result.testset = list.get(1);
            
            classified[iteration] = new boolean[result.testset.size()];
                
            classifierCopy = (KNNClassifier) ((Copyable)classifier).makeACopy(false);
            
            try {
                classifierCopy.fit(result.trainset);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            classifiers[iteration] = classifierCopy;

            results[iteration] = result;
            
            return;
            
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
         * specified {@code run}.
         */
        int tsIndex;
        
        /**
         * Constructs a new classification task.
         * 
         * @param run     run index
         * @param tsIndex index of the time series to be classified within the test set
         *                of the specified {@code run}
         */
        public ClassifyTask(int iteration, int tsIndex) {
            this.iteration = iteration;
            this.tsIndex = tsIndex;
        }

        @Override
        public void run() {
            
            TimeSeries ts = results[iteration].testset.get(tsIndex);
            
            KNNClassifier classifier = classifiers[iteration];
            
            final int k = classifier.getK();
            
            // classifying
            double[] labels;
            try {
                labels = classifier.classifyK(ts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            classified[iteration][tsIndex] = true;
            
            // updating the results
            synchronized(results[iteration]) {
                classifiedCount[iteration]++;
                for (int n = 0; n < k; n++)
                    if (labels[n] != ts.getLabel())
                        results[iteration].misclassified[n]++;
            }
            
            // calling back
            if (callback != null)
                synchronized(callback) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        try {
                            callback.callback(AbstractKNNSeedsEvaluator.this);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            
        }
        
    }

    /**
     * Performs multiple iterations of classifier evaluation with sequential
     * training and sequential or parallel (if {@code tnumber} is greater than 1)
     * classification.
     * 
     * @param classifier the classifier to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations the number of iterations
     * @param tnumber    the number of threads
     * @throws Exception if an error occurs during evaluation
     */
    protected void evaluateSequentialTraining(KNNClassifier classifier, Dataset dataset, int iterations, int tnumber) throws Exception {
        
        if (tnumber > 1)
            this.initExecutor(tnumber);
        
        final int k = classifier.getK();

        for (int iteration = 0; iteration < iterations; iteration++) {

            if (Thread.interrupted())
                throw new InterruptedException();
            
            // if the classifier has already been evaluated on the test set
            if (results[iteration] != null && classifiedCount[iteration] == results[iteration].testset.size())
                    continue;

            KNNFoldResult result = new KNNFoldResult();
            result.error = new double[k];
            result.misclassified = new int[k];
            
            List<Dataset> list = splitDataset(dataset, iteration);
            
            // preparing the test and tuning sets
            result.trainset = list.get(0);
            result.testset = list.get(1);
            
            classifier.fit(result.trainset);

            results[iteration] = result;
            classifiers[iteration] = classifier;
            classified[iteration] = new boolean[result.testset.size()];
            
            int tsize = result.testset.size();
            
            // multi-threaded testing
            if (tnumber > 1) {

                // initializing 
                List<ClassifyTask> tasks = new ArrayList<>(tsize);

                for (int i = 0; i < tsize; i++)
                    if (!classified[iteration][i])
                        tasks.add(new ClassifyTask(iteration, i));
                
                // only single-threaded classifiers are supported
                // - if the same classifier is used in multiple threads, the same executor will
                // deal with all the tasks generated in those threads
                int ctnumber = 0;
                if (classifier instanceof Multithreaded mc && tnumber > 1) {
                    ctnumber = mc.getNumberOfThreads();
                    mc.setNumberOfThreads(1);
                }

                try {
                    ThreadUtils.startRunnables(executor, tasks);
                }
                finally {
                    // restoring the thread number of multithreaded classifiers
                    if (classifier instanceof Multithreaded mc && tnumber > 1)
                        mc.setNumberOfThreads(ctnumber);
                }
                
            }
            
            // single-threaded testing
            else {
                
                for (int i = 0; i < tsize; i++) {
                    
                    if (Thread.interrupted())
                        throw new InterruptedException();
                    
                    TimeSeries ts = result.testset.get(i);
                    
                    double[] labels = classifier.classifyK(ts);
                    
                    classified[iteration][i] = true;
                    
                    classifiedCount[iteration]++;
                    for (int n = 0; n < k; n++)
                        if (labels[n] != ts.getLabel())
                            results[iteration].misclassified[n]++;
                    
                    // calling back
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
            
            for (int n = 0; n < k; n++)
                result.error[n] = (double) result.misclassified[n] / tsize;

            // reseting the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();
            
        }
        
    }
    
    /**
     * Performs multiple iterations of classifier evaluation with parallel training and
     * classification.
     * 
     * @param classifier the classifier to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations the number of iterations
     * @param tnumber    the number of threads
     * @throws Exception if an error occurs during evaluation
     */
    protected void evaluateParallelTraining(KNNClassifier classifier, 
                                            Dataset dataset, 
                                            int iterations, 
                                            int tnumber) throws Exception {

        this.initExecutor(tnumber);
        
        TrainTask.dataset = dataset;
        TrainTask.classifier = classifier;
        
        // creating tune tasks and executing them
        List<TrainTask> trainTasks = new ArrayList<>();
        
        for (int iteration = 0; iteration < iterations; iteration++ )
            if (results[iteration] == null)
                trainTasks.add(new TrainTask(iteration));
        
        ThreadUtils.startRunnables(executor, trainTasks);
        
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
    protected void init(AbstractKNNSeedsEvaluator copy, boolean deep) {
        copy.setStratified(this.isStratified());
        copy.setSeeds(this.seeds);
    }
    
}

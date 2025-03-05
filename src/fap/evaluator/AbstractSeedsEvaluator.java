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
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.evaluator.Evaluator;
import fap.core.trainer.Trainer;
import fap.trainer.ParameterTrainer;
import fap.util.Copier;
import fap.util.Copyable;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;
import fap.util.ThreadUtils.RunnableWrapper;

/**
 * Defines common methods and fields for classifier evaluators with seeds.
 * 
 * If the number of threads is greater than 1, parallel training is applied by
 * default. This means that copies of the classifier will be trained in parallel
 * using copies of the trainer, and then the time series of all the test subsets
 * are classified in parallel as well. This requires that both the trainer and
 * the classifier implement the {@link Copyable} interface (or that there is no
 * trainer and the classifier implements it). If this condition is not met (or
 * the number of seeds is less than 2), it reverts to sequential training of the
 * classifier and parallel classification only the time series belonging to
 * individual test subsets.
 * 
 * @author Zoltán Gellér
 * @version 2025.03.05.
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
     * Indicates whether to apply parallel training and evaluating the classifier
     * wich requires that both the trainer and the classifier implement the
     * {@link Copyable} interface (or that there is no trainer). It they do not
     * imeplemnt it, the algorithm will revert to sequential training (and parallel
     * evaluation). Default value is {@code true}.
     */
    protected boolean fullParallel = true;
    
    /**
     * An array containing the evaluation results obtained for each seed. A
     * {@code null} value indicates that the given seed has not yet been processed.
     */
    protected FoldResult[] results;
    
    /**
     * List of trainer copies used by the parallel implementation.
     */
    protected ConcurrentLinkedQueue<Trainer> trainers;
    
    /**
     * A trained classifier for each run.
     */
    protected Classifier[] classifiers;
    
    /**
     * Indicates whether the specified time series has already been classified.
     */
    protected boolean[][] classified;
    
    /**
     * The number of the classified time series within each run.
     */
    protected int[] classifiedCount;
    
    /**
     * New futures created within the trainer tasks.
     */
    private transient ConcurrentLinkedQueue<Future<?>> newFutures;
    
    protected double stepSize = 0;
    protected double progress = 0;
    protected int steps = 1;
    
    /**
     * Emptyt constructor (single-threaded, stratified splitting of the dataset,
     * parallel training).
     */
    public AbstractSeedsEvaluator() {
    }
    
    /**
     * Constructor with single-threaded execution.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     */
    public AbstractSeedsEvaluator(boolean stratified) {
        this.setStratified(stratified);
    }
    
    /**
     * Constructor with the number of threads, stratified splitting of the
     * dataset, and parallel training.
     * 
     * @param tnumber number of threads.
     */
    public AbstractSeedsEvaluator(int tnumber) {
        super(tnumber);
    }

    /**
     * Constructor with the number of threads, and parallel training.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     * @param tnumber    number of threads.
     */
    public AbstractSeedsEvaluator(boolean stratified, int tnumber) {
        super(tnumber);
        this.setStratified(stratified);
    }
    

    /**
     * Constructor with the number of threads, and stratified splitting of the
     * dataset.
     * 
     * @param tnumber      number of threads.
     * @param fullParallel indicates whether to apply full parallel training
     *                     ({@code true})
     */
    public AbstractSeedsEvaluator(int tnumber, boolean fullParallel) {
        this(null, tnumber, fullParallel);
    }

    /**
     * Constructor with the number of threads.
     * 
     * @param stratified   indicates whether to use stratified ({@code true}) or
     *                     random ({@code false}) splitting
     * @param tnumber      number of threads.
     * @param fullParallel indicates whether to apply full parallel training
     *                     ({@code true})
     */
    public AbstractSeedsEvaluator(boolean stratified, int tnumber, boolean fullParallel) {
        this(null, tnumber, fullParallel);
    }
    
    /**
     * Constructor with the seeds, stratified splitting of the dataset, and parallel
     * training.
     * 
     * @param seeds random seeds used to shuffle the dataset
     */
    public AbstractSeedsEvaluator(long[] seeds) {
        this.setSeeds(seeds);
    }

    /**
     * Constructor with the seeds, and parallel training.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     * @param seeds      random seeds used to shuffle the dataset
     */
    public AbstractSeedsEvaluator(boolean stratified, long[] seeds) {
        this.setStratified(stratified);
        this.setSeeds(seeds);
    }
    
    /**
     * Constructor with the seeds and the number of threads, stratified splitting of
     * the dataset, and parallel training.
     * 
     * @param seeds   random seeds used to shuffle the dataset
     * @param tnumber number of threads
     */
    public AbstractSeedsEvaluator(long[] seeds, int tnumber) {
        super(tnumber);
        this.setSeeds(seeds);
    }
    
    /**
     * Constructor with the seeds and the number of threads, and parallel training.
     * 
     * @param stratified indicates whether to use stratified ({@code true}) or
     *                   random ({@code false}) splitting
     * @param seeds      random seeds used to shuffle the dataset
     * @param tnumber    number of threads
     */
    public AbstractSeedsEvaluator(boolean stratified, long[] seeds, int tnumber) {
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
     * @param fullParallel indicates whether to apply full parallel training
     *                     ({@code true})
     */
    public AbstractSeedsEvaluator(long[] seeds, int tnumber, boolean fullParallel) {
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
     * @param fullParallel indicates whether to apply full parallel training
     *                     ({@code true})
     */
    public AbstractSeedsEvaluator(boolean stratified, long[] seeds, int tnumber, boolean fullParallel) {
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
     * Returns {@code true} if parallel training (and evaluating) should be applied.
     * 
     * @return {@code true} if parallel training (and evaluating) should be applied
     */
    public boolean isFullParallel() {
        return fullParallel;
    }
    
    /**
     * Indicates whether parallel training (and evaluating) should be applied.
     * 
     * @param fullParallel indicates whether parallel training (and evaluating)
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
     * Evaluates the specified {@code classifier} using the specified
     * {@code trainer} and {@code dataset}.
     * 
     * @param trainer    the trainer that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations number of iterations
     * @param cbCount    number of possible callbacks
     * @return the error rate
     * @throws Exception if an error occurs
     */
    protected double evaluate(Trainer trainer, Classifier classifier, Dataset dataset, int iterations, int cbCount) throws Exception {

        // callback initialization
        if (callback != null) {
            stepSize = (double) callback.getDesiredCallbackNumber() / cbCount;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(cbCount + 1);
                stepSize = 1;
            }
        }

        // initializing
        if (!insideLoop)
            initLoop(iterations);
        else if (callback != null)
            callback.setCallbackCount(steps);

        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

        ClassifyTask.tnumber = tnumber;
        
        // checking conditions for parallel training
        boolean parallelTraining = (tnumber > 1) && 
                                   this.isFullParallel() && 
                                   ((trainer == null) || (trainer instanceof Copyable)) && 
                                   (classifier instanceof Copyable);

        if (parallelTraining)
            evaluateParallelTraining(trainer, classifier, dataset, iterations, tnumber);
        else
            evaluateSequentialTraining(trainer, classifier, dataset, iterations, tnumber);

        // calculating error rates
        error = 0;
        for (FoldResult result : results) {
            result.error = (double) result.misclassified / result.testset.size();
            misclassified += result.misclassified;
            error += result.error;
        }

        // finalizing
        error = error / iterations;
        insideLoop = false;
        done = true;
        if (callback != null)
            callback.setCallbackCount(-1);

        return error;

    }
    
    /**
     * It should split the dataset for the specified {@code iteration} into training
     * and test subsets, in a thread-safe manner.
     * 
     * <p>
     * The zeroth element of the resulting list should represent the training set,
     * and the first element should represent the test set.
     * 
     * @param iteration iteration sequence number
     * @return the list containing the training and test set (in this order)
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
         * The trainer.
         */
        static Trainer trainer;
        
        /**
         * The classifier.
         */
        static Classifier classifier;
        
        /**
         * The sequence number of the iteration.
         */
        int iteration;
        
        /**
         * A copy of the trainer used to train the classifier.
         */
        Trainer trainerCopy;
        
        /**
         * A copy of the classifier that is to be trained.
         */
        Classifier classifierCopy;
        
        /**
         * Constructs a new training task for the specified {@code index}.
         * 
         * @param index the index of this task
         */
        public TrainTask(int iteration) {
            this.iteration = iteration;
        }

        @Override
        public void run() {

            // creating a new result object
            FoldResult result = new FoldResult();
            
            List<Dataset> list = splitDataset(TrainTask.dataset, iteration);
            
            result.trainset = list.get(0);
            result.testset = list.get(1);
            
            classified[iteration] = new boolean[result.testset.size()];
            
            // if there is no need for training
            if (trainer == null) {
                
                classifierCopy = (Classifier) ((Copyable)classifier).makeACopy(false);
                
                try {
                    classifierCopy.initialize(result.trainset);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
                classifiers[iteration] = classifierCopy;

                results[iteration] = result;
                
                return;
                
            }
            
            // getting a trainer
            trainerCopy = trainers.poll();
            
            // deep copy of the classifier is needed only if the trainer affects the
            // underlying distance measure
            classifierCopy = (Classifier) ((Copyable)classifier).makeACopy(trainerCopy.affectsDistance());
            
            // training and updating the result object 
            try {
                result.expectedError = trainerCopy.train(classifierCopy, result.trainset);
                if (trainerCopy instanceof ParameterTrainer<?> pt)
                    result.bestParams = pt.getParameters();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                classifierCopy.initialize(result.trainset);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            classifiers[iteration] = classifierCopy;
            results[iteration] = result;
            
            // reseting the trainer
            if (trainerCopy instanceof Resumable rt)
                rt.reset();

            // releasing the trainer
            trainers.add(trainerCopy);
            
            // creating classifying tasks and executing them
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
        
        static int tnumber;
        
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
         * @param run run index
         * @param ts  index of the time series to be classified within the test set of
         *            the specified {@code run}
         */
        public ClassifyTask(int iteration, int tsIndex) {
            this.iteration = iteration;
            this.tsIndex = tsIndex;
        }

        @Override
        public void run() {
            
            TimeSeries ts = results[iteration].testset.get(tsIndex);
            
            Classifier classifier = classifiers[iteration];

            // classifying
            double label = Double.NaN;
            try {
                label = classifier.classify(ts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            classified[iteration][tsIndex] = true;
            
            // updating the results
            synchronized(results[iteration]) {
                classifiedCount[iteration]++;
                if (label != ts.getLabel())
                    results[iteration].misclassified++;
            }
            
            // calling back
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
     * Multiple iterations of classifier evaluation with sequential training (and
     * parallal evaluating).
     * 
     * @param trainer    the trainer that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations number of iterations
     * @param tnumber    number of threads
     * @throws Exception if an error occurs
     */
    protected void evaluateSequentialTraining(Trainer trainer, 
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

            // if the classifier has already been trained and evaluated on the test set
            if (results[iteration] != null && classifiedCount[iteration] == results[iteration].testset.size())
                    continue;
            
            // if the classifier needs training
            else {

                result = new FoldResult();
                
                List<Dataset> list = splitDataset(dataset, iteration);
                
                // preparing the test and training sets
                result.trainset = list.get(0);
                result.testset = list.get(1);
                
                // training the classifier
                double expectedError = Double.NaN;
                if (trainer != null)
                    expectedError = trainer.train(classifier, result.trainset);
                
                classifier.initialize(result.trainset);

                result.expectedError = expectedError;
                result.bestParams = null;
                if (trainer instanceof ParameterTrainer<?> pt)
                    result.bestParams = pt.getParameters();

                results[iteration] = result;
                classifiers[iteration] = classifier;
                classified[iteration] = new boolean[result.testset.size()];
                
                // reseting the trainger
                if (trainer instanceof Resumable rt)
                    rt.reset();
                
                if (Thread.interrupted())
                    throw new InterruptedException();

            }
            
            result = results[iteration];

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
                    
                    double label = classifier.classify(ts);
                    
                    classified[iteration][i] = true;
                    
                    classifiedCount[iteration]++;
                    if (label != ts.getLabel())
                        results[iteration].misclassified++;
                    
                    // calling back
                    if (callback != null) {
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
            
            result.error = (double) result.misclassified / tsize;

            // reseting the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();
            
        }
        
    }
    
    /**
     * Multiple iterations of classifier evaluation with parallel training (and evaluating).
     * 
     * @param trainer    the trainer that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param iterations number of iterations
     * @param tnumber    number of threads
     * @throws Exception if an error occurs
     */
    protected void evaluateParallelTraining(Trainer trainer, 
                                            Classifier classifier, 
                                            Dataset dataset,
                                            int iterations,
                                            int tnumber) throws Exception {


        this.initExecutor(tnumber);
        
        // preparing the trainers
        trainers = new ConcurrentLinkedQueue<>();
        if (trainer != null)
            Copier.makeCopies(trainer, trainers, tnumber);
        
        TrainTask.dataset = dataset;
        TrainTask.trainer = trainer;
        TrainTask.classifier = classifier;
        
        // creating train tasks and executing them
        List<TrainTask> trainTasks = new ArrayList<>();
        
        for (int iteration = 0; iteration < iterations; iteration++ )
            if (results[iteration] == null)
                trainTasks.add(new TrainTask(iteration));
        
        newFutures = new ConcurrentLinkedQueue<>();
        
        ThreadUtils.startRunnables(executor, trainTasks);
        
        ThreadUtils.waitForFutures(executor, newFutures);
        
        if (Thread.interrupted())
            throw new InterruptedException();
        
        // creating classifying tasks and executing them
        List<ClassifyTask> classifyTasks = new ArrayList<>();
        
        for (int iteration = 0; iteration < iterations; iteration++)
            for (int index = 0; index < results[iteration].testset.size(); index++)
                if (!classified[iteration][index])
                    classifyTasks.add(new ClassifyTask(iteration, index));
        
        if (Thread.interrupted())
            throw new InterruptedException();
        
        ThreadUtils.startRunnables(executor, classifyTasks);
        
        trainers = null;
        classifiers = null;
        classified = null;
        classifiedCount = null;

    }
    

    /**
     * Sets the parameters of the specified evaluator to be equal to the parameters
     * of this evaluator.
     * 
     * @param copy the evaluator whose parameters are to be set
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(AbstractSeedsEvaluator copy, boolean deep) {
        copy.setStratified(this.isStratified());
        copy.setSeeds(this.getSeeds()); // consider making a copy
    }
    
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import fap.callback.Callback;
import fap.callback.Callbackable;
import fap.classifier.nn.KNNClassifier;
import fap.data.Dataset;
import fap.data.TimeSeries;
import fap.exception.EmptyDatasetException;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;

/**
 * A basic evaluator of kNN classifiers leveraging their ability to classify the
 * given time series for each neighbor count from {@code 1} to {@code k}, where
 * {@code k} denotes the number of nearest neighbors.
 * 
 * <p>
 * It does not implement the {@link fap.evaluator.KNNEvaluator KNNEvaluator}
 * interface.
 * 
 * <p>
 * It evaluates a classifier based on the specified test and train sets.
 * 
 * <p>
 * <em>Note:</em> Since classification is performed in a single pass for all
 * neighbor counts from {@code 1} to {@code k}, tuning another hyperparameter
 * individually for each number of nearest neighbors is not supported.
 * 
 * @author Zoltán Gellér
 * @version 2026.10.17.
 * @see Serializable
 * @see Callbackable
 * @see Resumable
 * @see Multithreaded
 */
public class KNNBasicEvaluator implements Serializable, Callbackable, Resumable, Multithreaded {

    private static final long serialVersionUID = 1L;

    /**
     * The Callback object.
     */
    private transient Callback callback;

    /**
     * The number of threads. Default value is {@code 1}.
     */
    private int numberOfThreads = 1;

    /**
     * The executor service used for implementing multithreaded evaluation.
     */
    private transient ThreadPoolExecutor executor;

    /**
     * Average error rates for each neighbor count from {@code 1} to {@code k}.
     */
    private double[] error;

    /**
     * Number of misclassified time series for each neighbor count from {@code 1} to {@code k}.
     */
    private int[] misclassfied;

    /**
     * Indicates whether evaluating has completed.
     */
    private boolean done;

    /**
     * Indicates whether evaluating has started.
     */
    private boolean insideLoop;

    /**
     * The number of time series classified so far.
     */
    private int counter;

    /**
     * The predicted labels of each time series from the test set for each
     * neighbor count from {@code 1} to {@code k}.
     * 
     * <p>
     * The matrix is of size {@code n} × {@code k}, where {@code n} is the
     * number of elements in the test set, and {@code k} is the upper bound on the
     * number of nearest neighbors. If the value in the {@code i}-th row of the
     * matrix is {@code null}, it means that the {@code i}-th element of the test
     * set has not yet been classified.
     */
    private double[][] labels;

    private double stepSize;
    private double progress;
    private int steps = 1;
    
    /**
     * Constructs a new single-threaded basic evaluator.
     */
    public KNNBasicEvaluator() {
    }
    
    // initialization
    private void init(int size, int k) {
        misclassfied = new int[k];
        error = new double[k];
        progress = -1;
        steps = 0;
        counter = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
        labels = new double[size][];
    }

    /**
     * Evaluates the error rates of the specified kNN classifier using the given
     * test and training datasets for each neighbor count from {@code 1} to {@code k}.
     * 
     * @param classifier the classifier to be evaluated
     * @param testset    the test set
     * @param trainset   the training set
     * @return an array containing the error rates of the evaluated kNN classifier
     *         for each neighbor count from {@code 1} to {@code k}
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs during evaluation
     */
    public double[] evaluate(KNNClassifier classifier, Dataset testset, Dataset trainset) throws Exception {

        Objects.requireNonNull(classifier, "The classifier cannot be null.");
        Objects.requireNonNull(testset, "The test set cannot be null.");
        Objects.requireNonNull(trainset, "The training set cannot be null.");
        EmptyDatasetException.check(testset, "test");
        EmptyDatasetException.check(trainset, "training");

        done = false;

        final int tsize = testset.size();

        // initialize callback
        if (callback != null) {
            stepSize = (double) callback.getDesiredCallbackNumber() / tsize;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(tsize + 1);
                stepSize = 1;
            }
        }
        
        final int k = classifier.getK();

        // initialize
        if (!insideLoop)
            init(tsize, k);
        else if (callback != null)
            callback.setCallbackCount(steps);

        // initialize the classifier
        classifier.fit(trainset);
        
        // evaluate
        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

        if (tnumber > 1)
            evaluateMultithreaded(classifier, testset, tnumber);
        else
            evaluateSinglethreaded(classifier, testset);
        
        // finalize
        for (int i = 0; i < k; i++)
            error[i] = (double) misclassfied[i] / tsize;
        insideLoop = false;
        done = true;
        if (callback != null) {
            callback.callback(this);
            callback.setCallbackCount(-1);
        }
        
        // reset the classifier
        if (classifier instanceof Resumable rc)
            rc.reset();
        
        return error;

    }

    /**
     * Performs single-threaded evaluation of the classifier.
     * 
     * @param classifier the classifier to be evaluated
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs during evaluation
     */
    private void evaluateSinglethreaded(KNNClassifier classifier, Dataset testset) throws Exception {

        final int tsize = testset.size();
        
        final int k = classifier.getK();
        
        // evaluate
        for (int i = 0; i < tsize; i++) {

            if (labels[i] != null)
                continue;

            if (Thread.interrupted())
                throw new InterruptedException();

            TimeSeries testSeries = testset.get(i);

            double[] klabels = classifier.classifyK(testSeries);

            counter++;
            final double tsLabel = testSeries.getLabel();
            for (int n = 0; n < k; n++)
                if (tsLabel != klabels[n])
                    misclassfied[n]++;

            labels[i] = klabels;

            // call back
            if (callback != null) {
                progress += stepSize;
                if (progress >= steps) {
                    steps++;
                    for (int n = 0; n < k; n++)
                        error[n] = (double) misclassfied[n] / counter;
                    callback.callback(this);
                }
            }

        }

    }
    
    /**
     * Auxiliary class for the multi-threaded implementation. 
     */
    protected class EvaluatorTask implements Runnable {

        /**
         * The classifier to be evaluated.
         */
        public static KNNClassifier classifier;
        
        /**
         * The test set.
         */
        public static Dataset testset;

        /**
         * The index of the time series to be classified.
         */
        private int index;
        
        /**
         * The auxiliary object used for synchronizing threads.
         */
        private static Object semaphore = new Object();
        
        public EvaluatorTask(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            
            // the time series to be tested
            TimeSeries ts = testset.get(index);
            
            final int k = classifier.getK();
            
            // test
            double[] klabels;
            try {
                klabels = classifier.classifyK(ts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            labels[index] = klabels;
            
            synchronized(semaphore) {
                
                // update the results
                counter++;
                final double tsLabel = ts.getLabel();
                for (int i = 0; i < k; i ++)
                    if (tsLabel != klabels[i])
                        misclassfied[i]++;
                
                // call back
                if (callback != null) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        for (int i = 0; i < k; i++)
                            error[i] = (double) misclassfied[i] / counter;
                        try {
                            callback.callback(KNNBasicEvaluator.this);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                
            }
            
        }
        
    }

    /**
     * Performs multi-threaded evaluation of the classifier.
     * 
     * @param classifier the classifier to be evaluated
     * @param testset    the test set
     * @param tnumber    the number of threads
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs during evaluation
     */
    private void evaluateMultithreaded(KNNClassifier classifier, Dataset testset, int tnumber) throws Exception {
        
        final int tsize = testset.size();
        
        // prepare the executor service and the tasks 

        this.initExecutor(tnumber);
        
        EvaluatorTask.testset = testset;
        EvaluatorTask.classifier = classifier;
        
        List<EvaluatorTask> tasks = new ArrayList<>();
        
        for (int i = 0; i < tsize; i++ )
            if (labels[i] == null)
                tasks.add(new EvaluatorTask(i));

        // only single-threaded classifiers are supported
        // - if the same classifier is used in multiple threads, the same executor will
        // deal with all the tasks generated in those threads
        // TODO: if the classifier is copyable, make copies
        int ctnumber = 0;
        if (classifier instanceof Multithreaded mc) {
            ctnumber = mc.getNumberOfThreads();
            mc.setNumberOfThreads(1);
        }

        try {
            ThreadUtils.startRunnables(executor, tasks);
        }
        finally {
            // restore the thread number of multithreaded classifiers
            if (classifier instanceof Multithreaded mc)
                mc.setNumberOfThreads(ctnumber);
        }
        
    }

    /**
     * Returns the predicted labels of each time series from the test set for each
     * neighbor count from {@code 1} to {@code k}.
     * 
     * <p>
     * The returned matrix is of size {@code n} × {@code k}, where {@code n} is the
     * number of elements in the test set, and {@code k} is the upper bound on the
     * number of nearest neighbors. If the value in the {@code i}-th row of the
     * matrix is {@code null}, it means that the {@code i}-th element of the test
     * set has not yet been classified.
     * 
     * @return the predicted labels
     */
    public double[][] getLabels() {
        return labels;
    }

    /**
     * Returns the error rates of the evaluated kNN classifier for each neighbor
     * count from {@code 1} to {@code k}.
     *
     * @return an array containing the error rates of the evaluated kNN classifier
     *         for each neighbor count from {@code 1} to {@code k}
     */
    public double[] getError() {
        return error;
    }

    /**
     * Returns the number of misclassified time series for each neighbor count from
     * {@code 1} to {@code k}.
     * 
     * @return an array containing the number of misclassified time series for each
     *         neighbor count from {@code 1} to {@code k}
     */
    public int[] getMisclassified() {
        return misclassfied;
    }

    /**
     * Initializes the executor service.
     * 
     * @param tnumber the number of threads
     * @see ThreadUtils#init
     */
    protected void initExecutor(int tnumber) {
        executor = ThreadUtils.init(executor, tnumber);
    }

    @Override
    public void setNumberOfThreads(int tnumber) {
        this.numberOfThreads = tnumber;
        initExecutor(tnumber);
    }

    @Override
    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public Callback getCallback() {
        return this.callback;
    }

    @Override
    public void reset() {
        this.done = false;
        this.insideLoop = false;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public boolean isInProgress() {
        return this.insideLoop;
    }
    
    @Override
    public void shutdown() {
        if (executor != null)
            executor.shutdown();
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

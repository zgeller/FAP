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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import fap.callback.Callback;
import fap.callback.Callbackable;
import fap.classifier.Classifier;
import fap.data.Dataset;
import fap.data.TimeSeries;
import fap.exception.EmptyDatasetException;
import fap.tuner.Tuner;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;

/**
 * A basic classifier evaluator that does not implement the
 * {@link fap.evaluator.Evaluator Evaluator} interface.
 * 
 * <p>
 * Evaluates a classifier based on the specified test and training sets.
 * 
 * <p>
 * In parallel evaluation, the thread count of the classifier is set to 1 (under
 * the assumption that the executor used by the classifier is instantiated once,
 * rather than per {@code classify} method call) and restored once the
 * evaluation is complete.
 * 
 * @author Zoltán Gellér
 * @version 2026.10.18.
 * @see Serializable
 * @see Callbackable
 * @see Resumable
 * @see Multithreaded
 */
public class BasicEvaluator implements Serializable, Callbackable, Resumable, Multithreaded {

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
     * The expected error rate (the lowest error rate on the training set). The
     * default value is {@code Double.NaN} (indicating that no tuning was
     * performed).
     */
    private double expectedError = Double.NaN;
    
    /**
     * The average error rate.
     */
    private double error;

    /**
     * The number of misclassified time series.
     */
    private int misclassfied;

    /**
     * Indicates whether evaluating has completed.
     */
    private boolean done;

    /**
     * Indicates whether evaluating has started.
     */
    private boolean insideLoop;

    /**
     * The number of time series tested so far.
     */
    private int counter;

    /**
     * The predicted labels. A {@code null} value indicates that the given time
     * series has not yet been classified.
     */
    private Double[] labels;

    private double stepSize;
    private double progress;
    private int steps = 1;

    /**
     * Constructs a new single-threaded basic evaluator.
     */
    public BasicEvaluator() {
    }

    /**
     * Constructs a new basic evaluator with the specified number of threads.
     * 
     * @param tnumber the number of threads
     */
    public BasicEvaluator(int tnumber) {
        this.setNumberOfThreads(tnumber);
    }

    // initialization
    private void init(int size) {
        misclassfied = 0;
        expectedError = Double.NaN;
        error = 0.0;
        progress = -1;
        steps = 0;
        counter = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
        labels = new Double[size];
    }

    /**
     * Evaluates the error rate of the specified classifier using the given test and
     * training sets.
     * 
     * @param tuner      the tuner used to train the classifier
     * @param classifier the classifier to be evaluated
     * @param testset    the test set
     * @param trainset   the training set
     * @return the error rate of the classifier
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs during evaluation
     */
    public double evaluate(Tuner tuner, Classifier classifier, Dataset testset, Dataset trainset) throws Exception {

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

        // initialize
        if (!insideLoop)
            init(tsize);
        else if (callback != null)
            callback.setCallbackCount(steps);

        // tune the classifier
        if (tuner != null)
            expectedError = tuner.tune(classifier, trainset);
        
        // initialize the classifier
        classifier.fit(trainset);
        
        // evaluate
        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());
        
        if (tnumber > 1)
            evaluateMultithreaded(classifier, testset, tnumber);
        else
            evaluateSinglethreaded(classifier, testset);
        
        // finalize
        error = (double) misclassfied / tsize;
        insideLoop = false;
        done = true;
        if (callback != null) {
            callback.callback(this);
            callback.setCallbackCount(-1);
        }
        
        // reset the tuner
        if (tuner instanceof Resumable rt)
            rt.reset();

        return error;

    }

    /**
     * Performs single-threaded evaluation of the classifier.
     * 
     * @param classifier the classifier to be evaluated
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs during evaluation
     */
    private void evaluateSinglethreaded(Classifier classifier, Dataset testset) throws Exception {

        final int tsize = testset.size();
        
        // evaluate
        for (int i = 0; i < tsize; i++) {

            if (labels[i] != null)
                continue;

            if (Thread.interrupted())
                throw new InterruptedException();

            TimeSeries ts = testset.get(i);

            double label = classifier.classify(ts);

            counter++;
            if (ts.getLabel() != label)
                misclassfied++;

            labels[i] = label;

            // reset the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();
            
            // call back
            if (callback != null) {
                progress += stepSize;
                if (progress >= steps) {
                    steps++;
                    error = (double) misclassfied / counter;
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
        public static Classifier classifier;
        
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
            
            // classify
            double label;
            try {
                label = classifier.classify(ts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            labels[index] = label;
            
            // reset the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();
            
            synchronized(semaphore) {
                
                // update the results
                counter++;
                if (ts.getLabel() != label)
                    misclassfied++;
                
                // call back
                if (callback != null) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        error = (double) misclassfied / counter;
                        try {
                            callback.callback(BasicEvaluator.this);
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
    private void evaluateMultithreaded(Classifier classifier, Dataset testset, int tnumber) throws Exception {
        
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
     * Returns the predicted label of each time series from the test set.
     * 
     * <p>
     * A {@code null} value indicates that the given time series has not yet been
     * classified.
     * 
     * @return the predicted labels
     */
    public Double[] getLabels() {
        return labels;
    }

    /**
     * Returns the expected error rate (the lowest error rate on the training set).
     * 
     * @return the expected error rate (the lowest error rate on the training set)
     *         or {@code Double.NaN} if no tuning was performed
     */
    public double getExpectedError() {
        return expectedError;
    }
    
    /**
     * Returns the average error rate of the evaluated classifier.
     * 
     * @return the average error rate of the evaluated classifier
     */
    public double getError() {
        return error;
    }

    /**
     * Returns the number of misclassified time series.
     * 
     * @return the number of misclassified time series
     */
    public int getMisclassified() {
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

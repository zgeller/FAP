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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import fap.callback.Callback;
import fap.callback.Callbackable;
import fap.core.classifier.Classifier;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.trainer.Trainer;
import fap.exception.EmptyDatasetException;
import fap.util.Copyable;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;

/**
 * A basic classifier evaluator that does not implements the
 * {@link fap.core.evaluator.Evaluator Evaluator} interface.
 * 
 * <p>
 * It evaluates a classifier based on the specified test and train sets.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.19.
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
    private ThreadPoolExecutor executor;

    /**
     * The expected error rate (the lowest error rate on the training set). Default
     * value is NaN (indicating that there was no training).
     */
    private double expectedError = Double.NaN;
    
    /**
     * Average error rate. 
     */
    private double error;

    /**
     * Number of misclassified time series.
     */
    private int misclassfied;

    /**
     * Indicates whether the testing is completed.
     */
    private boolean done;

    /**
     * Indicates whether the testing has started.
     */
    private boolean insideLoop;

    /**
     * Number of time series tested so far.
     */
    private int counter;

    /**
     * The predicted labels. A {@code null} value indicates that the given time
     * series has not yet been tested.
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
     * @param tnumber number of threads
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
     * @param trainer    the trainer that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param testset    test set
     * @param trainset   train set
     * @return the error rate of the classifier
     * @throws InterruptedException when the interrupted flag is set
     * @throws Exception            if an error occurs
     */
    public double evaluate(Trainer trainer, Classifier classifier, Dataset testset, Dataset trainset) throws Exception {

        if (done)
            return error;

        if (testset.isEmpty())
            throw new EmptyDatasetException("The test set cannot be empty.");

        if (trainset.isEmpty())
            throw new EmptyDatasetException("The train set cannot be empty.");

        final int tsize = testset.size();

        // callback initialization
        if (callback != null) {
            stepSize = (double) callback.getDesiredCallbackNumber() / tsize;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(tsize + 1);
                stepSize = 1;
            }
        }

        // initialization
        if (!insideLoop)
            init(tsize);
        else if (callback != null)
            callback.setCallbackCount(steps);

        // training the classifier
        if (trainer != null)
            expectedError = trainer.train(classifier, trainset);
        
        // initializing the classifier
        classifier.initialize(trainset);
        
        // evaluation
        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

        if (tnumber < 2 || ((trainer != null) && !(trainer instanceof Copyable)) || !(classifier instanceof Copyable))
            evaluateSinglethreaded(classifier, testset);
        else
            evaluateMultithreaded(classifier, testset, tnumber);

        // finalization
        error = (double) misclassfied / tsize;
        insideLoop = false;
        done = true;
        if (callback != null) {
            callback.callback(this);
            callback.setCallbackCount(-1);
        }
        
        // reseting the trainger
        if (trainer instanceof Resumable rt)
            rt.reset();

        // reseting the classifier
        if (classifier instanceof Resumable rc)
            rc.reset();
        
        return error;

    }

    /**
     * Single-threaded implementation of classifier evaluation.
     * 
     * @param classifier the classifier that is to be evaluated
     * @throws InterruptedException when the interrupted flag is set
     * @throws Exception            if an error occurs
     */
    private void evaluateSinglethreaded(Classifier classifier, Dataset testset) throws Exception {

        final int tsize = testset.size();
        
        // testing
        for (int i = 0; i < tsize; i++) {

            if (labels[i] != null)
                continue;

            if (Thread.interrupted())
                throw new InterruptedException();

            TimeSeries testSeries = testset.get(i);

            double label = classifier.classify(testSeries);

            counter++;
            if (testSeries.getLabel() != label)
                misclassfied++;

            labels[i] = label;

            // calling back
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
         * The classifier that is to be evaluated.
         */
        public static Classifier classifier;
        
        /**
         * The test set.
         */
        public static Dataset testset;

        /**
         * The index of the time series that is to be classified.
         */
        private int index;
        
        /**
         * Auxiliary object used for synchronizing threads.
         */
        private static Object semaphore = new Object();
        
        public EvaluatorTask(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            
            // the time series to be tested
            TimeSeries testSeries = testset.get(index);
            
            // testing
            double label;
            try {
                label = classifier.classify(testSeries);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            labels[index] = label;
            
            synchronized(semaphore) {
                
                // updating the results
                counter++;
                if (testSeries.getLabel() != label)
                    misclassfied++;
                
                // calling back
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
     * Multi-threaded implementation of classifier evaluation.
     * 
     * @param classifier the classifier that is to be evaluated
     * @param testset    test set
     * @param tnumber    number of threads
     * @throws InterruptedException when the interrupted flag is set
     * @throws Exception            if an error occurs
     */
    private void evaluateMultithreaded(Classifier classifier, Dataset testset, int tnumber) throws Exception {
        
        final int tsize = testset.size();
        
        // preparing the executor service and the tasks 

        this.initExecutor(tnumber);
        
        EvaluatorTask.testset = testset;
        EvaluatorTask.classifier = classifier;
        
        ArrayList<EvaluatorTask> tasks = new ArrayList<>();
        
        for (int i = 0; i < tsize; i++ )
            if (labels[i] == null)
                tasks.add(new EvaluatorTask(i));

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

    /**
     * Returns the predicted labels. A {@code null} value indicates that the given
     * time series has not yet been tested.
     * 
     * @return the predicted labels
     */
    public Double[] getLabels() {
        return labels;
    }

    /**
     * The expected error rate (the lowest error rate on the training set).
     * 
     * @return the expected error rate (the lowest error rate on the training set)
     *         or NaN if there was no training
     */
    public double getExpectedError() {
        return expectedError;
    }
    
    /**
     * Returns the average error rate of the tested classifier.
     * 
     * @return the average error rate of the tested classifier
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

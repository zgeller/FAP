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
import java.util.LinkedList;
import java.util.List;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.tuner.Tuner;
import fap.exception.EmptyDatasetException;
import fap.util.Copyable;
import fap.util.Copier;
import fap.util.Resumable;
import fap.util.ThreadUtils;

/**
 * Leave-One-Out (LOO) classifier evaluator.
 *
 * <p>
 * The multi-threaded implementation requires that the tuner (if there is one)
 * and the classifier (properly) implement the {@link Copyable} interface. If
 * this condition is not met, it reverts to single-threaded implementation.
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
 *  <li> E. Alpaydin, Introduction to Machine Learning, MIT Press, 2004.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.04.22.
 * @see AbstractExtendedEvaluator
 */
public class LeaveOneOutEvaluator extends AbstractExtendedEvaluator implements Copyable {

    private static final long serialVersionUID = 1L;

    /**
     * Number of time series tested so far.
     */
    private int counter;

    /**
     * Labels returned by the classifier. A {@code null} value indicates that the
     * given time series has not yet been tested.
     */
    private Double[] labels;
    
    /**
     * The expected error rates (the lowest error rate on the training set).
     */
    private Double[] expectedErrors;
    
    private double stepSize;
    private double progress;
    private int steps;
    
    /**
     * List of tuner copies used by the multi-threaded implementation.
     */
    private LinkedList<Tuner> tuners;
    
    /**
     * List of classifier copies used by the multi-threaded implementation.
     */
    private LinkedList<Classifier> classifiers;
    
    /**
     * Constructs a new (initially) single-threaded Leave-One-Out evaluator.
     */
    public LeaveOneOutEvaluator() {
    }

    /**
     * Constructs a new Leave-One-Out evaluator with the specified number of threads.
     * 
     * @param tnumber number of threads
     */
    public LeaveOneOutEvaluator(int tnumber) {
        super(tnumber);
    }

    /**
     * Initializes the evaluation process.
     * 
     * @param size the size of the dataset
     */
    private void init(int size, Tuner tuner) {
        misclassified = 0;
        error = 0.0;
        progress = -1;
        steps = 0;
        counter = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
        labels = new Double[size];
        if (tuner != null)
            expectedErrors = new Double[size];
    }
    
    /***
     * @throws InterruptedException when the interrupted flag is set
     */
    @Override
    public double evaluate(Tuner tuner, Classifier classifier, Dataset dataset) throws Exception {
        
        if (done)
            return error;
        
        EmptyDatasetException.check(dataset);
        
        final int dsize = dataset.size();

        // callback initialization
        if (callback != null) {
            stepSize = (double) callback.getDesiredCallbackNumber() / dsize;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(dsize + 1);
                stepSize = 1;
            }
        }

        // initialization
        if (!insideLoop)
            init(dsize, tuner);
        else if (callback != null)
            callback.setCallbackCount(steps);
        
        // evaluation
        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());
        
        if (tnumber < 2 || 
            ((tuner != null) && !(tuner instanceof Copyable)) || 
            !(classifier instanceof Copyable))
            evaluateSinglethreaded(tuner, classifier, dataset);
        else
            evaluateMultithreaded(tuner, classifier, dataset, tnumber);
        
        // finalization
        error = (double) misclassified / dsize;
        insideLoop = false;
        done = true;
        if (callback != null) {
            callback.callback(this);
            callback.setCallbackCount(-1);
        }

        return error;
        
    }
    
    /**
     * Single-threaded implementation of classifier evaluation.
     * 
     * @param tuner      the tuner that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @return the error rate of the classifier
     * @throws InterruptedException when the interrupted flag is set
     * @throws Exception            if an error occurs
     */
    private void evaluateSinglethreaded(Tuner tuner, Classifier classifier, Dataset dataset) throws Exception {

        final int dsize = dataset.size();

        // testing
        for (int i = 0; i < dsize; i++) {
            
            if (labels[i] != null)
                continue;

            if (Thread.interrupted())
                throw new InterruptedException();

            // test series
            TimeSeries testSeries = dataset.remove(i);

            // tuning
            if (tuner != null)
                expectedErrors[i] = tuner.tune(classifier, dataset);

            // building the classifier
            classifier.fit(dataset);

            // testing
            double label = classifier.classify(testSeries);

            counter++;
            if (testSeries.getLabel() != label)
                misclassified++;
            
            dataset.add(i, testSeries);

            labels[i] = label;

            // reseting the trainger
            if (tuner instanceof Resumable rt)
                rt.reset();
            
            // reseting the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();
            
            // calling back
            if (callback != null) {
                progress += stepSize;
                if (progress >= steps) {
                    steps++;
                    error = (double) misclassified / counter;
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
         * The whole dataset.
         */
        public static Dataset dataset;
        
        /**
         * The index of the time series that is to be classified.
         */
        private int index;
        
        public EvaluatorTask(int index) {
            this.index = index;
        }
        
        @Override
        public void run() {
            
            // the time series to be tested
            TimeSeries testSeries = dataset.get(index);
            
            // the training set
            Dataset trainset = new Dataset(dataset);
            trainset.remove(index);
            
            // getting a tuner and a classifier
            Tuner tuner;
            Classifier classifier;
            
            synchronized(tuners) {
                tuner = tuners.poll();
                classifier = classifiers.poll();
            }
            
            // tuning
            if (tuner != null)
                try {
                    expectedErrors[index] = tuner.tune(classifier, trainset);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            // initializing the classifier
            try {
                classifier.fit(trainset);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            // testing
            double label;
            try {
                label = classifier.classify(testSeries);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            labels[index] = label;
            
            // reseting the tuner
            if (tuner instanceof Resumable rt)
                rt.reset();
            
            // reseting the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();

            // updating the results, calling back, releasing the tuner and the classifier
            synchronized(tuners) {
                
                // updating the results
                counter++;
                if (testSeries.getLabel() != label)
                    misclassified++;
                
                // calling back
                if (callback != null) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        error = (double) misclassified / counter;
                        try {
                            callback.callback(LeaveOneOutEvaluator.this);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                // releasing the tuner and the classifier
                tuners.add(tuner);
                classifiers.add(classifier);
                
            }
            
        }
        
    }

    /**
     * Multi-threaded implementation of classifier evaluation.
     * 
     * @param tuner      the tuner that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param tnumber    number of threads
     * @return the error rate of the classifier
     * @throws InterruptedException when the interrupted flag is set
     * @throws Exception            if an error occurs
     */
    private void evaluateMultithreaded(Tuner tuner, 
                                       Classifier classifier, 
                                       Dataset dataset, 
                                       int tnumber) throws Exception {

        final int dsize = dataset.size();

        // preparing the tuners and the classifiers
        
        tuners = new LinkedList<>();
        classifiers = new LinkedList<>();

        Copier.makeCopies(tuner, classifier, tuners, classifiers, tnumber);
        
        // preparing the executor service and the tasks 

        this.initExecutor(tnumber);

        EvaluatorTask.dataset = dataset;

        List<EvaluatorTask> tasks = new ArrayList<>();
        
        for (int i = 0; i < dsize; i++ )
            if (labels[i] == null)
                tasks.add(new EvaluatorTask(i));
        
        ThreadUtils.startRunnables(executor, tasks);
        
    }
    
    /**
     * Returns the labels returned by the classifier. A {@code null} value indicates
     * that the given time series has not yet been tested.
     * 
     * @return labels returned by the classifier
     */
    public Double[] getLabels() {
        return labels;
    }
    
    /**
     * Returns the expected error rates (the lowest error rates on the training
     * set).
     * 
     * @return the expected error rates (the lowest error rates on the training set)
     *         or {@code null} if there was no tuning
     */
    public Double[] getExpectedErrors() {
        return expectedErrors;
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        return new LeaveOneOutEvaluator();
    }
    
}

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
import java.util.concurrent.ConcurrentLinkedQueue;

import fap.classifier.Classifier;
import fap.classifier.nn.KNNClassifier;
import fap.data.Dataset;
import fap.data.TimeSeries;
import fap.util.Copier;
import fap.util.Copyable;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;

/**
 * Leave-One-Out (LOO) evaluator of kNN classifiers leveraging their ability to
 * classify the given time series for each neighbor count from {@code 1} to
 * {@code k}, where {@code k} denotes the number of nearest neighbors.
 *
 * <p>
 * The multi-threaded implementation requires that the the classifier implement
 * the {@link Copyable} interface. If this condition is not met, it reverts to
 * single-threaded implementation.
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
 * @version 2026.10.18.
 * @see AbstractKNNExtendedEvaluator
 * @see Copyable
 */
public class KNNLeaveOneOutEvaluator extends AbstractKNNExtendedEvaluator implements Copyable {

    private static final long serialVersionUID = 1L;

    /**
     * The number of time series tested so far.
     */
    private int counter;

    /**
     * The predicted labels of each time series from the dataset for each neighbor
     * count from {@code 1} to {@code k}.
     * 
     * <p>
     * The matrix is of size {@code n} × {@code k}, where {@code n} is the number of
     * elements in the dataset, and {@code k} is the upper bound on the number of
     * nearest neighbors. If the value in the {@code i}-th row of the matrix is
     * {@code null}, it means that the {@code i}-th element of the test set has not
     * yet been classified.
     */
    private double[][] labels;
    
    private double stepSize;
    private double progress;
    private int steps;
    
    /**
     * The list of classifier copies used by the multi-threaded implementation.
     */
    private ConcurrentLinkedQueue<Classifier> classifiers;
    
    /**
     * Constructs a new single-threaded {@code KNNLeaveOneOut} evaluator.
     */
    public KNNLeaveOneOutEvaluator() {
    }

    /**
     * Constructs a new {@code KNNLeaveOneOut} evaluator with the specified number of threads.
     * 
     * @param tnumber the number of threads
     */
    public KNNLeaveOneOutEvaluator(int tnumber) {
        super(tnumber);
    }

    /**
     * Initializes the evaluation process.
     * 
     * @param size the size of the dataset
     * @param k    the upper bound on the number of nearest neighbors
     */
    private void init(int size, int k) {
        misclassified = new int[k];
        error = new double[k];
        progress = -1;
        steps = 0;
        counter = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
        labels = new double[size][];
    }
    
    /***
     * @throws InterruptedException if the current thread has been interrupted
     */
    @Override
    public double[] evaluate(KNNClassifier classifier, Dataset dataset) throws Exception {
        
        checkConditions(classifier, dataset);
        
        done = false;
        
        final int dsize = dataset.size();

        // initialize callback
        if (callback != null) {
            stepSize = (double) callback.getDesiredCallbackNumber() / dsize;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(dsize + 1);
                stepSize = 1;
            }
        }

        final int k = classifier.getK();
        
        // initialize
        if (!insideLoop)
            init(dsize, k);
        else if (callback != null)
            callback.setCallbackCount(steps);
        
        // evaluate
        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());
        
        if (tnumber < 2 || 
            !(classifier instanceof Copyable))
            evaluateSinglethreaded(classifier, dataset);
        else
            evaluateMultithreaded(classifier, dataset, tnumber);
        
        // finalize
        for (int i = 0; i < k; i++)
            error[i] = (double) misclassified[i] / dsize;
        insideLoop = false;
        done = true;
        if (callback != null) {
            callback.callback(this);
            callback.setCallbackCount(-1);
        }

        return error;
        
    }
    
    /**
     * Performs single-threaded evaluation of the classifier.
     * 
     * @param classifier the classifier to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @return the error rate of the classifier
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs during evaluation
     */
    private void evaluateSinglethreaded(KNNClassifier classifier, Dataset dataset) throws Exception {

        final int dsize = dataset.size();

        final int k = classifier.getK();
        
        // testing
        for (int i = 0; i < dsize; i++) {
            
            if (labels[i] != null)
                continue;

            if (Thread.interrupted())
                throw new InterruptedException();

            // test series
            TimeSeries ts = dataset.remove(i);

            classifier.fit(dataset);

            double[] klabels = classifier.classifyK(ts);

            counter++;
            final double tsLabel = ts.getLabel();
            for (int n = 0; n < k; n++)
                if (tsLabel != klabels[n])
                    misclassified[n]++;
            
            dataset.add(i, ts);

            labels[i] = klabels;

            // reset the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();
            
            // call back
            if (callback != null) {
                progress += stepSize;
                if (progress >= steps) {
                    steps++;
                    for (int n = 0; n < k; n++)
                        error[n] = (double) misclassified[n] / counter;
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
            TimeSeries ts = dataset.get(index);
            
            // the training set
            Dataset trainset = new Dataset(dataset);
            trainset.remove(index);
            
            // get a classifier
            KNNClassifier classifier = (KNNClassifier) classifiers.poll();
            
            final int k = classifier.getK();
            
            // initialize the classifier
            try {
                classifier.fit(trainset);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            // classify the time series
            double[] klabels;
            try {
                klabels = classifier.classifyK(ts);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            labels[index] = klabels;
            
            // reset the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();

            // update the results, call back, release the classifier
            synchronized(classifiers) {
                
                // update the results
                counter++;
                final double tsLabel = ts.getLabel();
                for (int i = 0; i < k; i ++)
                    if (tsLabel != klabels[i])
                        misclassified[i]++;
                
                // call back
                if (callback != null) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        for (int i = 0; i < k; i++)
                            error[i] = (double) misclassified[i] / counter;
                        try {
                            callback.callback(KNNLeaveOneOutEvaluator.this);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
            
            // release the tuner and the classifier
            classifiers.add(classifier);
            
        }
        
    }

    /**
     * Performs multi-threaded evaluation of the classifier.
     * 
     * @param classifier the classifier to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @param tnumber    the number of threads
     * @return the error rate of the classifier
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs during evaluation
     */
    private void evaluateMultithreaded(KNNClassifier classifier, 
                                       Dataset dataset, 
                                       int tnumber) throws Exception {

        final int dsize = dataset.size();

        // make copies of the classifier
        
        classifiers = new ConcurrentLinkedQueue<>();

        Copier.makeCopies(classifier, classifiers, false, tnumber);
        
        // prepare the executor service and the tasks 

        this.initExecutor(tnumber);

        EvaluatorTask.dataset = dataset;

        List<EvaluatorTask> tasks = new ArrayList<>();
        
        for (int i = 0; i < dsize; i++ )
            if (labels[i] == null)
                tasks.add(new EvaluatorTask(i));
        
        ThreadUtils.startRunnables(executor, tasks);

        // shut down the copies of the classifier if it is multi-threaded
        
        if (classifier instanceof Multithreaded)
            for (Classifier classifierCopy : classifiers)
                ((Multithreaded) classifierCopy).shutdown();                
        
        classifiers = null;
        
    }
    
    /**
     * Returns the predicted label of each time series from the dataset.
     * 
     * <p>
     * A {@code null} value indicates that the given time series has not yet been
     * classified.
     * 
     * @return the predicted labels
     */
    public double[][] getLabels() {
        return labels;
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        return new LeaveOneOutEvaluator();
    }

}

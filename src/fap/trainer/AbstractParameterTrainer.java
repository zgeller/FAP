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

package fap.trainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

import fap.callback.Callback;
import fap.callback.Callbackable;
import fap.core.classifier.Classifier;
import fap.core.data.Dataset;
import fap.core.evaluator.Evaluator;
import fap.core.trainer.AbstractTrainer;
import fap.core.trainer.Trainer;
import fap.exception.EmptyDatasetException;
import fap.util.Copier;
import fap.util.Copyable;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;

/**
 * Defines common methods and fields for callbackable, resumable, multithreaded
 * trainers that tunes a single parameter of the classifier or distance measure
 * relying on a parameter modifier.
 * 
 * @param <T> the type of the parameter that is to be tuned, it should implement
 *            the {@link Comparable} interface
 * 
 * @author Zoltán Gellér
 * @version 2025.03.06.
 * @see AbstractTrainer
 * @see ParameterTrainer
 * @see Callbackable
 * @see Resumable
 * @see Multithreaded
 * @see Modifier
 */
public abstract class AbstractParameterTrainer<T extends Comparable<T>> extends AbstractTrainer
        implements ParameterTrainer<T>, Callbackable, Resumable, Multithreaded, Copyable {

    private static final long serialVersionUID = 1L;
    
    /**
     * The executor service used for implementing multithreaded classification.
     */
    protected transient ThreadPoolExecutor executor;
    
    /**
     * The parameter modifier to set the value of to the parameter tuned by this
     * trainer.
     */
    protected Modifier<T> modifier;

    /**
     * List of values ​​evaluated by this trainer.
     */
    protected List<T> values;

    /**
     * The parameter value that produced the smallest classification error.
     */
    protected T bestValue;

    /**
     * The list of the parameter values that produced the smallest classification
     * error.
     */
    protected List<Comparable<?>> parameters;

    /**
     * The evaluator that is used to evaluate the classifier.
     */
    protected Evaluator evaluator;

    /**
     * The sub-trainer that is used to train the classifier.
     */
    protected ParameterTrainer<?> trainer;
    
    /**
     * Indicates whether the specified parameter value has already been used for training.
     */
    protected boolean trained[];
    
    /**
     * List of copies of this trainer used by the parallel implementation.
     */
    protected ConcurrentLinkedQueue<Trainer> trainers;
    
    /**
     * List of classifier copies used by the parallel implementation.
     */
    protected ConcurrentLinkedQueue<Classifier> classifiers;
    
    /**
     * During the parallel training, there will be a list of best parameters
     * provided by the sub-trainer for each value of the parameter of this trainer.
     */
    protected List<List<Comparable<?>>> trainedParameters;

    protected double stepSize = 0;
    protected double progress = 0;
    protected int steps = 1;
    
    /**
     * The Callback object.
     */
    protected transient Callback callback;

    /**
     * The number of threads. Default value is {@code 1}.
     */
    protected int numberOfThreads = 1;

    /**
     * Indicates whether the tuning has done. Default value is {@code false}.
     */
    protected boolean done = false;

    /**
     * Indicates whether the tuning has started. Default value is {@code false}.
     */
    protected boolean insideLoop = false;

    /**
     * Empty constructor.
     */
    public AbstractParameterTrainer() {
    }

    /**
     * Constructor with single-threaded execution and parameter modifier.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     */
    public AbstractParameterTrainer(Modifier<T> modifier) {
        this.setModifier(modifier);
    }

    /**
     * Constructor with the number of threads.
     * 
     * @param tnumber number of threads.
     */
    public AbstractParameterTrainer(int tnumber) {
        this((Modifier<T>) null, tnumber);
    }

    /**
     * Constructor with the parameter modifier and number of threads.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param tnumber  number of threads.
     */
    public AbstractParameterTrainer(Modifier<T> modifier, int tnumber) {
        this.setNumberOfThreads(tnumber);
        this.setModifier(modifier);
    }

    /**
     * Constructor with single-threaded execution, and the first and last parameter
     * values.
     * 
     * @param first the first value to be evaluated, must be {@code first <= last}
     * @param last  the first value to be evaluated, must be {@code first <= last}
     */
    public AbstractParameterTrainer(T first, T last) {
        this(null, first, last, null, 1);
    }

    /**
     * Constructor with single-threaded execution, the parameter modifier, and the
     * first and last parameter values.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param first    the first value to be evaluated, must be
     *                 {@code first <= last}
     * @param last     the first value to be evaluated, must be
     *                 {@code first <= last}
     */
    public AbstractParameterTrainer(Modifier<T> modifier, T first, T last) {
        this(modifier, first, last, null, 1);
    }

    /**
     * Constructor with the first and last parameter values, and number of threads.
     * 
     * @param first   the first value to be evaluated, must be {@code first <= last}
     * @param last    the first value to be evaluated, must be {@code first <= last}
     * @param tnumber number of threads.
     */
    public AbstractParameterTrainer(T first, T last, int tnumber) {
        this(null, first, last, null, tnumber);
    }

    /**
     * Constructor with the parameter modifier, the first and last parameter values,
     * and number of threads.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param first    the first value to be evaluated, must be
     *                 {@code first <= last}
     * @param last     the first value to be evaluated, must be
     *                 {@code first <= last}
     * @param tnumber  number of threads.
     */
    public AbstractParameterTrainer(Modifier<T> modifier, T first, T last, int tnumber) {
        this(modifier, first, last, null, tnumber);
    }

    /**
     * Constructor with single-threaded execution, the first and last parameter
     * values, and the increment.
     * 
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     */
    public AbstractParameterTrainer(T first, T last, T increment) {
        this(null, first, last, increment, 1);
    }

    /**
     * Constructor with single-threaded execution, the parameter modifier, the first
     * and last parameter values, and the increment.
     * 
     * @param modifier  the parameter modifier, which is to be used to set the value
     *                  of the parameter tuned by the trainer
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     */
    public AbstractParameterTrainer(Modifier<T> modifier, T first, T last, T increment) {
        this(modifier, first, last, increment, 1);
    }

    /**
     * Constructor with the first and last parameter values, the increment, and the
     * number of threads.
     * 
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     * @param tnumber   number of threads.
     */
    public AbstractParameterTrainer(T first, T last, T increment, int tnumber) {
        this(null, first, last, increment, tnumber);
    }

    /**
     * Constructor with the parameter modifier, the first and last parameter values,
     * the increment, and number of threads.
     * 
     * @param modifier  the parameter modifier, which is to be used to set the value
     *                  of the parameter tuned by the trainer
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     * @param tnumber   number of threads.
     */
    public AbstractParameterTrainer(Modifier<T> modifier, T first, T last, T increment, int tnumber) {
        this.setValues(first, last, increment);
        this.setNumberOfThreads(tnumber);
        this.setModifier(modifier);
    }

    /**
     * Constructor with single-threaded execution, and the list of values to be
     * evaluated.
     * 
     * @param values the list of values to be evaluated
     */
    public AbstractParameterTrainer(List<T> values) {
        this(null, values, 1);
    }

    /**
     * Constructor with single-threaded execution, the parameter modifier and the
     * list of values to be evaluated.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param values   the list of values to be evaluated
     */
    public AbstractParameterTrainer(Modifier<T> modifier, List<T> values) {
        this(modifier, values, 1);
    }

    /**
     * Constructor with the list of values to be evaluated, and the number of
     * threads.
     * 
     * @param values  the list of values to be evaluated
     * @param tnumber number of threads.
     */
    public AbstractParameterTrainer(List<T> values, int tnumber) {
        this(null, values, tnumber);
    }

    /**
     * Constructor with the parameter modifier, the list of values to be evaluated,
     * and number of threads.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param values   the list of values to be evaluated
     * @param tnumber  number of threads.
     */
    public AbstractParameterTrainer(Modifier<T> modifier, List<T> values, int tnumber) {
        this.setValues(values);
        this.setNumberOfThreads(tnumber);
        this.setModifier(modifier);
    }

    /**
     * Constructor with single-threaded execution, and the array of values to be
     * evaluated.
     * 
     * @param values the array of values to be evaluated
     */
    public AbstractParameterTrainer(T[] values) {
        this(null, values, 1);
    }

    /**
     * Constructor with single-threaded execution, the parameter modifier, and the
     * array of values to be evaluated.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param values   the array of values to be evaluated
     */
    public AbstractParameterTrainer(Modifier<T> modifier, T[] values) {
        this(modifier, values, 1);
    }

    /**
     * Constructor with the array of values to be evaluated, and the number of
     * threads.
     * 
     * @param values  the array of values to be evaluated
     * @param tnumber number of threads
     */
    public AbstractParameterTrainer(T[] values, int tnumber) {
        this(null, values, tnumber);
    }

    /**
     * Constructor with the parameter modifier, the array of values to be evaluated,
     * and number of threads.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param values   the array of values to be evaluated
     * @param tnumber  number of threads
     */
    public AbstractParameterTrainer(Modifier<T> modifier, T[] values, int tnumber) {
        this.setValues(values);
        this.setNumberOfThreads(tnumber);
        this.setModifier(modifier);
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
    public void setValues(List<T> values) {
        this.values = values;
    }

    @Override
    public void setValues(T[] values) {
        this.values = Arrays.asList(values);
    }

    /**
     * @param first the first value to be evaluated, must be {@code first <= last}
     * @param last  the first value to be evaluated, must be {@code first <= last}
     * @throws IllegalArgumentException if {@code first > last}
     */
    @Override
    public void setValues(T first, T last) throws IllegalArgumentException {
        if (first.compareTo(last) > 0)
            throw new IllegalArgumentException("Must be first <= last.");
    }

    /**
     * @param first the first value to be evaluated, must be {@code first <= last}
     * @param last  the first value to be evaluated, must be {@code first <= last}
     * @throws IllegalArgumentException if {@code first > last}
     */
    @Override
    public void setValues(T first, T last, T increment) throws IllegalArgumentException {
        if (first.compareTo(last) > 0)
            throw new IllegalArgumentException("Must be first <= last.");
    }

    @Override
    public List<T> getValues() {
        return this.values;
    }

    @Override
    public T getBestValue() {
        return this.bestValue;
    }

    @Override
    public List<Comparable<?>> getParameters() {
        return parameters;
    }

    @Override
    public void setEvaluator(Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Evaluator getEvaluator() {
        return evaluator;
    }

    @Override
    public void setTrainer(ParameterTrainer<?> subTrainer) {
        this.trainer = subTrainer;
    }

    @Override
    public ParameterTrainer<?> getTrainer() {
        return this.trainer;
    }

    // initialization
    protected void initLoop() {
        expectedError = Double.POSITIVE_INFINITY;
        progress = -1;
        steps = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
        bestValue = null;
        int len = values.size();
        trained = new boolean[len];
        parameters = new LinkedList<>();
        trainedParameters = new ArrayList<>();
        for (int i = 0; i < len; i++)
            trainedParameters.add(null);
    }

    /**
     * Trains the classifier using the specified trainset.
     * 
     * @param classifier the classifier to be trained
     * @param trainset   the training dataset
     * @param modifier   the parameter modifier, which is to be used to set the
     *                   value of the parameter tuned by the trainer
     * @return the expected error rate
     * @throws Exception             if an error occurs
     * @throws EmptyDatasetException if the trainset is empty
     */
    protected double train(Classifier classifier, Dataset trainset, Modifier<T> modifier) throws Exception {

        if (done || ((trainer == null) && (evaluator == null)))
            return expectedError;

        if (trainset.isEmpty())
            throw new EmptyDatasetException("The train set cannot be empty.");

        final int len = values.size();

        // callback initialization
        if (callback != null) {
            stepSize = (double) callback.getDesiredCallbackNumber() / len;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(len + 1);
                stepSize = 1;
            }
        }

        // initialization
        if (!insideLoop)
            initLoop();
        else if (callback != null)
            callback.setCallbackCount(steps);
        
        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

        boolean parallelTraining = (tnumber > 1) && 
                                   ((trainer == null) || (trainer instanceof Copyable)) && 
                                   (classifier instanceof Copyable);
        
        if (parallelTraining)
            trainParallel(classifier, trainset, modifier, tnumber);
        else
            trainSequential(classifier, trainset, modifier);

        // setting the best values of the parameters
        if (bestValue != null)
            this.setParameters(classifier, parameters);

        insideLoop = false;
        done = true;
        if (callback != null) {
            callback.callback(this);
            callback.setCallbackCount(-1);
        }
        
        return expectedError;
        
    }
    
    /**
     * Sequentially trains the classifier using the specified trainset.
     * 
     * @param classifier the classifier to be trained
     * @param trainset   the training dataset
     * @param modifier   the parameter modifier, which is to be used to set the
     *                   value of the parameter tuned by the trainer
     * @throws Exception if an error occurs
     */
    protected void trainSequential(Classifier classifier, Dataset trainset, Modifier<T> modifier) throws Exception {

        // finding the best value
        for (int index = 0; index < trained.length; index++) {
            
            if (Thread.interrupted())
                throw new InterruptedException();
            
            // if the value has already been used
            if (trained[index])
                continue;

            final T value = values.get(index);

            // setting the value of the parameter
            modifier.set(classifier, value);

            double error = Double.POSITIVE_INFINITY;

            if (trainer != null) {

                error = trainer.train(classifier, trainset);

                if (trainer instanceof Resumable rst)
                    rst.reset();

            } else {

                error = evaluator.evaluate(null, classifier, trainset);
                
                if (evaluator instanceof Resumable re)
                    re.reset();

            }

            if (error < expectedError) {

                expectedError = error;
                bestValue = value;

                parameters = new LinkedList<>();

                parameters.add(bestValue);

                if (trainer != null)
                    parameters.addAll(trainer.getParameters());

            }
            
            trained[index] = true;

            // reseting the classifier
            if (classifier instanceof Resumable rc)
                rc.reset();

            // calling back
            if (callback != null) {
                progress += stepSize;
                if (progress >= steps) {
                    steps++;
                    callback.callback(this);
                }
            }

        }
        
    }

    /**
     * Training task.
     */
    protected class TrainTask implements Callable<Double> {
        
        /**
         * The train set.
         */
        static Dataset trainset;

        /**
         * The value of the parameter.
         */
        private T value;
        
        /**
         * The index of the value.
         */
        int index;
        
        /**
         * Constructs a new training task for the specified {@code value}.
         * 
         * @param value the value of the parameter to be evaluated
         * @param index the index of the value
         */
        public TrainTask(T value, int index) {
            this.value = value;
            this.index = index;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public Double call() throws Exception {
            
            // getting a trainer and a classifier
            AbstractParameterTrainer<T> trainerCopy = (AbstractParameterTrainer<T>) trainers.poll();
            Classifier classifierCopy = classifiers.poll();
            
            ParameterTrainer<?> subTrainer = trainerCopy.getTrainer();
            Evaluator evaluator = trainerCopy.getEvaluator();
            Modifier<T> modifier = trainerCopy.getModifier();

            // setting the value of the parameter
            modifier.set(classifierCopy, value);

            double error = Double.POSITIVE_INFINITY;

            if (subTrainer != null) {

                error = subTrainer.train(classifierCopy, TrainTask.trainset);

                if (subTrainer instanceof Resumable rst)
                    rst.reset();

            } else {

                error = evaluator.evaluate(null, classifierCopy, TrainTask.trainset);

                if (evaluator instanceof Resumable re)
                    re.reset();

            }
            
            // memorizing parameter values
            List<Comparable<?>> params = new LinkedList<>();
            params.add(value);
            if (subTrainer != null)
                params.addAll(subTrainer.getParameters());
            
            trainedParameters.set(index, params);

            trained[index] = true;

            if (classifierCopy instanceof Resumable rc)
                rc.reset();
            
            trainers.add(trainerCopy);
            classifiers.add(classifierCopy);
            
            if (callback != null)
                synchronized(callback) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        callback.callback(AbstractParameterTrainer.this);
                    }
                }
            
            return error;
            
        }
        
    }

    /**
     * Parallely trains the classifier using the specified trainset.
     * 
     * @param classifier the classifier to be trained
     * @param trainset   the training dataset
     * @param modifier   the parameter modifier, which is to be used to set the
     *                   value of the parameter tuned by the trainer
     * @param tnumber    number of threads
     * @throws Exception if an error occurs
     */
    protected void trainParallel(Classifier classifier, Dataset trainset, Modifier<T> modifier, int tnumber) throws Exception {
        
        // making copies of this trainer
        trainers = new ConcurrentLinkedQueue<>();
        Copier.makeCopies(this, trainers, tnumber);
        
        // making copies of the classifier
        classifiers = new ConcurrentLinkedQueue<>();
        Copier.makeCopies(classifier, classifiers, this.affectsDistance(), tnumber);
        
        this.initExecutor(tnumber);
        
        TrainTask.trainset = trainset;
        
        // initializing tasks
        List<TrainTask> tasks = new ArrayList<>();
        
        for (int index = 0; index < trained.length; index++)
            if (!trained[index])
                tasks.add(new TrainTask(values.get(index), index));
        
        if (Thread.interrupted())
            throw new InterruptedException();
        
        List<Double> expectedErrors = ThreadUtils.startCallables(executor, tasks);
        
        // finding the lowest expected error
        double error = Double.POSITIVE_INFINITY;
        
        int bestIndex = -1;
        for (int i = 0; i < expectedErrors.size(); i++)
            if (expectedErrors.get(i) < error) {
                bestIndex = i;
                error = expectedErrors.get(i);
            }
        
        expectedError = expectedErrors.get(bestIndex);
        
        bestValue = tasks.get(bestIndex).value;
        
        parameters = new LinkedList<>(trainedParameters.get(bestIndex));
        
        trainers = null;
        classifiers = null;
        trainedParameters = null;
        trained = null;
        
    }
    

    @Override
    public void setNumberOfThreads(int tnumber) {
        this.numberOfThreads = tnumber;
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

        if (evaluator instanceof Multithreaded me)
            me.shutdown();

        if (trainer instanceof Multithreaded mst)
            mst.shutdown();

    }

    /**
     * Sets the parameter modifier, which is to be used to set the value of the
     * parameter tuned by the trainer.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     */
    public void setModifier(Modifier<T> modifier) {
        this.modifier = modifier;
    }

    /**
     * Returns the parameter modifier used to set the value of the parameter tuned
     * by this trainer.
     * 
     * @return the parameter modifier used to set the value of the parameter tuned
     *         by this trainer
     */
    public Modifier<T> getModifier() {
        return modifier;
    }

    /**
     * Delegates the first value to the parameter modifier and a new list containing
     * the rest of the values to the sub-trainer.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setParameters(Classifier classifier, List<Comparable<?>> bvalues) {
        this.parameters = bvalues;
        this.bestValue = (T) bvalues.get(0);
        modifier.set(classifier, this.bestValue);
        List<Comparable<?>> sublist = new LinkedList<>(parameters);
        sublist.removeFirst();
        if (trainer != null)
            trainer.setParameters(classifier, sublist);
    }

    @Override
    public double train(Classifier classifier, Dataset trainset) throws Exception {
        return train(classifier, trainset, modifier);
    }

    /**
     * Initializes the specified trainer with the common data structures of this
     * trainer.
     * 
     * @param copy the classifier whose data structures is to be initialized
     * @param deep indicates whether a deep copy should be made
     * @throws ClassCastException if the sub-trainer or the evaluator do not
     *                            implement the {@link Copyable} interface (when
     *                            making a deep copy)
     */
    protected void init(AbstractParameterTrainer<T> copy, boolean deep) {
        
        super.init(copy, deep);
        
        ParameterTrainer<?> subTrainerCopy = trainer;
        Evaluator evaluatorCopy = evaluator;

        if (deep) {
            if (trainer != null)
                subTrainerCopy = (ParameterTrainer<?>) ((Copyable) trainer).makeACopy();
            if (evaluator != null)
                evaluatorCopy = (Evaluator) ((Copyable) evaluator).makeACopy();
        }

        copy.setTrainer(subTrainerCopy);
        copy.setEvaluator(evaluatorCopy);
        copy.setModifier(this.getModifier()); // consider making a deep copy
        copy.setValues(this.getValues()); // considr making a deep copy
        
    }
    
    @Override
    public boolean affectsDistance() {
        return super.affectsDistance() || 
               (modifier != null && modifier.affectsDistance()) ||
               (trainer != null && trainer.affectsDistance());
    }
    
    @Override
    public String toString() {
        return super.toString() + ", values=" + getValues();
    }
    
}

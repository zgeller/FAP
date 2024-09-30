package fap.trainer;

import java.util.List;

import fap.core.classifier.Classifier;
import fap.core.evaluator.Evaluator;
import fap.core.trainer.Trainer;

/**
 * Declares common methods for classifier trainers that tunes a single parameter
 * of the classifier.
 * 
 * @param <T> the type of the parameter that is to be tuned, it should implement
 *            the {@link Comparable} interface
 *            
 * @author Zoltan Geller
 * @version 2024.09.23.
 * @see Trainer
 * @see Comparable
 */
public interface ParameterTrainer<T extends Comparable<T>> extends Trainer {

    /**
     * Sets the list of parameter values ​​to be evaluated by this trainer.
     * 
     * @param values the list of parameter values ​​to be evaluated by this trainer
     */
    public void setValues(List<T> values);

    /**
     * Sets the list of parameter values ​​to be evaluated by this trainer.
     * 
     * @param values the array of parameter values ​​to be evaluated by this
     *               trainer.
     */
    public void setValues(T[] values);

    /**
     * Initializes the list of parameter values ​​to be evaluated by this trainer
     * with the values from {@code first} to {@code last}, in unit increment.
     * 
     * @param first the first value to be evaluated
     * @param last  the first value to be evaluated
     */
    public void setValues(T first, T last);

    /**
     * Initializes the list of parameter values ​​to be evaluated by this trainer
     * with the values from {@code first} to {@code last}, in the specified
     * {@code increment}.
     * 
     * @param first     the first value to be evaluated
     * @param last      the first value to be evaluated
     * @param increment the increment
     */
    public void setValues(T first, T last, T increment);

    /**
     * Returns the list parameter values evaluated by this trainer.
     * 
     * @return the list parameter values evaluated by this trainer
     */
    public List<T> getValues();
    
    /**
     * Returns the parameter value that produced the smallest classification error.
     * 
     * @return the parameter value that produced the smallest classification error
     */
    public T getBestValue();

    /**
     * Returns the list of the values of the parameters that produced the smallest
     * classification error.
     * 
     * @return the list of the values of the parameters that produced the smallest
     *         classification error
     */
    public List<Comparable<?>> getParameters();
    
    /**
     * It should initialize the parameter with the first element of the list and
     * forward the rest of the list to the sub-trainer.
     * 
     * @param classifier the classifier whose parameters are to be set
     * @param parameters the values of the parameters to be set
     */
    public void setParameters(Classifier classifier, List<Comparable<?>> parameters);
    
    /**
     * Sets the evaluator that is be used to evaluate the classifier.
     * 
     * @param evaluator the evaluator that is be used to evaluate the classifier
     */
    public void setEvaluator(Evaluator evaluator);

    /**
     * Returns the evaluator used to evaluate the classifier.
     * 
     * @return the evaluator the evaluator used to evaluate the classifier
     */
    public Evaluator getEvaluator();

    /**
     * Sets the sub-trainer that is to be used to train the classifier.
     * 
     * @param trainer the sub-trainer that is to be used to train the classifier
     */
    public void setTrainer(ParameterTrainer<?> trainer);

    /**
     * Returns the sub-trainer that is used to train the classifier.
     * 
     * @return the sub-trainer that is used to train the classifier
     */
    public ParameterTrainer<?> getTrainer();

}

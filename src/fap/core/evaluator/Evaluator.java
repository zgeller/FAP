package fap.core.evaluator;

import java.io.Serializable;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;
import fap.core.trainer.Trainer;

/**
 * Declares basic methods for evaluating the performance of classifiers.
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see Serializable
 */
public interface Evaluator extends Serializable {

    /**
     * Returns the error rate of the tested classifier.
     * 
     * @return the error rate of the tested classifier
     */
    public double getError();

    /**
     * Returns the number of misclassified time series.
     * 
     * @return the number of misclassified time series
     */
    public int getMisclassified();

    /**
     * Evaluates the error rate of the specified classifier using the given dataset.
     * 
     * @param trainer    the trainer that is to be used to train the classifier
     * @param classifier the classifier that is to be evaluated
     * @param dataset    the dataset to be used for evaluating the classifier
     * @return the error rate of the classifier
     * @throws Exception if an error occurs
     */
    public double evaluate(Trainer trainer, Classifier classifier, Dataset dataset) throws Exception;

}

package fap.core.trainer;

import java.io.Serializable;

import fap.core.classifier.Classifier;
import fap.core.data.Dataset;

/**
 * Declares common methods for classifier trainers.
 * 
 * @author Zoltan Geller
 * @version 2024.09.22.
 * @see Serializable
 */
public interface Trainer extends Serializable  {

    /**
     * Tunes/trains the specified classifier using the given training dataset. It
     * should return the expected error rate.
     * 
     * @param classifier the classifier that is to be trained
     * @param trainset   the training dataset
     * @return the expected error rate
     * @throws Exception if an error occurs
     */
    public double train(Classifier classifier, Dataset trainset) throws Exception;

    /**
     * Returns the expected error rate.
     * 
     * @return the expected error rate
     */
    public double getExpectedError();
    
    /**
     * Should return {@code true} if this trainer or its subtrainer affects the
     * distance measure.
     * 
     * @return {@code true} if this trainer or its subtrainer affects the distance
     *         measure
     */
    public default boolean affectsDistance() {
        return false;
    }
    
}

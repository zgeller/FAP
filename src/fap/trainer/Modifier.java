package fap.trainer;

import fap.core.classifier.Classifier;

/**
 * Declares methods for parameter setter classes.
 * 
 * @param <T> the type of the parameter to be set by this parameter setter
 * 
 * @author Zoltan Geller
 * @version 2024.09.22.
 */
public interface Modifier<T> {
    
    /**
     * Sets the specified {@code value} of the parameter of the {@code classifier}.
     * 
     * @param classifier the classifier whose parameter is to be set
     * @param value      the value to set the parameter to
     */
    public void set(Classifier classifier, T value);
    
    /**
     * Should return {@code true} if this parameter setter affects the distance
     * measure.
     * 
     * @return {@code true} if this parameter setter affects the distance measure
     */
    public boolean affectsDistance();

}

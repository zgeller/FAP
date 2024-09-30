package fap.core.classifier;

import fap.core.distance.Distance;

/**
 * Declares common methods for distance-based classifiers.
 * 
 * @author Zoltan Geller
 * @version 2024.09.08.
 * @see Classifier
 */
public interface DistanceBasedClassifier extends Classifier {

    /**
     * Sets the distance measure. It can be ignored by non distance-based classifiers.
     * 
     * @param distance the distance measure to set
     */
    public void setDistance(Distance distance);

    /**
     * Returns the distance measure. It can be ignored by non distance-based classifiers.
     * 
     * @return the distance measure
     */
    public Distance getDistance();
    
}

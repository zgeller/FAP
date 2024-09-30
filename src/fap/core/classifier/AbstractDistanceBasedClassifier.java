package fap.core.classifier;

import fap.core.distance.Distance;
import fap.util.Copyable;

/**
 * Defines common methods and fields for distance-based classifiers.
 * 
 * @author Zoltan Geller
 * @version 2024.09.22.
 * @see Classifier
 */
public abstract class AbstractDistanceBasedClassifier implements DistanceBasedClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * The distance measure.
     */
    protected Distance distance;

    /**
     * Empty constructor.
     */
    public AbstractDistanceBasedClassifier() {
    }
    
    /**
     * Constructor with a distnace measure {@code distance}.
     * 
     * @param distance distance measure
     */
    public AbstractDistanceBasedClassifier(Distance distance) {
        this.setDistance(distance);
    }
    
    /**
     * Initializes the specified classifier with the common data structures of this
     * classifier.
     * 
     * @param copy the classifier whose data structures is to be initialized
     * @param deep indicates whether a deep copy should be made
     * @throws ClassCastException if the distance measure does not implement the
     *                            {@link Copyable} interface (when making a deep
     *                            copy)
     */
    protected void init(AbstractDistanceBasedClassifier copy, boolean deep) throws ClassCastException {

        Distance distCopy = distance;

        if (deep && distance != null)
            distCopy = (Distance) ((Copyable) distance).makeACopy(deep);
        
        copy.setDistance(distCopy);
        
    }
    
    
    @Override
    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    @Override
    public Distance getDistance() {
        return this.distance;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName();
    }
    
}

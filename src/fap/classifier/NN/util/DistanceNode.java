package fap.classifier.NN.util;

/**
 * Each node contains an {@link #obj object} of type {@code T} and an associated {@code double} value
 * (i.e. the {@link #distance} of the object).
 * 
 * @author Zoltan Geller
 * @version 2024.08.20.
 */
public class DistanceNode<T> {

    /**
     * The object.
     */
    public T obj;
    
    /**
     * The distance.
     */
    public double distance;

    /**
     * Constructs a new {@Code DisanceNode} object.
     * 
     * @param obj      the object
     * @param distance the distance
     */
    public DistanceNode(T obj, double distance) {
        this.obj = obj;
        this.distance = distance;
    }

}

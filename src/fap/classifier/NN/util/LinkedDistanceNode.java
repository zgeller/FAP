package fap.classifier.NN.util;

/**
 * Each node contains an {@link #obj object} of type {@code T} and an associated
 * {@code double} value (i.e. the {@link #distance} of the object), as well as
 * references to the {@link #prev previous} and {@link #next} nodes of the list
 * to which the given node belongs.
 * 
 * @author Zoltan Geller
 * @version 2024.08.20.
 */
public class LinkedDistanceNode<T> {

    /**
     * The object.
     */
    public T obj;

    /**
     * The distance.
     */
    public double distance;

    /**
     * Reference to the previous node of the list to which this node belongs.
     */
    public LinkedDistanceNode<T> prev;

    /**
     * Reference to the next node of the list to which this node belongs.
     */
    public LinkedDistanceNode<T> next;

    /**
     * Constructs a new {@code LinkedDistanceNode} object.
     * 
     * @param obj      the object
     * @param distance the distance
     * @param prev     reference to the previous node
     * @param next     reference to the next node
     */
    public LinkedDistanceNode(T obj, double distance, LinkedDistanceNode<T> prev, LinkedDistanceNode<T> next) {
        this.obj = obj;
        this.distance = distance;
        this.prev = prev;
        this.next = next;
    }

}

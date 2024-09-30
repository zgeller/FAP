package fap.dm;

import java.io.Serializable;

import fap.callback.Callback;
import fap.callback.Callbackable;
import fap.core.data.Dataset;
import fap.core.distance.Distance;
import fap.util.Multithreaded;
import fap.util.Resumable;

/**
 * Defines common methods and fields for distance matrix generator objects.
 * 
 * @author Zoltan Geller
 * @version 2024.09.10.
 * @param <T>
 * @see Serializable
 * @see Resumable
 * @see Callbackable
 * @see Multithreaded
 */
public abstract class AbstractDistanceGenerator<T> implements Serializable, Resumable, Callbackable, Multithreaded {

    private static final long serialVersionUID = 1L;

    /**
     * The dataset.
     */
    protected transient Dataset dataset;

    /**
     * The distance measure.
     */
    protected Distance distance;

    /**
     * The Callback object.
     */
    protected transient Callback callback;

    /**
     * Indicates whether to generate triangular distance matrices. Default value is
     * {@code true}.
     */
    protected boolean symmetrical = true;

    /**
     * First row of the distance matrix. Default value is {@code 0}.
     */
    protected int first = 0;

    /**
     * Last row of the distance matrix. Default value is {@code -1}, which denotes
     * the last row.
     */
    protected int last = -1;

    /**
     * Indicates whether generating the distance matrix is complete. Default value
     * is {@code false}.
     */
    protected boolean done = false;

    /**
     * Indicates whether generating the distance matrix has started. Default value
     * is {@code false}.
     */
    protected boolean insideLoop = false;

    /**
     * The number of threads. Default value is {@code 1}.
     */
    protected int numberOfThreads = 1;

    /**
     * Sets the dataset.
     * 
     * @param dataset the dataset to set
     */
    public void setDataSet(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Returns the dataset.
     * 
     * @return the dataset
     */
    public Dataset getDataSet() {
        return this.dataset;
    }

    /**
     * Sets the distance measure.
     * 
     * @param distance the distance measure to set
     */
    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    /**
     * Returns the distance measure.
     * 
     * @return the distance measure
     */
    public Distance getDistance() {
        return this.distance;
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public Callback getCallback() {
        return this.callback;
    }

    /**
     * Selects whether to generate triangular matrices.
     * 
     * @param symmetrical {@code boolean} value which indicates whether to generate
     *                    triangular distance matrices
     */
    public void setSymmetrical(boolean symmetrical) {
        this.symmetrical = symmetrical;
    }

    /**
     * Returns {@code true} if generating triangluar matrices has selected.
     * 
     * @return {@code true} if generating triangluar matrices has selected
     */
    public boolean isSymmetrical() {
        return this.symmetrical;
    }

    /**
     * Sets the first row.
     * 
     * @param first the first row
     */
    public void setFirst(int first) {
        this.first = first;
    }

    /**
     * Returns the first row.
     * 
     * @return the first row
     */
    public int getFirst() {
        return this.first;
    }

    /**
     * Sets the last row.
     * 
     * @param last the last row
     */
    public void setLast(int last) {
        this.last = last;
    }

    /**
     * Returns the last row
     * 
     * @return the last row
     */
    public int getLast() {
        return this.last;
    }

    /**
     * Computes the distances.
     * 
     * @throws Exception if an error occurs
     */
    public abstract void compute() throws Exception;

    /**
     * Returns the distance object.
     * 
     * @return distance object
     */
    public abstract T getDistanceObject();

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public void reset() {
        this.done = false;
        this.insideLoop = false;
    }

    @Override
    public boolean isInProgress() {
        return this.insideLoop;
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
    public void shutdown() {
    }
    
}

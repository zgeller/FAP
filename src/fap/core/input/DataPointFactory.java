package fap.core.input;

import fap.core.data.DataPoint;

/**
 * Generic interface for classes which produce data points.
 * 
 * @author Aleksa Todorovic
 * @version 1.0
 */
public interface DataPointFactory {

    /**
     * Evaluator if there is another data point which can be read.
     * 
     * @return {@code true} if there is data point available, {@code false}
     *         otherwise
     */
    public boolean hasNextPoint() throws IllegalArgumentException;

    /**
     * Returns next available data point.
     * 
     * @return next available data point.
     */
    public DataPoint nextPoint();

}

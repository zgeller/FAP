package fap.core.distance;

import java.io.Serializable;

import fap.core.data.TimeSeries;

/**
 * Declares common methods for distance measures.
 * 
 * @author Aleksa Todorovic, Zoltan Geller
 * @version 2024.08.17.
 * @see Serializable
 */
public interface Distance extends Serializable {

    /**
     * Computes the distance between {@code series1} and {@code series2}.
     * 
     * @param series1 the first time series
     * @param series2 the second time series
     * @return the distance between {@code series1} and {@code series2}
     */
    public double distance(TimeSeries series1, TimeSeries series2);

}

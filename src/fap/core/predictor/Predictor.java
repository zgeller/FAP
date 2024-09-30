package fap.core.predictor;

import java.io.Serializable;

import fap.core.data.DataPoint;
import fap.core.data.Representation;

/**
 * Predictor is object used to predict some point or value of unknown series.
 * 
 * @author Aleksa Todorovic, Zoltan Geller
 * @version 2024.09.17.
 */
public interface Predictor extends Serializable {

    /**
     * Predicts value of unknown series in some point {@code x}.
     * 
     * @param x     x-coordinate for which value of series is being looked for
     * @param param used to send predictor-specific parameters to function
     * @return value of unknown series for {@code x}
     */
    public double predictValue(double x, Object param);

    /**
     * Predicts value of some special point inside unknown series. Semantics of this
     * point depends on concrete type of predictor.
     * 
     * @param param used to send predictor-specific parameters to function
     * @return unknown point, null if predictor cannot predict point
     */
    public DataPoint predictPoint(Object param);

    /**
     * Generates some representation of unknown series.
     * 
     * @param param used to send predictor-specific parameters to function
     * @return some representation of unknown series
     */
    public Representation predictRepr(Object param);

}

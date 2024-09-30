package fap.core.predictor;

import fap.core.data.Dataset;
import fap.core.data.TimeSeries;

/**
 * Generic parent for all predictors which predict output based on input series
 * list and one testing series.
 * 
 * @author Aleksa Todorovic, Zoltan Geller
 * @version 2024.09.17.
 * @see Predictor
 */
public abstract class OutputPredictor<T extends TimeSeries> implements Predictor {

    private static final long serialVersionUID = 1L;

    /**
     * Testing input series.
     */
    protected T testInputSeries;

    /**
     * List of input series.
     */
    protected Dataset inputSeries;

    /**
     * Constructor.
     * 
     * @param testInputSeries testing input series
     * @param dataset    list of input series
     */
    protected OutputPredictor(T testInputSeries, Dataset inputSeries) {
        this.testInputSeries = testInputSeries;
        this.inputSeries = inputSeries;
    }

}

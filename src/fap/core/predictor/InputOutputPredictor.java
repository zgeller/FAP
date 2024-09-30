package fap.core.predictor;

import java.util.Map;

import fap.core.data.Dataset;
import fap.core.data.TimeSeries;

/**
 * Generic parent for all predictors which predict output based on input series
 * list, corresponding outputs and one testing series.
 * 
 * @author Aleksa Todorovic, Zoltan Geller
 * @version 2024.09.17.
 * @see Predictor
 */
public abstract class InputOutputPredictor implements Predictor {

    private static final long serialVersionUID = 1L;

    /**
     * Testing input series.
     */
    protected TimeSeries testInputSeries;

    /**
     * Dataset.
     */
    protected Dataset dataset;

    /**
     * Map of output series for input series. Keys are input series, values are
     * output series.
     */
    protected Map<TimeSeries, TimeSeries> inputOutputMapping;

    /**
     * Constructor
     * 
     * @param testInputSeries    testing input series
     * @param dataset            input dataset
     * @param inputOutputMapping map of output series for input series
     */
    protected InputOutputPredictor(TimeSeries testInputSeries, 
                                   Dataset dataset, 
                                   Map<TimeSeries, TimeSeries> inputOutputMapping) {
        this.testInputSeries = testInputSeries;
        this.dataset = dataset;
        this.inputOutputMapping = inputOutputMapping;
    }

}

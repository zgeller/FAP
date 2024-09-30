package fap.predictor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fap.core.data.DataPoint;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.data.Representation;
import fap.core.distance.Distance;
import fap.core.predictor.InputOutputPredictor;
import fap.distance.SplineDistance;
import fap.exception.IncomparableTimeSeriesException;
import fap.representation.SplineRepresentation;

/**
 * Prediction of saturation point based on collection of input and corresponding
 * output series and one testing input series.
 * 
 * This predictor will predict saturation point of output series which would
 * correspond to testing input series.
 * 
 * @author Aleksa Todorovic, Vladimir Kurbalija, Zoltan Geller
 * @version 2024.09.09.
 * @see InputOutputPredictor
 */

// 2023.02.20. - sun.reflect.generics.reflectiveObjects.NotImplementedException replaced with java.lang.UnsupportedOperationException

public class SplineInputOutputPredictor extends InputOutputPredictor {

    private static final long serialVersionUID = 1L;
    
    /**
     * Computed similarities of output series.
     */
    private Map<TimeSeries, Double> outputDistance = new HashMap<TimeSeries, Double>();

    /**
     * Constructor
     * 
     * @param testInputSeries    testing input series
     * @param dataset            input dataset
     * @param inputOutputMapping map which contain corresponding output series for
     *                           input series
     */
    public SplineInputOutputPredictor(TimeSeries testInputSeries, Dataset dataset,
            Map<TimeSeries, TimeSeries> inputOutputMapping) {
        super(testInputSeries, dataset, inputOutputMapping);
        train();
    }

    /**
     * Trains this predictors.
     */
    private void train() {
        
        Distance sim = new SplineDistance();

        for (int index = 0; index < dataset.size(); ++index) {
            TimeSeries inputSeries = dataset.get(index);

            TimeSeries outputSerie = inputOutputMapping.get(inputSeries);
            if (outputSerie == null) {
                continue;
            }

            SplineRepresentation inputRepr = inputSeries.getRepr(SplineRepresentation.class);
            if (inputRepr == null) {
                continue;
            }

            SplineRepresentation outputRepr = outputSerie.getRepr(SplineRepresentation.class);
            if (outputRepr == null) {
                continue;
            }

            SplineRepresentation testInputRepr = testInputSeries.getRepr(SplineRepresentation.class);
            if (testInputRepr == null) {
                continue;
            }

            inputRepr.setScale(1.0);
            double scale = testInputRepr.getMax() / inputRepr.getMax();

            inputRepr.setScale(scale);
            outputRepr.setScale(scale);

            try {
                double distance = sim.distance(inputSeries, testInputSeries);
                outputDistance.put(outputSerie, distance);
            } catch (IncomparableTimeSeriesException e) {
            }
            
        }
    }

    /**
     * Computed similarity for output series.
     * 
     * @param series output series for which is computed similarity being looked for
     * @return computed similarity of series
     * @throws IncomparableTimeSeriesException 
     */
    // TODO getDistance?
    public double getSimilarity(TimeSeries series) throws IncomparableTimeSeriesException {

        if (outputDistance.containsKey(series))
            return outputDistance.get(series);
        else
            throw new IncomparableTimeSeriesException(); // TODO: add some meaningful exception message
    }

    /**
     * @inherit
     */
    public double predictValue(double x, Object param) {
        throw new UnsupportedOperationException();
    }

    /**
     * @inherit
     */
    public DataPoint predictPoint(Object param) {
        
        int maxCount = 10;
        if (param instanceof Integer ip) {
            maxCount = ip;
        }

        double[] similarities = new double[maxCount];
        DataPoint[] simpoints = new DataPoint[maxCount];

        double min = 0.0;
        int count = 0;

        for (int i = 0; i < maxCount; i++) {
            double best = Double.MAX_VALUE;
            TimeSeries bestSeries = null;

            Iterator<TimeSeries> iterator = outputDistance.keySet().iterator();
            while (iterator.hasNext()) {
                TimeSeries tempSerie = iterator.next();
                double tempSimilarity = outputDistance.get(tempSerie);

                if ((tempSimilarity > min) && (tempSimilarity < best)) {
                    best = tempSimilarity;
                    bestSeries = tempSerie;
                }
            }

            if (bestSeries == null) {
                break;
            }

            min = best;

            SplineRepresentation repr = bestSeries.getRepr(SplineRepresentation.class);
            if (repr == null) {
                continue;
            }

            int j = 0;
            while ((j < bestSeries.length() - 1) && (bestSeries.getX(j) < repr.getSaturation())) {
                j++;
            }

            similarities[i] = 1.0 / best;
            simpoints[i] = bestSeries.get(j);

            ++count;
            
        }

        if (count == 0) {
            return null;
        }

        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum = sum + similarities[i];
        }

        double solutionX = 0.0;
        double solutionY = 0.0;

        for (int i = 0; i < count; i++) {
            solutionX += similarities[i] * simpoints[i].getX();
            solutionY += similarities[i] * simpoints[i].getY();
        }

        return new DataPoint(solutionX / sum, solutionY / sum);
    }

    /**
     * @inherit
     */
    public Representation predictRepr(Object param) {
        throw new UnsupportedOperationException();
    }

}

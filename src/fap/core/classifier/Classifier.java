package fap.core.classifier;

import java.io.Serializable;

import fap.core.data.Dataset;
import fap.core.data.TimeSeries;

/**
 * Declares common methods for classifiers.
 * 
 * @author Zoltan Geller
 * @version 2024.08.26.
 * @see Serializable
 */
public interface Classifier extends Serializable {

    /**
     * Initializes the classifier using the given training dataset.
     * 
     * <p>
     * <b>It is not intended for training the classifier. An appropriate
     * {@link fap.core.trainer.Trainer Trainer} should be used for training.</b>
     * 
     * @param trainset the training dataset
     * @throws Exception if an error occurs
     */
    public void initialize(Dataset trainset) throws Exception;

    /**
     * Classifies the given time series.
     * 
     * @param series the time series to be classified
     * @return the predicted class (label) of the time series
     * @throws Exception if an error occurs
     */
    public double classify(TimeSeries series) throws Exception;
    
}

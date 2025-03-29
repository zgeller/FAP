/*   
 * Copyright [2024] Zoltán Gellér
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fap.core.classifier;

import java.util.concurrent.Callable;

import fap.core.data.TimeSeries;

/**
 * A wrapper class that implements the {@link Callable} interface for
 * classifying time series.
 * 
 * <p>
 * The {@link Classifier#classify(TimeSeries) classify} method of the provided
 * classifier must be implemented in a thread safe manner.
 *
 * @author Zoltán Gellér
 * @version 2024.08.29.
 * @see Callable
 * @see TimeSeries
 * @see Classifier
 */
public class CallableClassifier implements Callable<Double> {

    /**
     * The classifier.
     */
    private Classifier classifier;

    /**
     * The time series to be classified.
     */
    private TimeSeries series;

    /**
     * Constructs a new {@code CallableClassifier} object.
     */
    public CallableClassifier() {
    }

    /**
     * Constructs a new {@code CallableClassifier} object using the specified
     * classifier and time series.
     * 
     * @param classifier the classifier
     * @param series     the time series to be classified using the specified classifier
     */
    public CallableClassifier(Classifier classifier, TimeSeries series) {
        this.setClassifier(classifier);
        this.setTimeSeries(series);
    }

    /**
     * Returns the classifier.
     * 
     * @return the classifier
     */
    public Classifier getClassifier() {
        return classifier;
    }

    /**
     * Sets the classifier.
     * 
     * @param classifier the classifier to set
     */
    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    /**
     * Returns the time series to be classified.
     * 
     * @return the series to be classified
     */
    public TimeSeries getTimeSeries() {
        return series;
    }

    /**
     * Sets the time series to be classified
     * 
     * @param series the time series to be classified
     */
    public void setTimeSeries(TimeSeries series) {
        this.series = series;
    }

    /**
     * Classifies the time {@link #series} using the {@link #classifier}.
     * 
     * @return a {@code Double} object representing the predicted class (label) of
     *         the time series
     */
    @Override
    public Double call() throws Exception {
        return classifier.classify(series);
    }

}

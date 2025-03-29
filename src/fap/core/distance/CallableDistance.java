/*   
 * Copyright 2024 Zoltán Gellér
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

package fap.core.distance;

import java.util.concurrent.Callable;

import fap.core.data.TimeSeries;

/**
 * A wrapper class that implements the {@link Callable} interface for computing
 * the distance between two time series.
 * 
 * <p>
 * The {@link Distance#distance(TimeSeries, TimeSeries) distance} method of the
 * provided distance measure must be implemented in a thread safe manner.
 * 
 * @author Zoltán Gellér
 * @version 2024.08.18.
 * @see Callable
 * @see TimeSeries
 * @see Distance
 */
public class CallableDistance implements Callable<Double> {

    /**
     * The distance measure.
     */
    private Distance distance;

    /**
     * The first time series.
     */
    private TimeSeries series1;

    /**
     * The second time series.
     */
    private TimeSeries series2;


    /**
     * Constructs a new {@code CallableDistance} object.
     */
    public CallableDistance() {
    }
    
    /**
     * Constructs a new {@code CallableDistance} object using the given distance
     * measure.
     * 
     * @param distance the distance measure
     */
    public CallableDistance(Distance distance) {
        setDistance(distance);
    }
    
    /**
     * Constructs a new {@code CallableDistance} object using the given distance
     * measure and two time series.
     * 
     * @param distance the distance measure
     * @param series1  the first time series
     * @param series2  the second time series
     */
    public CallableDistance(Distance distance, TimeSeries series1, TimeSeries series2) {
        setDistance(distance);
        setTimeSeries1(series1);
        setTimeSeries2(series2);
    }

    /**
     * Sets the distance measure.
     * 
     * @param distance the distance measure
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

    /**
     * Sets the first time series.
     * 
     * @param series the first time series
     */
    public void setTimeSeries1(TimeSeries series) {
        this.series1 = series;
    }

    /**
     * Returns the first time series.
     * 
     * @return the first time series
     */
    public TimeSeries getTimeSeries1() {
        return this.series1;
    }

    /**
     * Sets the second time series.
     * 
     * @param series the second time series
     */
    public void setTimeSeries2(TimeSeries series) {
        this.series2 = series;
    }

    /**
     * Returns the second time series.
     * 
     * @return the second time series
     */
    public TimeSeries getTimeSeries2() {
        return this.series2;
    }
    
    /**
     * Computes the distance between {@link #series1} and {@link #series2} using the
     * {@link #distance distance}.
     * 
     * @return a {@code Double} object representing the distance between
     *         {@code series1} and {@code series} computed using {@code distance}
     */
    @Override
    public Double call() throws Exception {
        return distance.distance(series1, series2);
    }

}

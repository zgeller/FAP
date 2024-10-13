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

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import fap.core.data.TimeSeries;

/**
 * Defines common methods and fields for distance measures.
 * 
 * <p>
 * Distances may be stored for future reuse. For this purpose, this class
 * utilizes a {@link HashMap} whose keys are the {@link TimeSeries#getIndex()
 * indices} of time series.
 * <ul>
 *  <li> <b>Time series should not be changed while storing distances is enabled.</b>
 *  <li> <b>Distance measures are required to {@link #clearStorage() clear} the
 *       storage when they change the value of any of their parameters that affect the
 *       distance.</b>
 * </ul>
 * 
 * @author Zoltán Gellér
 * @version 2024.09.22.
 * @see Distance
 */
public abstract class AbstractDistance implements Distance {

    private static final long serialVersionUID = 1L;

    /**
     * A {@link ConcurrentHashMap} for storing distances between time series.
     */
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>> storage = new ConcurrentHashMap<>();

    /**
     * Indicates whether storing distances is enabled.
     */
    private boolean storing = false;
    
    /**
     * Empty constructor.
     */
    public AbstractDistance() {
    }
    
    /**
     * Constructor with the possibility to enable or disable storing distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractDistance(boolean storing) {
        this.setStoring(storing);
    }

    /**
     * Clears the storage of distances.
     */
    public synchronized void clearStorage() {
        storage.clear();
    }

    /**
     * Enables or disables storing distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public void setStoring(boolean storing) {
        this.storing = storing;
    }

    /**
     * Returns whether storing distances is enabled.
     * 
     * @return {@code true} if storing distances is enabled
     */
    public boolean isStoring() {
        return this.storing;
    }

    /**
     * If storing distances is enabled the both the specified time series have
     * non-negative {@link TimeSeries#getIndex() indices}, it stores the specified
     * distance between the given time series for reuse.
     * 
     * @param series1  the first time series
     * @param series2  the second time series
     * @param distance the distance between {@code series1} and {@code series2} to
     *                 be stored
     */
    public void store(TimeSeries series1, TimeSeries series2, double distance) {
        
        int index1 = series1.getIndex();
        int index2 = series2.getIndex();

        if (!this.isStoring() || index1 < 0 || index2 < 0)
            return;

        if (index2 < index1) {
            int tmp = index1;
            index1 = index2;
            index2 = tmp;
        }
        
        ConcurrentHashMap<Integer, Double> newData = new ConcurrentHashMap<Integer, Double>();
        
        ConcurrentHashMap<Integer, Double> oldData = storage.putIfAbsent(index1, newData);
        
        if (oldData != null)
            oldData.put(index2, distance);
        else
            newData.put(index2, distance);
            
    }

    /**
     * Returns the stored distance between the two time series where the index of
     * {@code series1} is used as the primery and the index of {@code series2} as
     * the secondary key.
     * 
     * @param series1 the first time series whose index is used as the primary key
     * @param series2 the second time series wjpse omdex is used as the secondary
     *                key
     * @return the stored distance between {@code series1} and {@code series2}
     */
    private double getDistance(TimeSeries series1, TimeSeries series2) {

        double distance = Double.NaN;
            
        ConcurrentHashMap<Integer, Double> data = storage.get(series1.getIndex());

        if (data != null) {

            Double dist = data.get(series2.getIndex());

            if (dist != null)
                distance = dist;

        }
            
        return distance;

    }

    /**
     * Returns the stored distance between the specified time series or
     * {@link Double#NaN} if the storage does not contain the distance between them
     * or storing distances is not enabled.
     * 
     * @param series1 the first time series
     * @param series2 the second time series
     * @return the stored distance between {@code series1} and {@code series2} or
     *         {@link Double#NaN} if the storage does not contain the distance
     *         between them or storing distances is not enabled
     */
    public double recall(TimeSeries series1, TimeSeries series2) {

        int index1 = series1.getIndex();
        int index2 = series2.getIndex();
        
        if (!this.isStoring() || index1 < 0 || index2 < 0)
            return Double.NaN;

        double distance = Double.NaN;
        
        if (index1 < index2)
            distance = getDistance(series1, series2);
        else
            distance = getDistance(series2, series1);
        
        return distance;

    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

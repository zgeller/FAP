/*   
 * Copyright 2024-2025 Zoltán Gellér
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import fap.core.data.TimeSeries;

/**
 * Defines common methods and fields for distance measures.
 * 
 * <p>
 * Distances may be stored for future reuse. For this purpose, this class
 * utilizes a {@link ConcurrentHashMap} whose keys are the {@link TimeSeries#getIndex()
 * indices} of time series.
 * <ul>
 *  <li> <b>Time series should not be changed while storing distances is enabled.</b>
 *  <li> <b>Distance measures are required to {@link #clearStorage() clear} the
 *       storage when they change the value of any of their parameters that affect the
 *       distance.</b>
 * </ul>
 * 
 * @author Zoltán Gellér
 * @version 2025.03.14.
 * @see Distance
 */
public abstract class AbstractDistance implements Distance {

    private static final long serialVersionUID = 1L;

    /**
     * A {@link ConcurrentHashMap} for storing distances between time series.
     */
    private ConcurrentMap<Integer, ConcurrentMap<Integer, Double>> storage = new ConcurrentHashMap<>();

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
    public void clearStorage() {
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
        
        if (!this.isStoring())
            return;
        
        int index1 = series1.getIndex();
        int index2 = series2.getIndex();

        if (index1 < 0 || index2 < 0)
            return;

        if (index2 < index1) {
            int tmp = index1;
            index1 = index2;
            index2 = tmp;
        }
        
        ConcurrentMap<Integer, Double> newData = new ConcurrentHashMap<Integer, Double>();
        
        ConcurrentMap<Integer, Double> oldData = storage.putIfAbsent(index1, newData);
        
        if (oldData != null)
            oldData.put(index2, distance);
        else
            newData.put(index2, distance);
            
    }

    /**
     * Returns the stored distance between the time series with the given indices.
     * {@code index1} is used as the primary and {@code index2} as the secondary
     * key.
     * 
     * @param index1 the index of the first time series used as the primary key
     * @param index2 the index of the second time series used as the secondary key
     * @return the stored distance between {@code series1} and {@code series2}
     */
    private double getDistance(int index1, int index2) {

        double distance = Double.NaN;
            
        ConcurrentMap<Integer, Double> data = storage.get(index1);

        if (data != null) {

            Double dist = data.get(index2);

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

        if (!this.isStoring())
            return Double.NaN;
        
        int index1 = series1.getIndex();
        int index2 = series2.getIndex();
        
        if (index1 < 0 || index2 < 0)
            return Double.NaN;

        double distance = Double.NaN;
        
        if (index1 < index2)
            distance = getDistance(index1, index2);
        else
            distance = getDistance(index2, index1);
        
        return distance;

    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

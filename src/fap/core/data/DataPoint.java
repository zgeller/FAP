/*   
 * Copyright 2024-2025 Aleksa Todorović, Zoltán Gellér
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

package fap.core.data;

import java.io.Serializable;

/**
 * One point of time series data.
 * 
 * @author Aleksa Todorović, Zoltán Gellér
 * @version 2025.08.13.
 * @see Serializable
 * @see Comparable
 */
public class DataPoint implements Serializable, Comparable<DataPoint> {

    private static final long serialVersionUID = 1L;

    /**
     * The x-coordinate of the data point (usually denotes time).
     */
    private double x;

    /**
     * The y-coordinate of the data point (usually denotes value).
     */
    private double y;

    /**
     * Constructs a new data point with default values ({@code 0.0d}) of the coordinates.
     */
    public DataPoint() {
    }

    /**
     * Constructs a new data point and initializes it with the coordinates of the specified data point.
     * 
     * @param dp the data point whose coordinates will be used for initializing the
     *           new data point
     */
    public DataPoint(DataPoint dp) {
        this(dp.getX(), dp.getY());
    }

    /**
     * Constructs a new data point with the given coordinates.
     * 
     * @param x the value of the x-coordinate
     * @param y the value of the y-coordinate
     */
    public DataPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-coordinate of this data point.
     * 
     * @return the x-coordinate of this data point
     */
    public double getX() {
        return x;
    }
    
    /**
     * Sets the x-coordinate of this data point.
     * 
     * @param x the value of the x-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the y-coordinate of this data point.
     * 
     * @return the y-coordinate of this data point
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of this data point.
     * 
     * @param y the value of the y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }
    
    /**
     * Sets the coordinates of this data point.
     * 
     * @param x the value of the x-coordinate
     * @param y the value of the y-coordinate
     */
    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Shifts the x-coordinate of this data point by the given factor:<br>
     * {@code x = x + factor}.
     * 
     * @param factor the factor to shift the x-coordinate
     */
    public void shiftX(double factor) {
        this.x += factor;
    }

    /**
     * Shifts the y-coordinate of this data point by the given factor:<br>
     * {@code y = y + factor}.
     * 
     * @param factor the factor to shift the y-coordinate
     */
    public void shiftY(double factor) {
        this.y += factor;
    }

    /**
     * Shifts the coordinates of this data point by the given factors:<br>
     * {@code x = x + xFactor},<br>
     * {@code y = y + yFactor}.
     * 
     * @param xFactor the factor to shift the x-coordinate
     * @param yFactor the factor to shift the y-coordinate
     */
    public void shiftXY(double xFactor, double yFactor) {
        this.x += xFactor;
        this.y += yFactor;
    }

    /**
     * Scales the x-coordinate of this data point by the given factor:<br>
     * {@code x = x * factor}.
     * 
     * @param factor the factor to scale the x-coordinate
     */
    public void scaleX(double factor) {
        this.x *= factor;
    }

    /**
     * Scales the y-coordinate of this data point by the given factor:<br>
     * {@code y = y * factor}.
     * 
     * @param factor the factor to scale the y-coordinate
     */
    public void scaleY(double factor) {
        this.y *= factor;
    }

    /**
     * Scales the coordinates of this data point by the given factors:<br>
     * {@code x = x * xFactor},<br>
     * {@code y = y * yFactor}.
     * 
     * @param xFactor the factor to scale the x-coordinate
     * @param yFactor the factor to scale the y-coordinate
     */
    public void scaleXY(double xFactor, double yFactor) {
        this.x *= xFactor;
        this.y *= yFactor;
    }
    
    @Override
    public String toString() {
        return "(" + Double.toString(getX()) + ", " + Double.toString(getY()) + ")";
    }

    /**
     * Compares this data point with the specified data point based on the time
     * component (x-coordinate).
     * 
     * @param dp the data point to which this data point should be compared
     * @return a negative integer, zero, or a positive integer as the x-coordinate
     *         of this data point is less than, equal to, or greater than the
     *         x-coordinate of {@code dp}
     */
    @Override
    public int compareTo(DataPoint dp) {
        return Double.compare(x, dp.x);
    }

}

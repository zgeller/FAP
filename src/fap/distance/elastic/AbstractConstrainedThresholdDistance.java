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

package fap.distance.elastic;

import fap.util.TimeSeriesUtils;

/**
 * Abstract {@code Distance} class using the Sakoe-Chiba band or the Itakura
 * parallelogram and with start threshold parameter.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see AbstractConstrainedDistance
 * @see ConstrainedThresholdDistance
 */
public abstract class AbstractConstrainedThresholdDistance extends AbstractConstrainedDistance
        implements ConstrainedThresholdDistance {

    private static final long serialVersionUID = 1L;

    /**
     * The matching threshold. Two data points are considered to match if their
     * distance is not greater than the matching threshold. Default value is
     * specified by {@link TimeSeriesUtils#getMatchingThreshold()}.
     */
    private double epsilon = TimeSeriesUtils.getMatchingThreshold();
    
    /**
     * Empty constructor with default warping-window width
     * ({@link AbstractConstrainedDistance#r r}) and default matching threshold
     * ({@link #epsilon}).
     */
    public AbstractConstrainedThresholdDistance() {
    }
    
    /**
     * Constructor with the possibility to enable or disable storing distances, and
     * default warping-window width ({@link AbstractConstrainedDistance#r r}) and
     * default matching threshold ({@link #epsilon}).
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractConstrainedThresholdDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Constructor with the relative warping-window width and default matching
     * threshold ({@link #epsilon}).
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series)
     */
    public AbstractConstrainedThresholdDistance(double r) {
        super(r);
    }
    
    /**
     * Constructor with the relative warping-window width, the possibility to enable
     * or disable storing distances, and default matching threshold
     * ({@link #epsilon}).
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractConstrainedThresholdDistance(double r, boolean storing) {
        super(r, storing);
    }
    
    /**
     * Constructor with the relative warping-window width, a matching threshold
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon matching threshold
     */
    public AbstractConstrainedThresholdDistance(double r, double epsilon) {
        super(r);
        this.setEpsilon(epsilon);
    }
    
    /**
     * Constructor with the relative warping-window width, a matching threshold, and
     * the possibility to enable or disable storing distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param epsilon matching threshold
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractConstrainedThresholdDistance(double r, double epsilon, boolean storing) {
        super(r, storing);
        this.setEpsilon(epsilon);
    }

    /**
     * Sets the value of the matching threshold. Two data points are considered to
     * match if their distance is not greater than the matching threshold. Must be
     * {@code epsilon >= 0}.
     * 
     * @param epsilon the new value of the matching threshold, it must be
     *                {@code >= 0}
     * @throws IllegalArgumentException if {@code epsilon < 0}
     */
    @Override
    public void setEpsilon(double epsilon) {

        if (epsilon < 0)
            throw new IllegalArgumentException("Must be epsilon >= 0.");
        
        if (this.epsilon != epsilon) {
            this.clearStorage();
            this.epsilon = epsilon;
        }

    }

    @Override
    public double getEpsilon() {
        return this.epsilon;
    }

    /**
     * Initializes the specified distance with the common data structures of this
     * distance.
     * 
     * @param copy the distance whose data structures is to be initialized
     */
    protected void init(AbstractConstrainedThresholdDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setEpsilon(this.getEpsilon());
    }
    
    @Override
    public String toString() {
        return super.toString() + ", epsilon=" + getEpsilon();
    }

    
}

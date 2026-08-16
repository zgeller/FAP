/*   
 * Copyright 2024-2026 Zoltán Gellér
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

import fap.distance.AbstractCopyableDistance;

/**
 * Defines basic methods for constrained elastic distance measures.
 * 
 * <p>
 * For fixed-length time series, specifying the length avoids converting the
 * relative warping (editing) window width to an absolute value for every
 * pairwise distance calculation (i.e., it only needs to be computed once when
 * the relative width is modified).
 * 
 * @author Zoltán Gellér
 * @version 2026.08.16.
 * @see AbstractCopyableDistance
 * @see ConstrainedDistance
 */
public abstract class AbstractConstrainedDistance extends AbstractCopyableDistance implements ConstrainedDistance {

    private static final long serialVersionUID = 1L;

    /**
     * The relative width of the warping (editing) window (as a percentage of the
     * length of the time series). A negative value indicates that the absolute
     * width ({@link #w}) should be used. Default value is {@code 100}.
     */
    private double r = 100;

    /**
     * The length of the time series processed by this distance measure.
     */
    private final int length;

    /**
     * The absolute width of the warping (editing) window. A negative value
     * indicates that the relative width ({@link #r}) should be used. Default value
     * is {@code -1}.
     */
    private int w = -1;
    
    /**
     * Returns the default value {@code 0} of {@link #length}.
     * 
     * @return the default value {@code 0} of {@link #length}
     */
    private int defaultLength() {
        return 0;
    }
    
    /**
     * Constructs a default constrained distance measure.
     */
    protected AbstractConstrainedDistance() {
        this.length = this.defaultLength();
    }
    
    /**
     * Constructs a new constrained distance measure, specifying whether calculated
     * distances should be stored in memory for reuse.
     * 
     * @param storing {@code true} if calculated distances should be stored in
     *                memory for reuse
     */
    protected AbstractConstrainedDistance(boolean storing) {
        super(storing);
        this.length = this.defaultLength();
    }
    
    /**
     * Constructs a new constrained distance measure with a specified time series
     * length.
     * 
     * @param length the length of the time series
     */
    protected AbstractConstrainedDistance(int length) {
        this.length = length;
    }
    
    /**
     * Constructs a new constrained distance measure with a specified time series
     * length and an indication of whether calculated distances should be stored in
     * memory for reuse.
     * 
     * @param storing {@code true} if calculated distances should be stored in
     *                memory for reuse
     * @param length  the length of the time series
     */
    protected AbstractConstrainedDistance(boolean storing, int length) {
        super(storing);
        this.length = length;
    }

    /**
     * Returns the specified length of the time series processed by this distance
     * measure.
     * 
     * <p>
     * For fixed-length time series, specifying the length avoids converting the
     * relative warping window width to an absolute value for every pairwise
     * distance calculation (i.e., it only needs to be computed once when the
     * relative width is modified).
     * 
     * @return the specified length of time series processed by this distance measure.
     */
    public int getLength() {
        return this.length;
    }
    
    /**
     * Sets the relative width of the warping (editing) window (as a percentage of
     * the length of the time series). Must be in the range {@code [0..100]}.
     * Default value is 100.
     * 
     * <p>
     * If the specified length of the time series is greater than {@code 0}, it
     * calculates the absolute warping (editing) window width ({@link #w});
     * otherwise, it sets it to {@code -1}.
     * 
     * <p>
     * If the specified length of the time series is less than or equal to
     * {@code 0}, it clears the stored distances (if enabled) when the new warping
     * (editing) window width differs from the current one.
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series); must be in {@code [0..100]}
     * @throws IllegalArgumentException if {@code r} is not in {@code [0..100]}
     */
    @Override
    public void setR(double r) {

        if (r < 0 || r > 100)
            throw new IllegalArgumentException("r out of range [0..100]: " + r);
        
        if (this.length > 0)
            this.setW((int) (this.length * r / 100));
        
        else if (this.r != r) {
            this.clearStorage();
            this.r = r;
            this.w = -1;
        }
        
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Default value is 100.
     */
    @Override
    public double getR() {
        return this.r;
    }

    /**
     * Sets the absolute width of the warping (editing) window. Must be
     * {@code w >= 0}. The relative width is set to -1 (i.e. <code>{@link #r}
     *  = -1</code>).
     * 
     * <p>
     * Constrains the absolute width of the warping (editing) window based on the
     * length of the time series, if specified.
     * 
     * <p>
     * Clears the stored distances (if enabled) if the new warping (editing) window
     * width differs from the current one.
     * 
     * @param w the absolute width of the warping (editing) window; must be
     *          {@code >= 0}
     * @throws IllegalArgumentException if {@code w < 0}
     */
    @Override
    public void setW(int w) {

        if (w < 0)
            throw new IllegalArgumentException("Invalid w: " + w + " (must be >= 0)");
        
        if (this.length > 0 && w > this.length)
            w = this.length;

        if (this.w != w) {
            this.clearStorage();
            this.w = w;
            this.r = -1;
        }

    }

    @Override
    public int getW() {
        return this.w;
    }

    /**
     * Initializes the specified distance measure with the common data structures of this
     * distance measure.
     * 
     * @param copy the distance measure whose data structures is to be initialized
     */
    protected void init(AbstractConstrainedDistance copy, boolean deep) {
        super.init(copy, deep);
        double r = this.getR();
        int w = this.getW();
        if (r >= 0)
            copy.setR(r);
        if (w >= 0)
            copy.setW(w);
    }

    @Override
    public String toString() {
        return super.toString() + ", r=" + getR() + ", w=" + getW() + ", length=" + getLength();
    }
    
}

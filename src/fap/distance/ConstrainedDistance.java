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

package fap.distance;

import fap.core.distance.Distance;

/**
 * Declares basic methods for constrained elastic distance measures.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.01.
 * @see Distance
 */
public interface ConstrainedDistance extends Distance {

    /**
     * Sets the relative width of the warping (editing) window (as a percentage of
     * the length of the time series).
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series)
     */
    public void setR(double r);

    /**
     * Returns the relative width of the warping (editing) window (as a percentage
     * of the length of the time series).
     * 
     * @return the relative width of the warping (editing) window (as a percentage
     *         of the length of the time series)
     */
    public double getR();

    /**
     * Sets the absolute width of the warping (editing) window.
     * 
     * @param w the absolute width of the warping (editing) window
     */
    public void setW(int w);

    /**
     * Returns the absolute width of the warping (editing) window.
     * 
     * @return the absolute width of the warping (editing) window
     */
    public int getW();

}

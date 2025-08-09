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

package fap.core.tuner;

/**
 * Defines common methods and fields for classifier hyperparameter tuners.
 * 
 * @author Zoltán Gellér
 * @version 2025.04.21.
 * @see Tuner
 */
public abstract class AbstractTuner implements Tuner {

    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates whether this tuner affects the distance measure.
     */
    protected boolean affectsDistance;

    /**
     * The expected error rate. Default value is {@code Double.POSITIVE_INFINITY}.
     */
    protected double expectedError = Double.POSITIVE_INFINITY;

    /**
     * Empty constructor.
     */
    public AbstractTuner() {
    }
    
    /**
     * Constructor that sets whether the tuner affects the distance measure.
     * 
     * @param affectsDistance {@code true} if the tuner affacts the distance
     *                        measure
     */
    public AbstractTuner(boolean affectsDistance) {
        this.setAffectsDistance(affectsDistance);
    }
    
    public void setAffectsDistance(boolean affectsDistance) {
        this.affectsDistance = affectsDistance;
    }

    @Override
    public double getExpectedError() {
        return expectedError;
    }
    
    @Override
    public boolean affectsDistance() {
        return affectsDistance;
    }
    
    /**
     * Initializes the specified tuner with the common data structures of this
     * tuner.
     * 
     * @param copy the tuner whose data structures are to be initialized
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(AbstractTuner copy, boolean deep) {
        copy.setAffectsDistance(this.affectsDistance);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

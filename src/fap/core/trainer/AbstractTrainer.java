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

package fap.core.trainer;

/**
 * Defines common methods and fields for classifier trainers.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.23.
 * @see Trainer
 */
public abstract class AbstractTrainer implements Trainer {

    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates whether this trainer affects the distance measure.
     */
    protected boolean affectsDistance;

    /**
     * The expected error rate. Default value is {@code Double.POSITIVE_INFINITY}.
     */
    protected double expectedError = Double.POSITIVE_INFINITY;

    /**
     * Empty constructor.
     */
    public AbstractTrainer() {
    }
    
    /**
     * Constructor that sets whether the trainer affects the distance measure.
     * 
     * @param affectsDistance {@code true} if the trainer affacts the distance
     *                        measure
     */
    public AbstractTrainer(boolean affectsDistance) {
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
     * Initializes the specified trainer with the common data structures of this
     * trainer.
     * 
     * @param copy the classifier whose data structures is to be initialized
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(AbstractTrainer copy, boolean deep) {
        copy.setAffectsDistance(this.affectsDistance);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

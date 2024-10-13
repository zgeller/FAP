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

package fap.classifier.NN;

import fap.core.distance.Distance;
import fap.util.MathUtils;

/**
 * Defines common fields and methods for the {@link InverseKNNClassifier} and
 * {@link InverseSquaredKNNClassifier} classes.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.06.
 * @see KNNClassifier
 */
public class AbstractInverseKNNClassifier extends KNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * A small constant added to the denominator to avoid division by zero. Default
     * value is specified by {@link MathUtils#getZeroDenominator()}.
     */
    private double epsilon = MathUtils.getZeroDenominator();

    /**
     * Constructor with the default number of nearest neighbours
     * ({@link KNNClassifier#k k}), and the default
     * {@link #epsilon} value.
     */
    public AbstractInverseKNNClassifier() {
    }

    /**
     * Constructor with the number of nearest neighbours ({@code k}), and the
     * default {@link #epsilon} value.
     * 
     * @param k number of nearest neighbours, must be {@code k>=1}
     */
    public AbstractInverseKNNClassifier(int k) {
        super(k);
    }
    
    /**
     * Constructor with the number of nearest neighbours ({@code k}), number of
     * threads ({@code tnumber}), and the default {@link #epsilon} value.
     * 
     * @param k       number of nearest neighbours, must be {@code k>=1}
     * @param tnumber number of threads
     */
    public AbstractInverseKNNClassifier(int k, int tnumber) {
        super(k, tnumber);
    }

    /**
     * Constructor with the a distance measure ({@code distance}) and with the
     * default number of nearest neighbours ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public AbstractInverseKNNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructor with a distance measure ({@code distance}), number of nearest
     * neighbours ({@code k}) and with the default {@link #epsilon} value.
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     */
    public AbstractInverseKNNClassifier(Distance distance, int k) {
        super(distance, k);
    }
    
    /**
     * Constructor with a distance measure ({@code distance}), number of nearest
     * neighbours ({@code k}), number of threads ({@code tnumber}), and with the
     * default {@link #epsilon} value.
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     * @param tnumber  number of threads
     */
    public AbstractInverseKNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, k, tnumber);
    }

    /**
     * Constructor with the number of nearest neighbours ({@code k}), and {@code epsilon} value.
     * 
     * @param k       number of nearest neighbours, must be {@code k>=1}
     * @param epsilon a small constant that is to be added to the denominator to
     *                avoid division by zero, must be {@code > 0}
     */
    public AbstractInverseKNNClassifier(int k, double epsilon) {
        super(k);
        this.setEpsilon(epsilon);
    }
    
    /**
     * Constructor with the number of nearest neighbours ({@code k}),
     * {@code epsilon} value, and number of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbours, must be {@code k>=1}
     * @param epsilon a small constant that is to be added to the denominator to
     *                avoid division by zero, must be {@code > 0}
     * @param tnumber number of threads
     */
    public AbstractInverseKNNClassifier(int k, double epsilon, int tnumber) {
        super(k, tnumber);
        this.setEpsilon(epsilon);
    }

    /**
     * Constructor with a distance measure ({@code distance}), number of nearest
     * neighbours ({@code k}), and {@code epsilon} value.
     * 
     * @param distance the distance measure
     * @param k        number of nearest neighbours, must be {@code k>=1}
     * @param epsilon a small constant that is to be added to the denominator to
     *                avoid division by zero, must be {@code > 0}
     */
    public AbstractInverseKNNClassifier(Distance distance, int k, double epsilon) {
        super(distance, k);
        this.setEpsilon(epsilon);
    }
    
    /**
     * Constructor with a distance measure ({@code distance}), number of nearest
     * neighbours ({@code k}), and {@code epsilon} value, and number of threads
     * ({@code tnumber}).
     * 
     * @param distance the distance measure
     * @param k        number of nearest neighbours, must be {@code k>=1}
     * @param epsilon a small constant that is to be added to the denominator to
     *                avoid division by zero, must be {@code > 0}
     * @param tnumber  number of threads
     */
    public AbstractInverseKNNClassifier(Distance distance, int k, double epsilon, int tnumber) {
        super(distance, k, tnumber);
        this.setEpsilon(epsilon);
    }

    /**
     * Returns the value of {@code epsilon} (a small constant added to the
     * denominator to avoid division by zero).
     * 
     * @return the value of epsilon - a small constant added to the denominator to
     *         avoid division by zero
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Sets the value of {@code epsilon} (a small constant added to the denominator
     * to avoid division by zero). Must be {@code >=0}.
     * 
     * @param epsilon the epsilon value to set, must be {@code >=0}
     */
    public void setEpsilon(double epsilon) {
        if (epsilon < 0)
            throw new IllegalArgumentException("epsilon must be >= 0");
        this.epsilon = epsilon;
    }

}
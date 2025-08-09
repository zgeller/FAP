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

package fap.classifier.nn;

import java.util.HashMap;
import java.util.Map;

import fap.classifier.nn.util.LinkedDistanceNode;
import fap.classifier.nn.util.SortedList;
import fap.core.data.TimeSeries;
import fap.core.distance.Distance;

/**
 * Weighted kNN classifier utilizing inverse squared distances. The class of a
 * time series is determined by weighted voting of its k-nearest neighbors in
 * the training set. Weights are calculated as inverses of the squared
 * distances:
 * 
 * <blockquote> <img src="doc-files/InverseSquaredKNNClassifier-1.png">
 * </blockquote>
 * 
 * <p>
 * To avoid division by zero, a small value
 * ({@link AbstractInverseKNNClassifier#epsilon epsilon}) is added to the
 * denominator:
 * 
 * <blockquote> <img src="doc-files/InverseSquaredKNNClassifier-2.png">
 * </blockquote>
 *
 * <p>
 * The smaller the value of this constant, the greater the value of the
 * weighting will be in the case of identical time series, and the more
 * difficult it will be for time series belonging to another class to outvote
 * it.
 * 
 * <p>
 * For example, let {@code k = 4, ε = 0.001}, and<br>
 * <blockquote> 
 *  <code>d<sub>p</sub> = 0</code>, <br>
 *  <code>d<sub>q</sub> = 0.001</code>, <br>
 *  <code>d<sub>r</sub> = 0.002</code>, <br>
 *  <code>d<sub>s</sub> = 0.003</code>, <br>
 * </blockquote> 
 * and let <code>p</code> belong to class C1, and <code>q</code>,
 * <code>r</code>, and <code>s</code> belong to class C2.
 * 
 * <p>
 * Then:<br>
 * <blockquote> <code> 
 *  w<sub>p</sub> = 1000 <
 *  w<sub>q</sub> + w<sub>r</sub> +
 *  w<sub>s</sub> = 999.001 + 996.016 + 991.080 = 2986.097</code>, 
 * </blockquote> 
 * and the classifier will choose C2.
 * 
 * <p>
 * When {@code ε = 0}, <code>d<sub>p</sub> = Infinity</code> and C2 will never
 * outvote C1 regardless the distance of <code>q</code>, <code>r</code>, and
 * <code>s</code>.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> D.T. Larose, C.D. Larose, Discovering Knowledge in Data, 2nd ed., Wiley,
 *       2014. <a href="https://doi.org/10.1002/9781118874059">
 *                https://doi.org/10.1002/9781118874059</a>.
 *  <li> T.M. Mitchell, Machine Learning, McGraw-Hill, Inc., New York, NY, USA,
 *       1997.
 *  <li> P.-N. Tan, M. Steinbach, V. Kumar, A. Karpatne, Introduction to Data
 *       Mining, 2nd ed., Pearson Education, 2019.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.03.05.
 * @see AbstractInverseKNNClassifier
 */
public class InverseSquaredKNNClassifier extends AbstractInverseKNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * squared distances, with the default number of nearest neighbors
     * ({@link KNNClassifier#k k}) and the default
     * {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     */
    public InverseSquaredKNNClassifier() {
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * squared distances, with the given number of nearest neighbors ({@code k})
     * and the default {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     * 
     * @param k number of nearest neighbors, must be {@code >= 1}
     */
    public InverseSquaredKNNClassifier(int k) {
        super(k);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing inverse squared distances,
     * with the given number of nearest neighbors ({@code k}) and number of threads
     * ({@code tnumber}), and the default
     * {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     * 
     * @param k       number of nearest neighbors, must be {@code >= 1}
     * @param tnumber number of threads
     */
    public InverseSquaredKNNClassifier(int k, int tnumber) {
        super(k, tnumber);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * squared distances, with the given distance measure ({@code distance}) and
     * with the default number of nearest neighbors ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public InverseSquaredKNNClassifier(Distance distance) {
        super(distance);
    }
    

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * squared distances, with the given distance measure ({@code distance}), number
     * of nearest neighbors ({@code k}), and the default
     * {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     */
    public InverseSquaredKNNClassifier(Distance distance, int k) {
        super(distance, k);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing inverse squared distances,
     * with the given distance measure ({@code distance}), number of nearest
     * neighbors ({@code k}), and number of threads ({@code tnumber}), and the
     * default {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     * @param tnumber  number of threads
     */
    public InverseSquaredKNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, k, tnumber);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * squared distances, with the given number of nearest neighbors ({@code k})
     * and a small constant ({@code epsilon}) that is to be added to the denominator
     * to avoid division by zero.
     * 
     * @param k       number of nearest neighbors, must be {@code >= 1}
     * @param epsilon a small constant that is to be added to the denominator to
     *                avoid division by zero, must be {@code > 0}
     */
    public InverseSquaredKNNClassifier(int k, double epsilon) {
        super(k, epsilon);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing inverse squared distances,
     * with the given number of nearest neighbors ({@code k}), a small constant
     * ({@code epsilon}) that is to be added to the denominator to avoid division by
     * zero, and number of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbors, must be {@code >= 1}
     * @param epsilon a small constant that is to be added to the denominator to
     *                avoid division by zero, must be {@code > 0}
     * @param tnumber number of threads
     */
    public InverseSquaredKNNClassifier(int k, double epsilon, int tnumber) {
       super(k, epsilon, tnumber); 
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * squared distances, with the given distance measure ({@code distance}), number
     * of nearest neighbors ({@code k}), and a small constant ({@code epsilon})
     * that is to be added to the denominator to avoid division by zero.
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     * @param epsilon  a small constant that is to be added to the denominator to
     *                 avoid division by zero, must be {@code > 0}
     */
    public InverseSquaredKNNClassifier(Distance distance, int k, double epsilon) {
        super(distance, k, epsilon);
    }

    /**
     * Constructs a new weighted kNN classifier utilizing inverse squared distances,
     * with the given distance measure ({@code distance}), number of nearest
     * neighbors ({@code k}), a small constant ({@code epsilon}) that is to be
     * added to the denominator to avoid division by zero, and number of threads
     * ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     * @param epsilon  a small constant that is to be added to the denominator to
     *                 avoid division by zero, must be {@code > 0}
     * @param tnumber  number of threads
     */
    public InverseSquaredKNNClassifier(Distance distance, int k, double epsilon, int tnumber) {
        super(distance, k, epsilon, tnumber);
    }
    /**
     * Finds the best label among the nearest neighbors. Weights are calculated as
     * inverses of the squared distances.
     * 
     * @param list sorted list of the nearest neighbors
     * @return the best label
     */
    @Override
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();

        Map<Double, Double> neighbours = new HashMap<Double, Double>();

        double epsilon = this.getEpsilon();

        double bestLabel = node.obj.getLabel();
        double bestWeight = 1 / (node.distance * node.distance + epsilon);

        neighbours.put(bestLabel, bestWeight);
        node = node.next;

        while (node != null) {

            double label = node.obj.getLabel();
            double weight = 1 / (node.distance * node.distance + epsilon);

            if (neighbours.containsKey(label))
                weight += neighbours.get(label);
            neighbours.put(label, weight);

            if (weight > bestWeight) {
                bestLabel = label;
                bestWeight = weight;
            }

            node = node.next;
        }

        return bestLabel;
    }

    @Override
    public Object makeACopy(boolean deep) {
        InverseSquaredKNNClassifier copy = new InverseSquaredKNNClassifier();
        init(copy, deep);
        return copy;
    }
    
}

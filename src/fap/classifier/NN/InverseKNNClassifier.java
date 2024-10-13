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

import java.util.HashMap;

import fap.classifier.NN.util.LinkedDistanceNode;
import fap.classifier.NN.util.SortedList;
import fap.core.data.TimeSeries;
import fap.core.distance.Distance;

/**
 * Weighted kNN classifier utilizing inverse distances. The class of a time
 * series is determined by weighted voting of its k-nearest neighbours in the
 * training set. Weights are calculated using inverse distances:
 * 
 * <blockquote> <img src="doc-files/InverseKNNClassifier-1.png"> </blockquote>
 * 
 * <p>
 * To avoid division by zero, a small value
 * ({@link AbstractInverseKNNClassifier#epsilon epsilon}) is added to the
 * denominator:
 * 
 * <blockquote> <img src="doc-files/InverseKNNClassifier-2.png"> </blockquote>
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
 *  w<sub>s</sub> = 500 + 333.333 + 250 = 1083.333</code>, 
 * </blockquote> 
 * and the classifier will choose C2.
 * 
 * <p>
 * When {@code ε = 0}, <code>d<sub>p</sub> = Infinity</code> and C2 will never
 * outvote C1 regardless the distance of <code>q</code>, <code>r</code>, and
 * <code>s</code>.
 * 
 * 
 * <p>
 * References:
 * <ol>
 *  <li> S.A. Dudani, The Distance-Weighted k-Nearest-Neighbor Rule, IEEE Trans.
 *       Syst. Man. Cybern. SMC-6 (1976) 325–327.
 *       <a href="https://doi.org/10.1109/TSMC.1976.5408784">
 *          https://doi.org/10.1109/TSMC.1976.5408784</a>.
 *  <li> J. Xu, An Empirical Comparison of Weighting Functions for Multi-Label
 *       Distance Weighted K - Nearest Neighbour Method, in: 2011: pp. 13–20.
 *       <a href="https://doi.org/10.5121/csit.2011.1302">
 *          https://doi.org/10.5121/csit.2011.1302</a>.
 *  <li> J. Zavrel, An Empirical Re-Examination of Weighted Voting for k-NN, in:
 *       Proc. 7th Belgian-Dutch Conf. Mach. Learn., 1997: pp. 139–148.</li>
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see AbstractInverseKNNClassifier
 */
public class InverseKNNClassifier extends AbstractInverseKNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * distances, with the default number of nearest neighbours
     * ({@link KNNClassifier#k k}) and the default
     * {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     */
    public InverseKNNClassifier() {
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * distances, with the given number of nearest neighbours ({@code k}) and the
     * default {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     * 
     * @param k number of nearest neighbours, must be {@code >= 1}
     */
    public InverseKNNClassifier(int k) {
        super(k);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing inverse distances, with
     * the given number of nearest neighbours ({@code k}) and number of threads
     * ({@code tnumber}), and the default
     * {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     * 
     * @param k       number of nearest neighbours, must be {@code >= 1}
     * @param tnumber number of threads
     */
    public InverseKNNClassifier(int k, int tnumber) {
        super(k, tnumber);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier inverse distances,
     * with the given distance measure ({@code distance}) and with the default
     * number of nearest neighbours ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public InverseKNNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * distances, with the given distance measure ({@code distance}), number of
     * nearest neighbours ({@code k}), and the default
     * {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     */
    public InverseKNNClassifier(Distance distance, int k) {
        super(distance, k);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing inverse distances, with
     * the given distance measure ({@code distance}), number of nearest neighbours
     * ({@code k}), and number of threads ({@code tnumber}), and the default
     * {@link AbstractInverseKNNClassifier#epsilon epsilon} value.
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     * @param tnumber  number of threads
     */
    public InverseKNNClassifier(Distance distance, int k, int tnumber) {
       super(distance, k, tnumber); 
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * distances, with the given number of nearest neighbours ({@code k}) and a
     * small constant ({@code epsilon}) that is to be added to the denominator to
     * avoid division by zero.
     * 
     * @param k       number of nearest neighbours, must be {@code >= 1}
     * @param epsilon a small constant that is to be added to the denominator to
     *                avoid division by zero, must be {@code > 0}
     */
    public InverseKNNClassifier(int k, double epsilon) {
        super(k, epsilon);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing inverse distances, with
     * the given number of nearest neighbours ({@code k}), a small constant
     * ({@code epsilon}) that is to be added to the denominator to avoid division
     * by zero, and number of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbours, must be {@code >= 1}
     * @param epsilon a small constant that is to be added to the denominator to
     *                avoid division by zero, must be {@code > 0}
     * @param tnumber number of threads
     */
    public InverseKNNClassifier(int k, double epsilon, int tnumber) {
        super(k, epsilon, tnumber);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing inverse
     * distances, with the given distance measure ({@code distance}), number of
     * nearest neighbours ({@code k}), and a small constant ({@code epsilon}) that
     * is to be added to the denominator to avoid division by zero.
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     * @param epsilon  a small constant that is to be added to the denominator to
     *                 avoid division by zero, must be {@code > 0}
     */
    public InverseKNNClassifier(Distance distance, int k, double epsilon) {
        super(distance, k, epsilon);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing inverse distances, with
     * the given distance measure ({@code distance}), number of nearest neighbours
     * ({@code k}), a small constant ({@code epsilon}) that is to be added to the
     * denominator to avoid division by zero, and number of threads
     * ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     * @param epsilon  a small constant that is to be added to the denominator to
     *                 avoid division by zero, must be {@code > 0}
     * @param tnumber  number of threads
     */
    public InverseKNNClassifier(Distance distance, int k, double epsilon, int tnumber) {
        super(distance, k, epsilon, tnumber);
    }

    /**
     * Finds the best label among the nearest neighbours. Weights are calculated
     * using inverse distances.
     * 
     * @param list sorted list of the nearest neighbours
     * @return the best label
     */
    @Override
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();

        HashMap<Double, Double> neighbours = new HashMap<Double, Double>();
        
        double epsilon = this.getEpsilon();

        double bestLabel = node.obj.getLabel();
        double bestWeight = 1 / (node.distance + epsilon);

        neighbours.put(bestLabel, bestWeight);
        node = node.next;

        while (node != null) {

            double label = node.obj.getLabel();
            double weight = 1 / (node.distance + epsilon);

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
        InverseKNNClassifier copy = new InverseKNNClassifier();
        init(copy, deep);
        return copy;
    }
    
}

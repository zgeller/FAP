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
 * Weighted kNN classifier utilizing the uniform weighting function. The class
 * of a time series is determined by weighted voting of its k-nearest neighbours
 * in the training set. Weights are calculated using inverse ranks of the
 * neighbours:
 * 
 * <blockquote> <img src="doc-files/UniformKNNClassifier-1.png"> </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> J. Gou, T. Xiong, Y. Kuang, A Novel Weighted Voting for K-Nearest
 *       Neighbor Rule, J. Comput. 6 (2011) 833–840. 
 *       <a href="https://doi.org/10.4304/jcp.6.5.833-840">
 *          https://doi.org/10.4304/jcp.6.5.833-840</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see KNNClassifier
 */
public class UniformKNNClassifier extends KNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * uniform weighting function, with the default number of nearest neighbours
     * ({@link KNNClassifier#k k}).
     */
    public UniformKNNClassifier() {
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * uniform weighting function, with the given number of nearest neighbours
     * ({@code k}).
     * 
     * @param k number of nearest neighbours, must be {@code >= 1}
     */
    public UniformKNNClassifier(int k) {
        super(k);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing the uniform weighting
     * function, with the given number of nearest neighbours ({@code k}), and number
     * of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbours, must be {@code >= 1}
     * @param tnumber number of threads
     */
    public UniformKNNClassifier(int k, int tnumber) {
        super(k, tnumber);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * uniform weighting function, with the given distance measure
     * ({@code distance}) and with the default number of nearest neighbours
     * ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public UniformKNNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * uniform weighting function, with the given distance measure
     * ({@code distance}) and number of nearest neighbours ({@code k}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     */
    public UniformKNNClassifier(Distance distance, int k) {
        super(distance, k);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing the uniform weighting
     * function, with the given distance measure ({@code distance}), number of
     * nearest neighbours ({@code k}), and number of threads ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     * @param tnumber  number of threads
     */
    public UniformKNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, k, tnumber);
    }

    /**
     * Finds the best label among the nearest neighbours. Weights are calculated
     * using inverse ranks of the neighbours.
     * 
     * @param list sorted list of the nearest neighbours
     * @return the best label
     */
    @Override
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();

        HashMap<Double, Double> neighbours = new HashMap<Double, Double>();

        double bestLabel = node.obj.getLabel();
        double bestWeight = 1.0d;

        neighbours.put(bestLabel, bestWeight);
        node = node.next;
        int index = 2;

        while (node != null) {

            double label = node.obj.getLabel();
            double weight = 1.0d / index;

            if (neighbours.containsKey(label))
                weight += neighbours.get(label);
            neighbours.put(label, weight);

            if (weight > bestWeight) {
                bestLabel = label;
                bestWeight = weight;
            }

            node = node.next;
            index++;
        }

        return bestLabel;
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        UniformKNNClassifier copy = new UniformKNNClassifier();
        init(copy, deep);
        return copy;
    }

}

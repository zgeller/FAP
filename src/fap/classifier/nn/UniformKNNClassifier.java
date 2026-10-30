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

package fap.classifier.nn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fap.classifier.nn.util.LinkedDistanceNode;
import fap.classifier.nn.util.SortedList;
import fap.data.TimeSeries;
import fap.distance.Distance;

/**
 * Weighted kNN classifier utilizing the uniform weighting function. The class
 * of a time series is determined by weighted voting of its k-nearest neighbors
 * in the training set. Weights are calculated using inverse ranks of the
 * neighbors:
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
 * @version 2026.10.27.
 * @see KNNClassifier
 */
public class UniformKNNClassifier extends KNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses the
     * uniform weighting function, with the default number of nearest neighbors
     * ({@link KNNClassifier#k k}).
     */
    public UniformKNNClassifier() {
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses the
     * uniform weighting function, with the given number of nearest neighbors
     * ({@code k}).
     * 
     * @param k the number of nearest neighbors, must be {@code >= 1}
     */
    public UniformKNNClassifier(int k) {
        super(k);
    }
    
    /**
     * Constructs a new weighted kNN classifier that uses the uniform weighting
     * function, with the given number of nearest neighbors ({@code k}), and number
     * of threads ({@code tnumber}).
     * 
     * @param k       the number of nearest neighbors, must be {@code >= 1}
     * @param tnumber the number of threads
     */
    public UniformKNNClassifier(int k, int tnumber) {
        super(k, tnumber);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses the
     * uniform weighting function, with the given distance measure
     * ({@code distance}) and with the default number of nearest neighbors
     * ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public UniformKNNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier that uses the
     * uniform weighting function, with the given distance measure
     * ({@code distance}) and number of nearest neighbors ({@code k}).
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors, must be {@code >= 1}
     */
    public UniformKNNClassifier(Distance distance, int k) {
        super(distance, k);
    }
    
    /**
     * Constructs a new weighted kNN classifier that uses the uniform weighting
     * function, with the given distance measure ({@code distance}), number of
     * nearest neighbors ({@code k}), and number of threads ({@code tnumber}).
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors, must be {@code >= 1}
     * @param tnumber  the number of threads
     */
    public UniformKNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, k, tnumber);
    }

    /**
     * Finds the best label among the nearest neighbors. Weights are calculated
     * using inverse ranks of the neighbors.
     * 
     * @param list the sorted list of the nearest neighbors
     * @return the best label
     */
    @Override
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();

        Map<Double, Double> neighbors = new HashMap<>();

        double bestLabel = node.obj.getLabel();
        double bestWeight = 1.0d;

        neighbors.put(bestLabel, bestWeight);
        node = node.next;
        int index = 2;

        while (node != null) {

            double label = node.obj.getLabel();
            double weight = 1.0d / index;

            weight = neighbors.merge(label, weight, Double::sum);
            
            if (weight > bestWeight) {
                bestLabel = label;
                bestWeight = weight;
            }

            node = node.next;
            index++;
            
        }

        return bestLabel;
        
    }
    
    /**
     * Returns the best labels evaluated across the first {@code i} nearest
     * neighbors in {@code list}, for each {@code i} in the range
     * {@code [1, k]}.
     *
     * @param list the sorted list of nearest neighbors
     * @return an array of best labels evaluated over the first {@code i} nearest
     *         neighbors, for each {@code i} in {@code [1, k]}
     */
    protected double[] getBestLabels(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();
        
        final int k = this.getK();          // the number of nearest neighbors that should be considered
        final int count = list.getCount();  // the actual number of nearest neighbors

        List<Map<Double, Double>> neighbors = new ArrayList<>(k);
        
        double[] bestLabels = new double[k];
        double[] bestWeights = new double[k];

        double label = node.obj.getLabel();
        
        for (int i = 0; i < k; i++) {
            
            Map<Double, Double> hmap = new HashMap<>();
            double weight = 1;
            hmap.put(label, weight);
            neighbors.add(hmap);
            
            bestLabels[i] = label;
            bestWeights[i] = 1;
            
        }

        node = node.next;
        int index = 1;

        while (node != null) {

            label = node.obj.getLabel();
            
            double weight = 1.0d / (index + 1);

            for (int i = index; i < count; i++) {
                
                double weightSum = neighbors.get(i).merge(label, weight, Double::sum);
               
                if (weightSum > bestWeights[i]) {
                    bestLabels[i] = label;
                    bestWeights[i] = weightSum;
                }
                
            }

            node = node.next;
            index++;
            
        }

        // if there is less than k nearest neighbors
        // the best label is determined based on the available number of nearest neighbors
        if (count < k) {
            double bestLabel = bestLabels[count - 1];
            for (int i = count; i < k; i++)
                bestLabels[i] = bestLabel;
        }
        
        return bestLabels;
        
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        UniformKNNClassifier copy = new UniformKNNClassifier();
        init(copy, deep);
        return copy;
    }

}

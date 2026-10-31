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
 * Weighted kNN classifier utilizing the Fibonacci weighting function. The class
 * of a time series is determined by weighted voting of its k-nearest neighbors
 * in the training set. Weights are calculated based on the Fibonacci sequence:
 * 
 * <blockquote> <img src="doc-files/FibonacciKNNClassifier-1.png"> </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> T.-L. Pao, Y.-T. Chen, J.-H. Yeh, Y.-M. Cheng, Y.-Y. Lin, A Comparative
 *       Study of Different Weighting Schemes on KNN-Based Emotion Recognition in
 *       Mandarin Speech, in: D.-S. Huang, L. Heutte, M. Loog (Eds.), Adv. Intell.
 *       Comput. Theor. Appl. With Asp. Theor. Methodol. Issues, Springer Berlin
 *       Heidelberg, Berlin, Heidelberg, 2007: pp. 997–1005.
 *       <a href="https://doi.org/10.1007/978-3-540-74171-8_101">
 *          https://doi.org/10.1007/978-3-540-74171-8_101</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2026.10.31.2
 * @see KNNClassifier
 */
public class FibonacciKNNClassifier extends KNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * The Fibonacci sequence.
     */
    private int[] fibonacci;

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses the
     * Fibonacci weighting function, with the default number of nearest neighbors
     * ({@link KNNClassifier#k k}).
     */
    public FibonacciKNNClassifier() {
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses the
     * Fibonacci weighting function, with the given number of nearest neighbors
     * ({@code k}).
     * 
     * @param k the number of nearest neighbors, must be {@code >= 1}
     */
    public FibonacciKNNClassifier(int k) {
        super(k);
    }
    
    /**
     * Constructs a new weighted kNN classifier that uses the Fibonacci weighting
     * function, with the given number of nearest neighbors ({@code k}), and number
     * of threads ({@code tnumber}).
     * 
     * @param k       the number of nearest neighbors, must be {@code >= 1}
     * @param tnumber the number of threads
     */
    public FibonacciKNNClassifier(int k, int tnumber) {
        super(k, tnumber);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier that uses the
     * Fibonacci weighting function, with the given distance measure
     * ({@code distance}) and with the default number of nearest neighbors
     * ({@link KNNClassifier#k k}).
     * 
     * @param distance the distance measure
     */
    public FibonacciKNNClassifier(Distance distance) {
        super(distance);
    }
    

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses the
     * Fibonacci weighting function, with the given distance measure
     * ({@code distance}) and number of nearest neighbors ({@code k}).
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors, must be {@code >= 1}
     */
    public FibonacciKNNClassifier(Distance distance, int k) {
        super(distance, k);
    }
    
    /**
     * Constructs a new weighted kNN classifier that uses the Fibonacci weighting
     * function, with the given distance measure ({@code distance}), number of
     * nearest neighbors ({@code k}), and number of threads ({@code tnumber}).
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors, must be {@code >= 1}
     * @param tnumber  the number of threads
     */
    public FibonacciKNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, k, tnumber);
    }

    @Override
    public void setK(int k) {
        super.setK(k);
        initFibonacci(k);
    }

    /**
     * Populates {@link #fibonacci} with the Fibonacci sequence.
     * 
     * @param k the length of the sequence
     */
    private void initFibonacci(int k) {
        fibonacci = new int[k];
        fibonacci[--k] = 1;
        if (k > 0) {
            fibonacci[--k] = 1;
            while (k > 0)
                fibonacci[--k] = fibonacci[k + 1] + fibonacci[k + 2];
        }
    }

    /**
     * Finds the best label among the nearest neighbors using closeness ranks as
     * weights.
     * 
     * @param list the sorted list of the nearest neighbors
     * @return the best label
     */
    @Override
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();
        Map<Double, Integer> neighbors = new HashMap<>();

        int len = list.getCount();
        if ((fibonacci == null) || (len != fibonacci.length))
            initFibonacci(len);

        double bestLabel = node.obj.getLabel();
        int index = 0;
        int bestWeight = fibonacci[0];

        neighbors.put(bestLabel, bestWeight);
        node = node.next;
        index++;

        while (node != null) {

            double label = node.obj.getLabel();
            int weight = fibonacci[index];

            weight = neighbors.merge(label, weight, Integer::sum);

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

        List<Map<Double, Integer>> neighbors = new ArrayList<>(k);
        
        if ((fibonacci == null) || (count != fibonacci.length))
            initFibonacci(count);
        
        double[] bestLabels = new double[k];
        int[] bestWeights = new int[k];

        double label = node.obj.getLabel();
        
        final int lastIndex = count - 1;
        
        for (int i = 0; i < k; i++) {
            
            final int weight = i < count ? fibonacci[lastIndex - i] : fibonacci[lastIndex];
            
            Map<Double, Integer> hmap = new HashMap<>();
            hmap.put(label, weight);
            neighbors.add(hmap);
            
            bestLabels[i] = label;
            bestWeights[i] = weight;
            
        }

        node = node.next;
        int index = 1;

        while (node != null) {

            label = node.obj.getLabel();
            
            int findex = lastIndex;
            
            for (int i = index; i < count; i++) {

                int weightSum = neighbors.get(i).merge(label, fibonacci[findex--], Integer::sum);
                
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
            double bestLabel = bestLabels[lastIndex];
            for (int i = count; i < k; i++)
                bestLabels[i] = bestLabel;
        }
        
        return bestLabels;
        
    }
    

    @Override
    public Object makeACopy(boolean deep) {
        FibonacciKNNClassifier copy = new FibonacciKNNClassifier();
        init(copy, deep);
        return copy;
    }
    
}

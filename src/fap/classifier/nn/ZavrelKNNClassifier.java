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
 * Weighted kNN classifier utilizing Zavrel's weighting function. The class of a
 * time series is determined by weighted voting of its k-nearest neighbors in
 * the training set. Weights are calculated as defined by Zavrel:
 * 
 * <blockquote> <img src="doc-files/ZavrelKNNClassifier-1.png"> </blockquote>
 * 
 * where {@code α} dictates the slope of the exponential decay function, and
 * {@code β} governs the power of the decay.
 * 
 * <p>
 * <ul>
 *  <li> default values: {@code α = β = 1}
 * </ul>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> J. Zavrel, An Empirical Re-Examination of Weighted Voting for k-NN, in:
 *       Proc. 7th Belgian-Dutch Conf. Mach. Learn., 1997: pp. 139–148.
 *  <li> J. Xu, An Empirical Comparison of Weighting Functions for Multi-Label
 *       Distance Weighted K - Nearest Neighbour Method, in: 2011: pp. 13–20.
 *       <a href="https://doi.org/10.5121/csit.2011.1302">
 *          https://doi.org/10.5121/csit.2011.1302</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2026.10.27.
 * @see KNNClassifier
 */
public class ZavrelKNNClassifier extends KNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * The slope of the exponential decay function. The default value is {@code 1}.
     */
    private double alpha = 1;

    /**
     * The power of the exponential decay function. The default value is {@code 1}.
     */
    private double beta = 1;

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses Zavrel's
     * weighting function, with the default number of nearest neighbors
     * ({@link KNNClassifier#k k}) and the default values of the parameters
     * {@link #alpha} and {@link #beta}.
     */
    public ZavrelKNNClassifier() {
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier that uses Zavrel's
     * weighting function, with the specified number of nearest neighbors
     * ({@code k}), and the default values of the parameters {@link #alpha} and
     * {@link #beta}.
     * 
     * @param k the number of nearest neighbors, must be {@code >= 1}
     */
    public ZavrelKNNClassifier(int k) {
        super(k);
    }
    
    /**
     * Constructs a new weighted kNN classifier that uses Zavrel's weighting
     * function, with the specified number of nearest neighbors ({@code k}), number
     * of threads ({@code tnumber}), and the default values of the parameters
     * {@link #alpha} and {@link #beta}.
     * 
     * @param k       the number of nearest neighbors, must be {@code >= 1}
     * @param tnumber the number of threads
     */
    public ZavrelKNNClassifier(int k, int tnumber) {
        super(k, tnumber);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses Zavrel's
     * weighting function, with the given number of nearest neighbors ({@code k}),
     * and slope ({@code alpha}) and power ({@code beta}) of the exponential decay
     * function.
     * 
     * @param k     the number of nearest neighbors, must be {@code >= 1}
     * @param alpha the slope of the exponential decay function
     * @param beta  the power of the exponential decay function
     */
    public ZavrelKNNClassifier(int k, double alpha, double beta) {
        super(k);
        this.setAlpha(alpha);
        this.setBeta(beta);
    }
    
    /**
     * Constructs a new weighted kNN classifier that uses Zavrel's weighting
     * function, with the specified number of nearest neighbors ({@code k}), slope
     * ({@code alpha}) and power ({@code beta}) of the exponential decay function,
     * and number of threads ({@code tnumber}).
     * 
     * @param k       the number of nearest neighbors, must be {@code >= 1}
     * @param alpha   the slope of the exponential decay function
     * @param beta    the power of the exponential decay function
     * @param tnumber the number of threads
     */
    public ZavrelKNNClassifier(int k, double alpha, double beta, int tnumber) {
        super(k, tnumber);
        this.setAlpha(alpha);
        this.setBeta(beta);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier that uses Zavrel's
     * weighting function, with the given distance measure ({@code distance}) and
     * with the default number of nearest neighbors ({@link KNNClassifier#k k}).
     * 
     * @param distance the distance measure
     */
    public ZavrelKNNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier that uses Zavrel's
     * weighting function, with the given distance measure ({@code distance}),
     * number of nearest neighbors ({@code k}), and the default values of the
     * parameters {@link #alpha} and {@link #beta}.
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors, must be {@code >= 1}
     */
    public ZavrelKNNClassifier(Distance distance, int k) {
        super(distance, k);
    }
    
    /**
     * Constructs a new weighted kNN classifier that uses Zavrel's weighting
     * function, with the specified distance measure ({@code distance}), number of
     * nearest neighbors ({@code k}), number of threads ({@code tnumber}), and the
     * default values of the parameters {@link #alpha} and {@link #beta}.
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors, must be {@code >= 1}
     * @param tnumber  the number of threads
     */
    public ZavrelKNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, k, tnumber);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier that uses Zavrel's
     * weighting function, with the given distance measure ({@code distance}),
     * number of nearest neighbors ({@code k}), and slope ({@code alpha}) and power
     * ({@code beta}) of the exponential decay function.
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors, must be {@code >= 1}
     * @param alpha    the slope of the exponential decay function
     * @param beta     the power of the exponential decay function
     */
    public ZavrelKNNClassifier(Distance distance, int k, double alpha, double beta) {
        super(distance, k);
        this.setAlpha(alpha);
        this.setBeta(beta);
    }

    /**
     * Constructs a new weighted kNN classifier that uses Zavrel's weighting
     * function, with the given distance measure ({@code distance}), number of
     * nearest neighbors ({@code k}), slope ({@code alpha}) and power
     * ({@code beta}) of the exponential decay function, and number of threads
     * ({@code tnumber}).
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors, must be {@code >= 1}
     * @param alpha    the slope of the exponential decay function
     * @param beta     the power of the exponential decay function
     * @param tnumber  the number of threads
     */
    public ZavrelKNNClassifier(Distance distance, int k, double alpha, double beta, int tnumber) {
        super(distance, k, tnumber);
        this.setAlpha(alpha);
        this.setBeta(beta);
    }
    
    /**
     * Returns the slope ({@code alpha}) of the exponential decay function.
     * 
     * @return the slope ({@code alpha}) of the exponential decay function
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Sets the slope ({@code alpha}) of the exponential decay function.
     * 
     * @param alpha the slope of the exponential decay function
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Returns the power ({@code beta}) of the exponential decay function.
     * 
     * @return the power ({@code beta}) of the exponential decay function
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Sets the power ({@code beta}) of the exponential decay function.
     * 
     * @param beta power of the exponential decay function
     */
    public void setBeta(double beta) {
        this.beta = beta;
    }

    /**
     * Finds the best label among the nearest neighbors. Weights are calculated as
     * defined by Zavrel.
     * 
     * @param list the sorted list of the nearest neighbors
     * @return the best label
     */
    @Override
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();

        Map<Double, Double> neighbors = new HashMap<>();
        
        double alpha = this.getAlpha();
        double beta = this.getBeta();

        double bestLabel = node.obj.getLabel();
        double bestWeight = Math.exp(-alpha * Math.pow(node.distance, beta));

        neighbors.put(bestLabel, bestWeight);
        node = node.next;

        while (node != null) {

            double label = node.obj.getLabel();
            double weight = Math.exp(-alpha * Math.pow(node.distance, beta));

            weight = neighbors.merge(label, weight, Double::sum);
            
            if (weight > bestWeight) {
                bestLabel = label;
                bestWeight = weight;
            }

            node = node.next;
            
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
        
        double alpha = this.getAlpha();
        double beta = this.getBeta();

        List<Map<Double, Double>> neighbors = new ArrayList<>(k);
        
        double[] bestLabels = new double[k];
        double[] bestWeights = new double[k];

        double label = node.obj.getLabel();
        double weight = Math.exp(-alpha * Math.pow(node.distance, beta));
        
        for (int i = 0; i < k; i++) {
            
            Map<Double, Double> hmap = new HashMap<>();
            hmap.put(label, weight);
            neighbors.add(hmap);
            
            bestLabels[i] = label;
            bestWeights[i] = weight;
            
        }

        node = node.next;
        int index = 1;

        while (node != null) {

            label = node.obj.getLabel();
            
            weight = Math.exp(-alpha * Math.pow(node.distance, beta));

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
    
    /**
     * Sets the parameters of the specified classifier to be equal to the parameters
     * of this classifier.
     * 
     * @param copy the classifier whose parameters are to be set
     */
    protected void init(ZavrelKNNClassifier copy, boolean deep) {
        super.init(copy, deep);
        copy.setAlpha(this.getAlpha());
        copy.setBeta(this.getBeta());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        KNNClassifier copy = new KNNClassifier();
        init(copy, deep);
        return copy;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", alpha=" + getAlpha() + ", beta=" + getBeta();
    }
    
}

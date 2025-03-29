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
import java.util.Map;

import fap.classifier.NN.util.DistanceNode;
import fap.classifier.NN.util.LinkedDistanceNode;
import fap.classifier.NN.util.SortedList;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.distance.Distance;
import fap.exception.EmptyDatasetException;
import fap.exception.IncomparableTimeSeriesException;
import fap.util.ThreadUtils;

/**
 * Weighted kNN classifier utilizing Macleod's weighting function. The class of a
 * time series is determined by weighted voting of its k-nearest neighbors in
 * the training set. Weights are calculated as defined by Macleod:
 * 
 * <blockquote> <img src="doc-files/MacleodKNNClassifier-1.png">, </blockquote>
 * 
 * where {@code s >= k} and {@code α >= 0}.
 * 
 * <p>
 * <ul>
 *  <li> when {@code s = k} and {@code α = 0}, it reverts to Dudani's weighting
 *       function
 *  <li> default values: {@code s = k}, {@code α = 1}
 * </ul>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> J. Macleod, A. Luk, D. Titterington, A Re-Examination of the
 *       Distance-Weighted k-Nearest Neighbor Classification Rule, IEEE Trans. Syst.
 *       Man. Cybern. 17 (1987) 689–696. 
 *       <a href="https://doi.org/10.1109/TSMC.1987.289362">
 *          https://doi.org/10.1109/TSMC.1987.289362</a>.
 *  <li> J. Xu, An Empirical Comparison of Weighting Functions for Multi-Label
 *       Distance Weighted K - Nearest Neighbour Method, in: 2011: pp. 13–20. 
 *       <a href="https://doi.org/10.5121/csit.2011.1302">
 *          https://doi.org/10.5121/csit.2011.1302</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.03.05.2
 * @see KNNClassifier
 */
public class MacleodKNNClassifier extends KNNClassifier {

    private static final long serialVersionUID = 1L;

    private int s = -1;

    private double alpha = 1;

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing Macleod's
     * weighting function, with the default number of nearest neighbors
     * ({@link KNNClassifier#k k}) and default values of parameters
     * ({@code s = k, alpha = 1}).
     */
    public MacleodKNNClassifier() {
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing Macleod's
     * weighting function, with the specified number of nearest neighbors
     * ({@link KNNClassifier#k k}) and default values of parameters
     * ({@code s = k, alpha = 1}).
     * 
     * @param k number of nearest neighbors, must be {@code >= 1}
     */
    public MacleodKNNClassifier(int k) {
        super(k);
    }

    /**
     * Constructs a new weighted kNN classifier utilizing Macleod's weighting
     * function, with the specified number of nearest neighbors
     * ({@link KNNClassifier#k k}), default values of parameters
     * ({@code s = k, alpha = 1}), and number of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbors, must be {@code >= 1}
     * @param tnumber number of threads
     */
    public MacleodKNNClassifier(int k, int tnumber) {
        super(k, tnumber);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing Macleod's
     * weighting function, with the specified number of nearest neighbors
     * ({@code k}) and parameters ({@code s, alpha}).
     * 
     * @param k     number of nearest neighbors, must be {@code >= 1}
     * @param s     should be {@code >= k}
     * @param alpha must be {@code >= 0}
     */
    public MacleodKNNClassifier(int k, int s, double alpha) {
        super(k);
        this.setS(s);
        this.setAlpha(alpha);
    }
    
    /**
     * Constructs a new weighted kNN classifier utilizing Macleod's weighting
     * function, with the specified number of nearest neighbors ({@code k}),
     * parameters ({@code s, alpha}), and number of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbors, must be {@code >= 1}
     * @param s       should be {@code >= k}
     * @param alpha   must be {@code >= 0}
     * @param tnumber number of threads
     */
    public MacleodKNNClassifier(int k, int s, double alpha, int tnumber) {
        super(k, tnumber);
        this.setS(s);
        this.setAlpha(alpha);
    }

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing Macleod's
     * weighting function, with the given distance measure ({@code distance}) and
     * with the default number of nearest neighbors ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public MacleodKNNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing Macleod's
     * weighting function, with the specified distance measure ({@code distance}),
     * number of nearest neighbors ({@code k}), and default values of parameters
     * ({@code s = k, alpha = 1}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     */
    public MacleodKNNClassifier(Distance distance, int k) {
        super(distance, k);
    }

    /**
     * Constructs a new weighted kNN classifier utilizing Macleod's weighting
     * function, with the specified distance measure ({@code distance}), number of
     * nearest neighbors ({@code k}), default values of parameters
     * ({@code s = k, alpha = 1}), and number of threads ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     * @param tnumber  number of threads
     */
    public MacleodKNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, k, tnumber);
    }
    
    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing Macleod's
     * weighting function, with the specified distance measure ({@code distance}),
     * number of nearest neighbors ({@code k}), and parameters ({@code s, alpha}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     * @param s        should be {@code >= k}
     * @param alpha    must be {@code >= 0}
     */
    public MacleodKNNClassifier(Distance distance, int k, int s, double alpha) {
        super(distance, k);
        this.setS(s);
        this.setAlpha(alpha);
    }

    /**
     * Constructs a new weighted kNN classifier utilizing Macleod's weighting
     * function, with the specified distance measure ({@code distance}), number of
     * nearest neighbors ({@code k}), parameters ({@code s, alpha}), and number of
     * threads ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     * @param s        should be {@code >= k}
     * @param alpha    must be {@code >= 0}
     * @param tnumber  number of threads
     */
    public MacleodKNNClassifier(Distance distance, int k, int s, double alpha, int tnumber) {
        super(distance, k, tnumber);
        this.setS(s);
        this.setAlpha(alpha);
    }

    /**
     * Returns the value of parameter {@code s}.
     * 
     * @return the value of parameter {@code s}
     */
    public int getS() {
        return s;
    }

    /**
     * Sets the value of parameter {@code s}.
     * 
     * @param s the new value, it should not be greater than the number of nearest neighbors
     */
    public void setS(int s) {
        this.s = s;
    }

    /**
     * Returns the value of parameter {@code alpha}.
     * 
     * @return the value of parameter {@code alpha}
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Sets the value of parameter {@code alpha}. It must be {@code >= 0}.
     * 
     * @param alpha the new value, must be {@code >= 0}
     * 
     * @throws IllegalArgumentException if {@code alpha < 0}
     */
    public void setAlpha(double alpha) {
        if (alpha < 0)
            throw new IllegalArgumentException("alpha must be >= 0");
        this.alpha = alpha;
    }

    /**
     * Finds the best label among the nearest neighbors. Weights are calculated as
     * defined by Macleod.
     * 
     * @param list sorted list of the nearest neighbors
     * @return the best label
     */
    @Override
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> firstNode = list.getFirst();
        LinkedDistanceNode<TimeSeries> sNode = list.getLast();

        double sDist = sNode.distance;
        double diff = sDist - firstNode.distance;

        double bestLabel;
        int index = 1;
        int k = Math.min(this.getK(), list.getCount());

        if (diff == 0) {

            LinkedDistanceNode<TimeSeries> node = firstNode;

            Map<Double, Integer> neighbours = new HashMap<Double, Integer>();

            bestLabel = node.obj.getLabel();
            int bestWeight = 1;

            neighbours.put(bestLabel, bestWeight);
            node = node.next;

            while (node != null && index < k) {

                double label = node.obj.getLabel();
                int weight = 1;

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

        }

        else {

            LinkedDistanceNode<TimeSeries> node = firstNode;

            Map<Double, Double> neighbours = new HashMap<Double, Double>();

            double alpha = this.getAlpha();

            double alfaDiff = alpha * diff;
            diff *= (1 + alpha);

            bestLabel = firstNode.obj.getLabel();
            double bestWeight = (sDist - node.distance + alfaDiff) / diff;

            neighbours.put(bestLabel, bestWeight);
            node = node.next;

            while (node != null && index < k) {

                double label = node.obj.getLabel();
                double weight = (sDist - node.distance + alfaDiff) / diff;

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
        }

        return bestLabel;
    }

    @Override
    public void initialize(Dataset trainset) throws Exception {
        super.initialize(trainset);
        int max = this.getS();
        int k = this.getK();
        if (max < k)
            max = k;
        findKNeighbours(trainset, max);
    }

    /**
     * @throws EmptyDatasetException           if the training set is empty
     * @throws IncomparableTimeSeriesException if the series is incomparable with a
     *                                         series from the training set
     * @throws InterruptedException            if the thread has been interrupted
     */
    @Override
    public double classify(TimeSeries series) throws Exception {

        EmptyDatasetException.check(trainset);

        int max = this.getS();
        int k = this.getK();
        if (max < k)
            max = k;

        // sorted list of k nearest neighbors
        SortedList<TimeSeries> list;

        double label;

        // if the sorted list of nearest neighbors exists
        if (kNeighbours != null)
            list = kNeighbours.get(series.getIndex());

        // if the sorted list of nearest neighbors doesn't exist
        else {

            int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

            if (tnumber < 2) {
                list = new SortedList<TimeSeries>(max);
                for (DistanceNode<TimeSeries> node : findDistances(series, trainset))
                    list.add(node);
            }
            else
                list = findSortedDistancesMultithreaded(series, trainset, max, tnumber);

        }

        if (Thread.interrupted())
            throw new InterruptedException();
        
        int len = k - exclude;
        list.remove(exclude);

        if (len > 1)
            label = getBestLabel(list);
        else
            label = list.getFirst().obj.getLabel();

        return label;

    }

    /**
     * Sets the parameters of the specified classifier to be equal to the parameters
     * of this classifier.
     * 
     * @param copy the classifier whose parameters are to be set
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(MacleodKNNClassifier copy, boolean deep) {
        super.init(copy, deep);
        copy.setS(this.getS());
        copy.setAlpha(this.getAlpha());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        MacleodKNNClassifier copy = new MacleodKNNClassifier();
        init(copy, deep);
        return null;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", s=" + getS() + ", alpha=" + getAlpha();
    }

}

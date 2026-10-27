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
import fap.data.Dataset;
import fap.data.TimeSeries;
import fap.distance.Distance;
import fap.exception.EmptyDatasetException;
import fap.exception.IncomparableTimeSeriesException;
import fap.util.ThreadUtils;

/**
 * Majority-voting kNN classifier. The class of a time series {@code Q} is determined by a
 * majority vote of its k-nearest neighbors in the training set:
 *
 * <blockquote> <img src="doc-files/KNNClassifier-1.png"> </blockquote>
 * 
 * where E(∙) is the indicator function, and T<sub>i</sub><sup>c</sup> is the i-th nearest neighbor's class.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> J. Gou, L. Du, Y. Zhang, T. Xiong, A New distance-weighted k-nearest
 *       neighbor classifier, J. Inf. Comput. Sci. 9 (2012) 1429–1436.
 *  <li> T.-L. Pao, W.-Y. Liao, Y.-T. Che, A Weighted Discrete KNN Method for
 *       Mandarin Speech and Emotion Recognition, in: Speech Recognit., InTech, 2008.
 *       <a href="https://doi.org/10.5772/6370">
 *          https://doi.org/10.5772/6370</a>.
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
 * @version 2026.10.27.
 * @see AbstractNNClassifier
 */
public class KNNClassifier extends AbstractNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * The number of nearest neighbors to consider. Default value is {@code 10}.
     */
    protected int k = 10;
    
    /**
     * Constructs a new single-threaded, majority-voting kNN classifier with
     * the default number of nearest neighbors ({@link #k}) and without a
     * distance measure.
     */
    public KNNClassifier() {
    }

    /**
     * Constructs a new single-threaded, majority-voting kNN classifier with the
     * given number of nearest neighbors ({@code k}) and without a distance measure.
     *
     * @param k the number of nearest neighbors; must be at least {@code 1}
     */
    public KNNClassifier(int k) {
        this.setK(k);
    }
    
    /**
     * Constructs a new majority-voting kNN classifier with the specified number of
     * nearest neighbors ({@code k}) and number of threads ({@code tnumber}), and
     * without a distance measure.
     *
     * @param k       the number of nearest neighbors; must be at least {@code 1}
     * @param tnumber the number of threads
     */
    public KNNClassifier(int k, int tnumber) {
        super(tnumber);
        this.setK(k);
    }
    
    /**
     * Constructs a new single-threaded, majority-voting kNN classifier with the
     * specified distance measure ({@code distance}) and the default number of
     * nearest neighbors ({@link #k}).
     *
     * @param distance the distance measure
     */
    public KNNClassifier(Distance distance) {
        super(distance);
    }

    /**
     * Constructs a new single-threaded, majority-voting kNN classifier with the
     * specified distance measure ({@code distance}) and number of nearest
     * neighbors ({@code k}).
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors; must be at least {@code 1}
     */
    public KNNClassifier(Distance distance, int k) {
        super(distance);
        this.setK(k);
    }
    
    /**
     * Constructs a new majority-voting kNN classifier with the specified distance
     * measure ({@code distance}), number of nearest neighbors ({@code k}), and
     * number of threads ({@code tnumber}).
     * 
     * @param distance the distance measure
     * @param k        the number of nearest neighbors; must be at least {@code 1}
     * @param tnumber  the number of threads
     */
    public KNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, tnumber);
        this.setK(k);
    }

    /**
     * Returns the number of nearest neighbors ({@code k}) to consider.
     *
     * @return the number of nearest neighbors
     */
    public int getK() {
        return k;
    }

    /**
     * Sets the number of nearest neighbors ({@code k}) to consider.
     *
     * @param k the number of nearest neighbors
     * @throws IllegalArgumentException if {@code k < 1}
     */
    public void setK(int k) {
        if (k < 1)
            throw new IllegalArgumentException("Invalid k: " + k + " (must be >= 1)");
        this.k = k;
    }

    /**
     * Returns the best label among the sorted nearest neighbors in {@code list}
     * using unweighted voting.
     * 
     * @param list sorted list of the nearest neighbors
     * @return the best label
     */
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();

        Map<Double, Integer> neighbors = new HashMap<>();

        double bestLabel = node.obj.getLabel();
        int bestWeight = 1;

        neighbors.put(bestLabel, bestWeight);
        node = node.next;

        while (node != null) {

            double label = node.obj.getLabel();
            
            int weight = neighbors.merge(label, 1, Integer::sum);

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
     * {@code [1, k]}, using unweighted voting.
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
        
        double[] bestLabels = new double[k];
        int[] bestWeights = new int[k];

        double label = node.obj.getLabel();
        
        for (int i = 0; i < k; i++) {
            
            Map<Double, Integer> hmap = new HashMap<>();
            hmap.put(label, 1);
            neighbors.add(hmap);
            
            bestLabels[i] = label;
            bestWeights[i] = 1;
            
        }

        node = node.next;
        int index = 1;

        while (node != null) {

            label = node.obj.getLabel();

            for (int i = index; i < count; i++) {
                
                int weight = neighbors.get(i).merge(label, 1, Integer::sum);
               
                if (weight > bestWeights[i]) {
                    bestLabels[i] = label;
                    bestWeights[i] = weight;
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
    public void fit(Dataset trainset) throws Exception {
        super.fit(trainset);
        findKNeighbors(trainset, this.k);
    }

    /**
     * Returns the distances between the specified time series ({@code series}) and
     * the elements of the provided training set ({@code trainset}).
     * 
     * @param series   the time series against which the distances of the elements
     *                 of {@code trainset} should be found
     * @param trainset the training set
     * @return the distances between {@code series} and the elements of {@code trainset}
     * @throws InterruptedException if the current thread has been interrupted
     */
    protected double[] findDistances(TimeSeries series, Dataset trainset)
                                             throws InterruptedException {

        int len = trainset.size();
        
        double[] result = new double[len];

        // if the matrix of distances doesn't exists, we must use the distance measure
        if (distances == null)

            for (int i = 0; i < len; i++) {

                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                result[i] = distance.distance(series, trainset.get(i)); // might throw IncomparableTimeSeriesException

            }

        // if the matrix of distances exists, we use it instead of the similarity
        // computor
        else {

            int sindex = series.getIndex();

            for (int i = 0; i < len; i++) {

                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                int tindex = trainset.get(i).getIndex();

                if (tindex < distances[sindex].length)
                    result[i] = distances[sindex][tindex];
                else
                    result[i] = distances[tindex][sindex];

            }

        }
        
        return result;

    }
    
    /**
     * @throws EmptyDatasetException           if the training set is empty
     * @throws IncomparableTimeSeriesException if {@code series} is incompatible
     *                                         with a series in the training set
     * @throws InterruptedException            if the current thread has been
     *                                         interrupted
     */
    @Override
    public double classify(TimeSeries series) throws Exception {

        EmptyDatasetException.check(trainset);
        
        int k = this.getK();

        // sorted list of k nearest neighbors
        SortedList<TimeSeries> list;

        double label;

        // if the sorted list of nearest neighbors exists
        if (kNeighbors != null)
            list = kNeighbors.get(series.getIndex());
        
        // if the sorted list of nearest neighbors doesn't exist
        else {

            int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

            // if the number of threads is 1 or the matrix of distances exists
            if (tnumber < 2 || distances != null)
                list = findSortedDistances(series, trainset, k);
            else
                list = findSortedDistancesMultithreaded(series, trainset, k, tnumber);

        }

        if (list.getCount() > 1)
            label = getBestLabel(list);
        else
            label = list.getFirst().obj.getLabel();
        
        return label;
        
    }
    
    /**
     * Classifies the given time series for each neighbor count from {@code 1} to
     * {@code k}.
     *
     * @param series the time series to be classified
     * @return an array of predicted labels for {@code series}, where the element at
     *         index {@code i} corresponds to the classification result using
     *         {@code i + 1} nearest neighbors
     * @throws EmptyDatasetException           if the training set is empty
     * @throws IncomparableTimeSeriesException if {@code series} is incompatible
     *                                         with a series in the training set
     * @throws InterruptedException            if the current thread has been
     *                                         interrupted
     * @throws Exception                       if an error occurs during
     *                                         classification
     */
    public double[] classifyK(TimeSeries series) throws Exception {
        
        EmptyDatasetException.check(trainset);
        
        int k = this.getK();

        // sorted list of k nearest neighbors
        SortedList<TimeSeries> list;

        double[] labels;

        // if the sorted list of nearest neighbors exists
        if (kNeighbors != null)
            list = kNeighbors.get(series.getIndex());
        
        // if the sorted list of nearest neighbors doesn't exist
        else {

            int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

            // if the number of threads is 1 or the matrix of distances exists
            if (tnumber < 2 || distances != null)
                list = findSortedDistances(series, trainset, k);
            else
                list = findSortedDistancesMultithreaded(series, trainset, k, tnumber);

        }
        
        if (list.getCount() > 1)
            labels = getBestLabels(list);
        else {
            labels = new double[1];
            labels[0] = list.getFirst().obj.getLabel();
        }
        
        return labels;
        
    }

    /**
     * Returns the sorted list of up to {@code k} nearest neighbors (and their
     * distances) for the specified time series ({@code series}) from the training
     * set ({@code trainset}).
     * 
     * @param series   the time series to be classified
     * @param trainset the training set
     * @param k        the number of nearest neighbors
     * @return the sorted list of up to {@code k} nearest neighbors (and their
     *         distances) for the specified time series ({@code series}) from the
     *         training set ({@code trainset})
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs
     */
    protected SortedList<TimeSeries> findSortedDistances(TimeSeries series, Dataset trainset, int k) throws Exception {
        
        double[] dists = findDistances(series, trainset);
        
        SortedList<TimeSeries> list = new SortedList<>(k);
        
        int len = trainset.size();
        
        if (k > 1)
            for (int i = 0; i < len; i++)
                list.add(trainset.get(i), dists[i]);
        
        else {
            int bestIndex = 0;
            double bestDist = dists[0];
            for (int i = 0; i < len; i++) {
                double dist = dists[i];
                if (dist < bestDist) {
                    bestIndex = i;
                    bestDist = dist;
                }
            }
            list.add(trainset.get(bestIndex), dists[bestIndex]);
        }
            
        return list;
            
    }
    
    /**
     * Returns the sorted list of up to {@code k} nearest neighbors (and their
     * distances) for the specified time series ({@code series}) from the training
     * set ({@code trainset}) using {@code tnumber} threads.
     * 
     * @param series   the time series to be classified
     * @param trainset the training set
     * @param k        the number of nearest neighbors
     * @param tnumber  the number of threads
     * @return the sorted list of up to {@code k} nearest neighbors (and their
     *         distances) for the specified time series ({@code series}) from the
     *         training set ({@code trainset})
     * @throws InterruptedException if the current thread has been interrupted
     * @throws Exception            if an error occurs
     */
    protected SortedList<TimeSeries> findSortedDistancesMultithreaded(TimeSeries series, Dataset trainset, int k, int tnumber) throws Exception {

        double[] distances = this.findDistances(series, trainset, tnumber);

        SortedList<TimeSeries> list = new SortedList<>(k);
        
        if (k > 1)
            for (int i = 0; i < distances.length; i++)
                list.add(trainset.get(i), distances[i]);
        
        else {
            int index = getMinIndex(distances);
            list.add(trainset.get(index), distances[index]);
        }

        return list;
        
    }

    /**
     * Sets the number of neighbors of the specified classifier {@code copy} to
     * match that of this classifier.
     * 
     * @param copy the classifier whose number of nearest neighbors is to be set
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(KNNClassifier copy, boolean deep) {
        super.init(copy, deep);
        copy.setK(this.getK());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        KNNClassifier copy = new KNNClassifier();
        init(copy, deep);
        return copy;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", k=" + getK();
    }

}

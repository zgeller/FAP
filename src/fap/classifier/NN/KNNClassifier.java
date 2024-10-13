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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
 * Majority-voting kNN classifier. The class of a time series {@code Q} is determined by a
 * majority vote of its k-nearest neighbours in the training set:
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
 * @version 2024.10.01.
 * @see AbstractNNClassifier
 */
public class KNNClassifier extends AbstractNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * The number of nearest neighbours to consider. Default value is {@code 10}.
     */
    protected int k = 10;
    
    /**
     * Indicates how many of the nearest neighbors should be excluded from
     * consideration. Must be {@code >= 0} and {@code < k}.
     */
    protected int exclude = 0;

    /**
     * Constructs a new single-threaded majority-voting kNN classifier, with the
     * default number of nearest neighbours ({@link #k}) and without a distance
     * measuer.
     */
    public KNNClassifier() {
    }

    /**
     * Constructs a new single-threaded majority-voting kNN classifier utilizing,
     * with the given number of nearest neighbours ({@code k}).
     * 
     * @param k number of nearest neighbours, must be {@code >= 1}
     */
    public KNNClassifier(int k) {
        this.setK(k);
    }
    
    /**
     * Constructs a new majority-voting kNN classifier, with the specified number of
     * nearest neighbours ({@code k}), and number of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbours, must be {@code >= 1}
     * @param tnumber number of threads
     */
    public KNNClassifier(int k, int tnumber) {
        super(tnumber);
        this.setK(k);
    }
    
    /**
     * Constructs a new single-threaded majority-voting kNN classifier, with the
     * specified distance measure ({@code distance}), and the default number of
     * nearest neighbours ({@link #k}).
     * 
     * @param distance distance measure
     */
    public KNNClassifier(Distance distance) {
        super(distance);
    }

    /**
     * Constructs a new single-threaded majority-voting kNN classifier, with the
     * specified distance measure ({@code distance}) and number of nearest
     * neighbours ({@code k}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     */
    public KNNClassifier(Distance distance, int k) {
        super(distance);
        this.setK(k);
    }
    
    /**
     * Constructs a new majority-voting kNN classifier, with the specified distance
     * measure ({@code distance}), number of nearest neighbours ({@code k}), and
     * number of threads ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     * @param tnumber  number of threads
     */
    public KNNClassifier(Distance distance, int k, int tnumber) {
        super(distance, tnumber);
        this.setK(k);
    }

    /**
     * Returns the number of nearest neighbors to consider {@code k}.
     * 
     * @return the number of nearest neighbors to consider {@code k}
     */
    public int getK() {
        return k;
    }

    /**
     * Sets the number of nearest neighbors to consider {@code k}. Must be
     * {@code >= 1}.
     * 
     * @param k number of nearest neighbors, must be {@code >= 1}
     * @throws IllegalArgumentException if {@code k < 1}
     */
    public void setK(int k) {
        if (k < 1)
            throw new IllegalArgumentException("k must be > 0.");
        if (k <= exclude)
            exclude = 0;
        this.k = k;
    }

    /**
     * Returns the number of nearest neighbors that are excluded from consideration.
     * 
     * @return the number of nearest neighbors that are excluded from consideration
     */
    public int getExclude() {
        return exclude;
    }

    /**
     * Sets the number of nearest neighbors to be excluded from consideration. Must
     * be {@code >= 0} and {@code < k}.
     * 
     * @param exclude the number of nearest neighbors to be excluded from
     *                consideration, must be {@code >= 0} and {@code < k}
     * @throws IllegalArgumentException if {@code exclude < 0} or
     *                                  {@code exclude >= k}
     */
    public void setExclude(int exclude) throws IllegalArgumentException {
        if (exclude < 0)
            throw new IllegalArgumentException("Exclude must be >= 0.");
        if (exclude >= k)
            throw new IllegalArgumentException("Exclude must be < k.");
        this.exclude = exclude;
    }
    
    /**
     * Finds the best label among the nearest neighbors without weighting (with
     * voting).
     * 
     * @param list sorted list of the nearest neighbours
     * @return the best label
     */
    protected double getBestLabel(SortedList<TimeSeries> list) {

        LinkedDistanceNode<TimeSeries> node = list.getFirst();

        HashMap<Double, Integer> neighbours = new HashMap<Double, Integer>();

        double bestLabel = node.obj.getLabel();
        int bestWeight = 1;

        neighbours.put(bestLabel, bestWeight);
        node = node.next;

        while (node != null) {

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
        }

        return bestLabel;
    }

    @Override
    public void initialize(Dataset dataset) throws Exception {
        super.initialize(dataset);
        findKNeighbours(dataset, this.k);
    }

    /**
     * Finds the distances between the specified time series ({@code series}) and
     * the elements of the provided training set ({@code trainset}).
     * 
     * @param series   the time series against which the distances of the elements
     *                 of {@code trainset} should be found
     * @param trainset the training set
     * @return list of {@link DistanceNode} objects containing the elements of
     *         {@code trainset} and their distances from {@code series}
     * @throws InterruptedException if the thread has been interrupted
     */
    protected ArrayList<DistanceNode<TimeSeries>> findDistances(TimeSeries series, 
                                                                Dataset trainset) 
                                                  throws InterruptedException {

        ArrayList<DistanceNode<TimeSeries>> list = new ArrayList<>(trainset.size());

        // if the matrix of distances doesn't exists, we must use the distance measure
        if (distances == null)

            for (TimeSeries ts : trainset) {

                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                double dist = distance.distance(series, ts); // might throw IncomparableTimeSeriesException

                list.add(new DistanceNode<TimeSeries>(ts, dist));

            }

        // if the matrix of distances exists, we use it instead of the similarity
        // computor
        else {

            int sindex = series.getIndex();

            for (TimeSeries ts : trainset) {

                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                double dist;

                int tindex = ts.getIndex();

                if (tindex < distances[sindex].length)
                    dist = distances[sindex][tindex];
                else
                    dist = distances[tindex][sindex];

                list.add(new DistanceNode<TimeSeries>(ts, dist));

            }

        }
        
        return list;

    }
    
    
    /**
     * @throws EmptyDatasetException           if the training set is empty
     * @throws IncomparableTimeSeriesException if the series is incomparable with a
     *                                         series from the training set
     * @throws InterruptedException            when the interrupted flag is set
     */
    @Override
    public double classify(TimeSeries series) throws Exception {

        EmptyDatasetException.check(trainset);
        
        int k = this.getK();

        // sorted list of k nearest neighbours
        SortedList<TimeSeries> list;

        double label;

        // if the sorted list of nearest neighbours exists
        if (kNeighbours != null)
            list = kNeighbours.get(series.getIndex());
        
        // if the sorted list of nearest neighbours doesn't exist
        else {

            int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

            // if the number of threads is 1 or the matrix of distances exists
            if (tnumber < 2 || distances != null) {
                list = new SortedList<TimeSeries>(k);
                for (DistanceNode<TimeSeries> node : findDistances(series, trainset))
                    list.add(node);
            }
            else
                list = findSortedDistancesMultithreaded(series, trainset, k, tnumber);

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
     * Finds the soerted list of {@code k} nearest neighbours (and their distances)
     * of the specified time series ({@code series}) in the training set
     * ({@link AbstractNNClassifier#trainset trainset}) relying on {@code tnumber}
     * of threads.
     * 
     * @param series  the time series to be classified
     * @param trainset the training set
     * @param k       number of nearest neighbours
     * @param tnumber number of threads
     * @return the sorted list of {@code k} nearest neighbours (and their distances)
     *         of the specified time series ({@code series}) in the training set
     *         ({@code trainset})
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    protected SortedList<TimeSeries> findSortedDistancesMultithreaded(TimeSeries series, Dataset trainset, int k, int tnumber) throws Exception {

        List<Double> results = this.findDistances(series, trainset, tnumber);

        SortedList<TimeSeries> list = new SortedList<>(k);

        for (int i = 0; i < results.size(); i++)
            list.add(new DistanceNode<TimeSeries>(trainset.get(i), results.get(i)));

        return list;
    }

    /**
     * Sets the number of neighbours of the specified classifier to be equal to the
     * number of neighbours of this classifier.
     * 
     * @param copy the classifier whose number of nearest neighbours is to be set
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

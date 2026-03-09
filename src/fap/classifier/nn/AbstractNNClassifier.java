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
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import fap.classifier.AbstractDistanceBasedClassifier;
import fap.classifier.nn.util.SortedList;
import fap.data.Dataset;
import fap.data.TimeSeries;
import fap.distance.AbstractDistance;
import fap.distance.Distance;
import fap.util.Copyable;
import fap.util.Multithreaded;
import fap.util.ThreadUtils;

/**
 * Defines common methods and fields for (multithreaded) NN classifiers.
 * 
 * @author Zoltán Gellér
 * @version 2025.04.17.
 * @see AbstractDistanceBasedClassifier
 * @see Multithreaded
 * @see Copyable
 */
public abstract class AbstractNNClassifier extends AbstractDistanceBasedClassifier implements Multithreaded, Copyable {

    private static final long serialVersionUID = 1L;

    /**
     * The training dataset.
     */
    protected transient Dataset trainset;

    /**
     * The neighborhood matrix (sorted by the distance in ascending order). The
     * elements of the matrix are the indices of the time series' neighbors:
     * 
     * <ul>
     * <li>it must apply to the entire dataset, not just the training set
     * <li>{@code neighbours[n][m]} is the index of the m-th nearest neighbor of
     * the time series whose index is n.
     * </ul>
     */
    protected transient int[][] neighbors;

    /**
     * The matrix of distances between time series.
     * 
     * <ul>
     * <li>it must apply to the entire dataset, not just the training set
     * <li>{@code distances[n][m]} is the distance between the time series whose
     * indices are n and m
     * </ul>
     */
    protected transient double[][] distances;

    /**
     * The list of sorted lists of k nearest neighbors (and their distances):
     * 
     * <ul>
     * <li>{@code kNeighbours[n]} is a (sorted) list of the k nearest neighbors
     * (and their distances) of the time series whose index is n.
     * </ul>
     * <p>
     */
    protected transient List<SortedList<TimeSeries>> kNeighbours;

    /**
     * The number of threads. Default value is {@code 1}.
     */
    protected int numberOfThreads = 1;

    /**
     * The executor service used for implementing multithreaded classification.
     */
    protected transient ForkJoinPool executor;
    
    /**
     * Indicates whether the common pool should be used for parallelization. Default
     * value is {@code false}.
     */
    protected boolean useCommonPool = false;

    /**
     * Empty constructor.
     */
    public AbstractNNClassifier() {
    }
    
    /**
     * Constructor with the number of threads.
     * 
     * @param tnumber number of threads
     */
    public AbstractNNClassifier(int tnumber) {
        this.setNumberOfThreads(tnumber);
    }
    
    /**
     * Constructor with a distance measure ({@code distance}).
     * 
     * @param distance distance measure
     */
    public AbstractNNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructor with a distance measure ({@code distance}), and number of threads
     * ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param tnumber  number of threads
     */
    public AbstractNNClassifier(Distance distance, int tnumber) {
        super(distance);
        this.setNumberOfThreads(tnumber);
    }
    
    /**
     * Indicates whether the common pool should be used for parallelization.
     * 
     * @param useCommonPool indicates whether the common pool should be used for
     *                      parallelization
     */
    public void setUseCommonPool(boolean useCommonPool) {
        this.useCommonPool = useCommonPool;
    }
    
    /**
     * Returns {@code true} if the common pool should be used for parallelization.
     * 
     * @return {@code true} if the common pool should be used for parallelization
     */
    public boolean isUseCommonPool() {
        return useCommonPool;
    }
    
    /**
     * Populates {@link #kNeighbours kNeighbours[index]} with up to {@code k}
     * nearest neighbors of the time series with the given {@code index} among the
     * given {@code list} of time series relying on the {@link #neighbors} and
     * {@link #distances} matrices.
     * 
     * <ul>
     * <li>{@code list} should contain as many elements as there are time series in
     * the entire dataset (not just the training set). If a time series with index
     * {@code i} is to be excluded from consideration, {@code list[i]} should be set
     * to {@code null}.
     * </ul>
     * 
     * @param index index of the time series whose list of {@code k} nearest
     *              neighbors is to be found
     * @param list  an array of time series among which the {@code k} nearest
     *              neighbors of the time series with the given {@code index}
     *              should be found
     * @param k     the number of nearest neighbors to be found
     */
    private void findKNeighbours(int index, TimeSeries[] list, int k) {

        /*
         * For each member of the neighbors[index] array with index nIndex, the
         * algorithm checks whether list[nIndex] contains the time series or is null. If
         * it contains a time series, it is added to kNeighbors[index] (along with its
         * distance obtained from the distances matrix).
         */

        kNeighbours.set(index, new SortedList<TimeSeries>(k));

        int nIndex = 0;

        int len = neighbors[index].length;

        while (k > 0 && nIndex < len) {

            int neighbor = neighbors[index][nIndex];

            if (list[neighbor] != null) {

                k--;
                nIndex++;

                double distance;
                if (neighbor < distances[index].length)
                    distance = distances[index][neighbor];
                else
                    distance = distances[neighbor][index];

                // appending is possible because the elements of neighbors[index] are sorted by
                // distance in ascending order
                // i.e. Neighbors[index][nIndex] is at a smaller distance than
                // Neighbors[index][nIndex + i], i>0
                kNeighbours.get(index).add(list[neighbor], distance);

            } else
                nIndex++;
        }

    }

    /**
     * Populates {@link #kNeighbours} with up to {@code k} nearest neighbors of
     * every time series in the whole dataset among the training set
     * ({@code trainset}) relying on the {@link #neighbors} and {@link #distances}
     * matrices.
     * 
     * <p>
     * It should be invoked only within the
     * {@link fap.classifier.Classifier#fit(Dataset) fit} method.
     * 
     * @param trainset the training dataset
     * @param k        number of neighbors
     */
    protected void findKNeighbours(Dataset trainset, int k) {

        if (neighbors != null && distances != null) {

            int nlen = neighbors.length;
            int dsSize = trainset.size();

            TimeSeries[] list = new TimeSeries[nlen];
            for (TimeSeries ts : trainset)
                list[ts.getIndex()] = ts;

            if (k > dsSize)
                k = dsSize;

            kNeighbours = new ArrayList<SortedList<TimeSeries>>(nlen);
            for (int index = 0; index < nlen; index++) {
                kNeighbours.add(null);
                if (list[index] == null)
                    findKNeighbours(index, list, k);
            }
        }

    }

    @Override
    public void fit(Dataset trainset) throws Exception {
        this.trainset = trainset;
        /*
         * Method findKNeighbours cannot be called here because the number of nearest
         * neighbors is not known. In the case of the 1NN classifier, that number is 1,
         * and in the case of the kNN classifiers, it is determined by the parameter k.
         */
    }

    /**
     * Sets the matrix of neighbors. Must be invoked before the {@link #fit}
     * method.
     * 
     * @param neighbours the matrix of neighbors to set
     */
    public void setNeighbours(int[][] neighbours) {
        this.neighbors = neighbours;
    }

    /**
     * Returns the matrix of neighbors.
     * 
     * @return the matrix of neighbors
     */
    public int[][] getNeighbours() {
        return this.neighbors;
    }

    /**
     * Sets the distance matrix. Must be invoked before the {@link #fit}
     * method.
     * 
     * @param distances the distance matrix to set
     */
    public void setDistances(double[][] distances) {
        this.distances = distances;
    }

    /**
     * Returns the distance matrix.
     * 
     * @return the distance matrix
     */
    public double[][] getDistances() {
        return this.distances;
    }

    /**
     * Initializes the executor service.
     * 
     * @param tnumber the number of threads
     * @see ThreadUtils#init
     */
    protected void initExecutor(int tnumber) {
        executor = ThreadUtils.init(executor, tnumber);
    }

    @Override
    public void setNumberOfThreads(int tnumber) {
        this.numberOfThreads = tnumber;
        initExecutor(tnumber);
    }

    @Override
    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }
    
    /**
     * Calculates the distances between {@code series} and the elements of
     * {@code dataset} and adds them to the {@code distances} list.
     * 
     * @param distances the list to which the calculated distances should be added
     * @param series    the time series whose distances are to be calculated
     * @param dataset   the time series whose distances from {@code series} are to
     *                  be calculated
     * @param tnumber   number of threads
     * @throws Exception if an error occurs
     */
    private void addDistances(List<Double> distances, TimeSeries series, Dataset dataset, int tnumber) throws Exception {
        
        try {

            if (useCommonPool) {
                for (Double d: dataset.parallelStream().mapToDouble(ts -> distance.distance(series, ts)).toArray())
                    distances.add(d);
            }
            else {
                this.initExecutor(tnumber);
                for (Double d: executor.submit(() -> dataset.parallelStream().mapToDouble(ts -> distance.distance(series, ts)).toArray()).get())
                    distances.add(d);
            }
            
        } catch (ExecutionException | CancellationException | InterruptedException e) {
            
            ThreadUtils.shutdown(executor);
            throw e;
            
        }
        
    }
    
    
    /**
     * Finds the distances between the specified time series ({@code series}) and
     * the elements of the given training set ({@code trainset}) relying on
     * {@code tnumber} of threads.
     * 
     * @param series  the time series whose distances are to be calculated
     * @param dataset the time series whose distances from {@code series} are to be
     *                calculated
     * @param tnumber number of threads
     * @return the list of distances
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    protected List<Double> findDistances(TimeSeries series, Dataset dataset, int tnumber) throws Exception {

        final int tsize = dataset.size(); 
        
        List<Double> distances = new ArrayList<>(tsize);
        
        // if the trainset contains only one time series
        if (dataset.size() == 1)
            distances.add(distance.distance(series, dataset.get(0)));
        
        // if the trainset contains more than one time series
        else {

            // if storing distances is enabled, we need to calculate only the not yet calculated distances
            if (distance instanceof AbstractDistance dist && dist.isStoring()) {

                // time series whose distances must be calculated
                Dataset newDataset = new Dataset();

                // indices of time series whose distance must be calculated
                List<Integer> indices = new ArrayList<>();

                // finding time series whose distance has not yet been calculated
                for (int i = 0; i < tsize; i++) {

                    TimeSeries ts = dataset.get(i);

                    Double recall = dist.recall(series, ts);

                    distances.add(recall);

                    if (recall == null) {
                        indices.add(i);
                        newDataset.add(ts);
                    }

                }

                final int dsize = newDataset.size();

                // if we have only one distance to calculate
                if (dsize == 1)
                    distances.set(indices.get(0), distance.distance(series, newDataset.get(0)));

                // if we have more than one distance to calculate
                else if (dsize > 1) {

                    List<Double> newDistances = new ArrayList<>(dsize);
                    
                    addDistances(newDistances, series, newDataset, tnumber);

                    // merging distances
                    for (int i = 0; i < dsize; i++)
                        distances.set(indices.get(i), newDistances.get(i));

                }

            }

            // if storing distances is not enabled, we need to calculate all the distances
            else 
                addDistances(distances, series, dataset, tnumber);


        }

        return distances;
    }

    /**
     * Initializes the specified classifier with the common data structures of this
     * classifier.
     * 
     * @param copy the classifier whose data structures is to be initialized
     * @param deep indicates whether a deep copy should be made
     * @throws ClassCastException if the distance measure does not implement the
     *                            {@link Copyable} interface
     */
    protected void init(AbstractNNClassifier copy, boolean deep) throws ClassCastException {

        super.init(copy, deep);
        
        copy.setUseCommonPool(useCommonPool);

        if (!deep) {
            copy.setDistances(this.getDistances());            
            copy.setNeighbours(this.getNeighbours());
        }
        
    }
    
    @Override
    public void shutdown() {
        if (executor != null)
            executor.shutdown();
    }
    
}

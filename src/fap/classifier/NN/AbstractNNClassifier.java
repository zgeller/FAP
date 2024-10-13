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
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import fap.classifier.NN.util.SortedList;
import fap.core.classifier.AbstractDistanceBasedClassifier;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.distance.AbstractDistance;
import fap.core.distance.Distance;
import fap.util.Copyable;
import fap.util.Multithreaded;
import fap.util.ThreadUtils;

/**
 * Defines common methods and fields for (multithreaded) NN classifiers.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.22.
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
     * elements of the matrix are the indices of the time series' neighbours:
     * 
     * <ul>
     * <li>it must apply to the entire dataset, not just the training set
     * <li>{@code neighbours[n][m]} is the index of the m-th nearest neighbour of
     * the time series whose index is n.
     * </ul>
     */
    protected transient int[][] neighbours;

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
     * <li>{@code kNeighbours[n]} is a (sorted) list of the k nearest neighbours
     * (and their distances) of the time series whose index is n.
     * </ul>
     * <p>
     */
    protected transient ArrayList<SortedList<TimeSeries>> kNeighbours;

    /**
     * The number of threads. Default value is {@code 1}.
     */
    protected int numberOfThreads = 1;

    /**
     * The executor service used for implementing multithreaded classification.
     */
    protected ThreadPoolExecutor executor;

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
     * Constructor with a distnace measure ({@code distance}).
     * 
     * @param distance distance measure
     */
    public AbstractNNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructor with a distnace measure ({@code distance}), and number of threads
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
     * Populates {@link #kNeighbours kNeighbours[index]} with up to {@code k}
     * nearest neighbours of the time series with the given {@code index} among the
     * given {@code list} of time series relying on the {@link #neighbours} and
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
     *              neighbours is to be found
     * @param list  an array of time series among which the {@code k} nearest
     *              neighbours of the time series with the given {@code index}
     *              should be found
     * @param k     the number of nearest neighbours to be found
     */
    private void findKNeighbours(int index, TimeSeries[] list, int k) {

        /*
         * For each member of the neighbours[index] array with index nIndex, the
         * algorithm checks whether list[nIndex] contains the time series or is null. If
         * it contains a time series, it is added to kNeighbors[index] (along with its
         * distance obtained from the distances matrix).
         */

        kNeighbours.set(index, new SortedList<TimeSeries>(k));

        int nIndex = 0;

        int len = neighbours[index].length;

        while (k > 0 && nIndex < len) {

            int neighbour = neighbours[index][nIndex];

            if (list[neighbour] != null) {

                k--;
                nIndex++;

                double distance;
                if (neighbour < distances[index].length)
                    distance = distances[index][neighbour];
                else
                    distance = distances[neighbour][index];

                // appending is possible because the elements of neighbours[index] are sorted by
                // distance in ascending order
                // i.e. neighbours[index][nIndex] is at a smaller distance than
                // neighbours[index][nIndex + i], i>0
                kNeighbours.get(index).add(list[neighbour], distance);

            } else
                nIndex++;
        }

    }

    /**
     * Populates {@link #kNeighbours} with up to {@code k} nearest neighbours of
     * every time series in the whole dataset among the training set
     * ({@code trainset}) relying on the {@link #neighbours} and {@link #distances}
     * matrices.
     * 
     * <p>
     * It should be invoked only within the
     * {@link fap.core.classifier.Classifier#initialize(Dataset) initialize} method.
     * 
     * @param trainset the training dataset
     * @param k        number of neighbours
     */
    protected void findKNeighbours(Dataset trainset, int k) {

        if (neighbours != null && distances != null) {

            int nlen = neighbours.length;
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
    public void initialize(Dataset trainset) throws Exception {
        this.trainset = trainset;
        /*
         * Method findKNeighbours cannot be called here because the number of nearest
         * neighbors is not known. In the case of the 1NN classifier, that number is 1,
         * and in the case of the kNN classifiers, it is determined by the parameter k.
         */
    }

    /**
     * Sets the matrix of neighbours. Must be invoked before the {@link #initialize}
     * method.
     * 
     * @param neighbours the matrix of neighbours to set
     */
    public void setNeighbours(int[][] neighbours) {
        this.neighbours = neighbours;
    }

    /**
     * Returns the matrix of neighbours.
     * 
     * @return the matrix of neighbours
     */
    public int[][] getNeighbours() {
        return this.neighbours;
    }

    /**
     * Sets the distance matrix. Must be invoked before the {@link #initialize}
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
     * A {@link Runnable} class that calculates the distances between a given time series and the elements of the specified dataset.
     */
    protected class DistanceTask implements Runnable {
        
        static TimeSeries series;
        
        private Dataset dataset;
        
        private List<Double> distances;

        public DistanceTask(Dataset dataset) {
            this.dataset = dataset;
            this.distances = new ArrayList<>(dataset.size());
        }

        @Override
        public void run() {
            
            for (TimeSeries ts: dataset) {
                
                if (Thread.currentThread().isInterrupted())
                    throw new RuntimeException(new InterruptedException());
                
                distances.add(distance.distance(series, ts));
            }

        }

    }
    
    /**
     * Finds the distances between the specified time series ({@code series}) and
     * the elements of the given training set ({@code trainset}) relying on
     * {@code tnumber} of threads.
     * 
     * @param series   the time series to be classified
     * @param trainset the training set
     * @param tnumber  number of threads
     * @return the list of distances
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    protected List<Double> findDistances(TimeSeries series, Dataset trainset, int tnumber) throws Exception {

        final int tsize = trainset.size(); 
        
        List<Double> distances = new ArrayList<>(tsize);
        
        // if the trainset contains only one time series
        if (trainset.size() == 1)
            distances.add(distance.distance(series, trainset.get(0)));
        
        // if the trainset contains more than one time series
        else {

            this.initExecutor(tnumber);
            
            // if storing distances is enabled, we need to calculate only the not yet calculated distances
            if (distance instanceof AbstractDistance dist && dist.isStoring()) {

                // time series whose distances must be calculated
                Dataset dataset = new Dataset();

                // indices of time series whose distance must be calculated
                List<Integer> indices = new ArrayList<>();

                // finding time series whose dinstance has not yet been calculated
                for (int i = 0; i < tsize; i++) {

                    TimeSeries ts = trainset.get(i);

                    double recall = dist.recall(series, ts);

                    distances.add(recall);

                    if (Double.isNaN(recall)) {
                        indices.add(i);
                        dataset.add(ts);
                    }

                }

                final int dsize = dataset.size();

                // if we have only one distance to calculate
                if (dsize == 1)
                    distances.set(indices.get(0), distance.distance(series, dataset.get(0)));

                // if we have more than one distance to calculate
                else if (dsize > 1) {

                    DistanceTask.series = series;

                    this.initExecutor(tnumber);

                    // if there are more threads than time series
                    if (tnumber > dsize)
                        tnumber = dsize;
                    
                    // splitting the dataset
                    List<Dataset> list = dataset.split(tnumber, false);

                    // creating tasks
                    List<DistanceTask> tasks = new ArrayList<>();
                    for (Dataset ds : list)
                        tasks.add(new DistanceTask(ds));

                    // calucalting distances
                    ThreadUtils.startRunnables(executor, tasks);

                    // merging results
                    List<Double> results = new ArrayList<>(dsize);
                    for (DistanceTask task : tasks)
                        results.addAll(task.distances);

                    // merging distances
                    for (int i = 0; i < dsize; i++)
                        distances.set(indices.get(i), results.get(i));

                }

            }

            // if storing distances is not enabled, we need to calculate all the distances
            else {

                DistanceTask.series = series;

                this.initExecutor(tnumber);
                
                // if there are more threads than time series
                if (tnumber > tsize)
                    tnumber = tsize;

                // splitting the dataset
                List<Dataset> list = trainset.split(tnumber, false);

                // creating tasks
                List<DistanceTask> tasks = new ArrayList<>();
                for (Dataset ds : list)
                    tasks.add(new DistanceTask(ds));

                // calucalting distances
                ThreadUtils.startRunnables(executor, tasks);

                // updating distances
                for (DistanceTask task : tasks)
                    distances.addAll(task.distances);

            }

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

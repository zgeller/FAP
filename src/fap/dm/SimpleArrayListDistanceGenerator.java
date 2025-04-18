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

package fap.dm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fap.callback.Callback;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.distance.CallableDistance;
import fap.core.distance.Distance;
import fap.util.ThreadUtils;

/**
 * Generates distance matrices in form of {@code ArrayList<Double>} objects.
 * Fields {@code first} and {@code last} represent the first and the last
 * element of the matrix to be calculated.
 * 
 * @author Zoltán Gellér
 * @version 2025.03.05.
 * @see AbstractDistanceGenerator
 */
public class SimpleArrayListDistanceGenerator extends AbstractDistanceGenerator<ArrayList<Double>> {

    private static final long serialVersionUID = 1L;

    /**
     * Distance list. Default value is {@code null}.
     */
    private ArrayList<Double> distList = null;

    private int index = 0;
    private double stepSize = 0;
    private double progress = 0;
    private int steps = 1;

    /**
     * Creates a new {@code SimpleArrayListDistanceGenerator} object with default
     * parameter values.
     */
    public SimpleArrayListDistanceGenerator() {
    }

    /**
     * Creates a new {@code SimpleArrayListDistanceGenerator} object with the given
     * parameter values.
     * 
     * @param dataset     the dataset
     * @param first       position of the first element (indexing starts from {@code 0})
     * @param last        position of the last element (indexing starts from {@code 0})
     * @param symmetrical true for diagonal matrix
     * @param distance    the distance measure
     * @param callback    callback object
     */
    public SimpleArrayListDistanceGenerator(Dataset dataset, 
                                            int first, 
                                            int last, 
                                            boolean symmetrical, 
                                            Distance distance,
                                            Callback callback) {
        this.setDataSet(dataset);
        this.setFirst(first);
        this.setLast(last);
        this.setSymmetrical(symmetrical);
        this.setDistance(distance);
        this.setCallback(callback);
    }

    /**
     * Creates a new {@code SimpleArrayListDistanceGenerator} object for generating
     * triangular distance matrices with the given parameter values.
     * 
     * @param dataset  the dataset
     * @param first    position of the first element (indexing starts from {@code 0})
     * @param last     position of the last element (indexing starts from {@code 0})
     * @param distance the distance measure
     * @param callback callback object
     */
    public SimpleArrayListDistanceGenerator(Dataset dataset, 
                                            int first, 
                                            int last, 
                                            Distance distance,
                                            Callback callback) {
        this(dataset, first, last, true, distance, callback);
    }

    /**
     * Creates a new {@code SimpleArrayListDistanceGenerator} object with the given
     * parameter values and {@code first=0}, {@code last=-1}.
     * 
     * @param dataset     the dataset
     * @param distance    the distance measure
     * @param symmetrical true for diagonal matrix
     * @param callback    callback object
     */
    public SimpleArrayListDistanceGenerator(Dataset dataset, 
                                            Distance distance, 
                                            boolean symmetrical,
                                            Callback callback) {
        this(dataset, 0, -1, symmetrical, distance, callback);
    }

    /**
     * Creates a new {@code SimpleArrayListDistanceGenerator} object for generating
     * triangular distance matrices with the given parameter values and
     * {@code first=0}, {@code last=-1}.
     * 
     * @param dataset  the dataset
     * @param distance the distance measure
     * @param callback callback object
     */
    public SimpleArrayListDistanceGenerator(Dataset dataset, 
                                            Distance distance, 
                                            Callback callback) {
        this(dataset, distance, true, callback);
    }

    /**
     * @return the distance matrix in form of {@code ArrayList<Double>}
     */
    @Override
    public ArrayList<Double> getDistanceObject() {
        return this.distList;
    }

    /***
     * @exception InterruptedException when the interrupted flag is set
     */
    @Override
    public void compute() throws Exception {

        if (dataset.isEmpty() || done) {
            insideLoop = false;
            done = true;
            return;
        }

        int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

        if (tnumber < 2)
            computeSingleThreaded();
        else
            computeMultiThreaded(tnumber);

    }

    /**
     * Method used for setting default fields values.
     */
    private void init() {
        index = first;
        distList = new ArrayList<Double>();
        progress = -1;
        steps = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
    }

    /**
     * Single-threaded implementation.
     * 
     * @throws Exception if an error occurs
     * @throws InterruptedException when the interrupted flag is set
     */
    private void computeSingleThreaded() throws Exception {

        TimeSeries a, b;

        int dsize = dataset.size();

        if (last < 0)
            last = (symmetrical ? dsize * (dsize + 1) / 2 : dsize * dsize) - 1;

        // computing number of cells
        int cnt = last - first + 1;

        // initializing callback
        boolean callbackNotNull = callback != null;
        if (callbackNotNull) {
            stepSize = (double) callback.getDesiredCallbackNumber() / cnt;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(cnt + 1);
                stepSize = 1;
            }
        }

        // generator initialization
        if (!insideLoop) {
            init();
            distList.ensureCapacity(cnt + 1);
        } else if (callbackNotNull)
            callback.setCallbackCount(steps);

        // finding start posiztion
        int i, j;
        if (symmetrical) {
            i = 0;
            while (i * (i + 1) <= 2 * index)
                i++;
            i--;
            j = index - i * (i + 1) / 2;
        } else {
            i = index / dsize;
            j = index % dsize;
        }

        // generating
        while (index <= last) {

            a = dataset.get(i);

            int endj = symmetrical ? i + 1 : dsize;

            while (index <= last && j < endj) {

                if (Thread.interrupted())
                    throw new InterruptedException();

                b = dataset.get(j);
                distList.add(distance.distance(a, b));

                j++;
                index++;

                // calling back
                if (callbackNotNull) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        callback.callback(this);
                    }
                }

            }

            j = 0;
            i++;

        }

        // finalizing
        insideLoop = false;
        done = true;
        if (callbackNotNull)
            callback.callback(this);

    }

    /**
     * Multithreaded implementation.
     * 
     * @throws Exception            if an error occurs
     * @throws InterruptedException when the interrupted flag is set
     */
    private void computeMultiThreaded(int tnumber) throws Exception {

        TimeSeries a, b;

        int dsize = dataset.size();

        if (last < 0)
            last = (symmetrical ? dsize * (dsize + 1) / 2 : dsize * dsize) - 1;

        // computing number of cells
        int cnt = last - first + 1;

        // initializing callback
        boolean callbackNotNull = callback != null;
        if (callbackNotNull) {
            stepSize = (double) callback.getDesiredCallbackNumber() / cnt;
            if (stepSize >= 1 || stepSize == 0) {
                callback.setPossibleCallbackNumber(cnt + 1);
                stepSize = 1;
            }
        }

        // generator initialization
        if (!insideLoop) {
            init();
            distList.ensureCapacity(cnt + 1);
        } else if (callbackNotNull)
            callback.setCallbackCount(steps);

        // Executor initialization
        CallableDistance[] tasks = new CallableDistance[tnumber];
        int[] tasks_index = new int[tnumber];
        for (int i = 0; i < tnumber; i++) {
            tasks[i] = new CallableDistance();
            tasks[i].setDistance(distance);
        }
        ExecutorService executor = Executors.newFixedThreadPool(tnumber);
        int counter = 0;

        // finding start posiztion
        int i, j;
        if (symmetrical) {
            i = 0;
            while (i * (i + 1) <= 2 * index)
                i++;
            i--;
            j = index - i * (i + 1) / 2;
        } else {
            i = index / dsize;
            j = index % dsize;
        }

        // generating
        while (index <= last) {

            a = dataset.get(i);

            int endj = symmetrical ? i + 1 : dsize;

            while (index <= last && j < endj) {

                if (Thread.interrupted())
                    throw new InterruptedException();

                b = dataset.get(j);

                tasks[counter].setTimeSeries1(a);
                tasks[counter].setTimeSeries2(b);

                int lastIndex = index;

                tasks_index[counter] = index;

                counter++;
                if (counter == tnumber) {

                    List<Double> results;
                    try {
                        results = ThreadUtils.startCallables(executor, tasks, counter);
                    } catch (InterruptedException e) {
                        index = tasks_index[0];
                        throw e;
                    }

                    for (int k = 0; k < counter; k++) {

                        distList.add(results.get(k));

                        // calling back
                        if (callbackNotNull) {
                            progress += stepSize;
                            if (progress >= steps) {
                                steps++;
                                index = tasks_index[k] + 1;
                                callback.callback(this);
                            }
                        }

                    }

                    counter = 0;

                }

                j++;
                index = lastIndex + 1;

            }

            j = 0;
            i++;

        }

        // finalizing
        if (counter > 0) {

            List<Double> results;
            try {
                results = ThreadUtils.startCallables(executor, tasks, counter);
            } catch (InterruptedException e) {
                index = tasks_index[0];
                throw e;
            }

            for (int k = 0; k < counter; k++) {

                distList.add(results.get(k));

                // calling back
                if (callbackNotNull) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        index = tasks_index[k] + 1;
                        callback.callback(this);
                    }
                }

            }

        }

        ThreadUtils.shutdown(executor);

        insideLoop = false;
        done = true;
        if (callbackNotNull)
            callback.callback(this);

    }

}

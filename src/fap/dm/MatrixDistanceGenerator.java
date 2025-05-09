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
 * Generates distance matrices in form of {@code Double [][]} objects.
 * 
 * @author Zoltán Gellér
 * @version 2025.03.06.
 * @see AbstractDistanceGenerator
 */
public class MatrixDistanceGenerator extends AbstractDistanceGenerator<Double[][]> {

    private static final long serialVersionUID = 1L;

    /**
     * Distance matrix. Default value is {@code null}.
     */
    private Double distMatrix[][] = null;

    private int starti = 0;
    private int startj = 0;
    private double stepSize = 0;
    private double progress = 0;
    private int steps = 1;

    /**
     * Creates a new {@code MatrixDistanceGenerator} object with default parameter
     * values.
     */
    public MatrixDistanceGenerator() {
    }

    /**
     * Creates a new {@code MatrixDistanceGenerator} object with the given parameter
     * values.
     * 
     * @param dataset     the dataset
     * @param first       first line (line indexing starts from {@code 0})
     * @param last        last line (line indexing starts from {@code 0})
     * @param symmetrical true for diagonal matrix
     * @param distance    the distance measure
     * @param callback    callback object
     */
    public MatrixDistanceGenerator(Dataset dataset, 
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
     * Creates a new {@code MatrixDistanceGenerator} object for generating
     * triangular distance matrices with the given parameter values.
     * 
     * @param dataset  the dataset
     * @param first    first line (line indexing starts from {@code 0})
     * @param last     last line (line indexing starts from {@code 0})
     * @param distance the distance measure
     * @param callback callback object
     */
    public MatrixDistanceGenerator(Dataset dataset, 
                                   int first, 
                                   int last, 
                                   Distance distance, 
                                   Callback callback) {
        this(dataset, first, last, true, distance, callback);
    }

    /**
     * Creates a new {@code MatrixDistanceGenerator} object with the given parameter
     * values and {@code first=0}, {@code last=-1}.
     * 
     * @param dataset     the dataset
     * @param distance    the distance measure
     * @param symmetrical true for diagonal matrix
     * @param callback    callback object
     */
    public MatrixDistanceGenerator(Dataset dataset, 
                                   Distance distance, 
                                   boolean symmetrical, 
                                   Callback callback) {
        this(dataset, 0, -1, symmetrical, distance, callback);
    }

    /**
     * Creates a new {@code MatrixDistanceGenerator} object for generating
     * triangular distance matrices with the given parameter values and
     * {@code first=0}, {@code last=-1}.
     * 
     * @param dataset  the dataset
     * @param distance the distance measure
     * @param callback callback object
     */
    public MatrixDistanceGenerator(Dataset dataset, 
                                   Distance distance, 
                                   Callback callback) {
        this(dataset, distance, true, callback);
    }

    /**
     * @return the distance matrix in form of {@code Double[][]}
     */
    @Override
    public Double[][] getDistanceObject() {
        return this.distMatrix;
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
        starti = first;
        startj = 0;
        distMatrix = new Double[dataset.size()][];
        progress = -1;
        steps = 0;
        if (callback != null)
            callback.setCallbackCount(0);
        insideLoop = true;
    }

    /***
     * Single threaded implementation.
     * 
     * @throws Exception if an error occurs
     * @throws InterruptedException when the interrupted flag is set
     */
    private void computeSingleThreaded() throws Exception {

        TimeSeries a, b;

        int dsize = dataset.size();

        if (last < 0)
            last = dsize - 1;

        // computing number of cells
        int cnt;
        if (symmetrical) {
            int max = (last + 1) * (last + 2) / 2;
            int min = first * (first + 1) / 2;
            cnt = max - min;
        } else
            cnt = (last - first + 1) * dsize;

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
        if (!insideLoop)
            init();
        else if (callbackNotNull)
            callback.setCallbackCount(steps);

        // generating
        for (int i = starti; i <= last; i++) {

            a = dataset.get(i);

            int endj = symmetrical ? i + 1 : dsize;
            if (distMatrix[i] == null) {
                distMatrix[i] = new Double[endj];
                startj = 0;
            }

            for (int j = startj; j < endj; j++) {

                if (Thread.interrupted())
                    throw new InterruptedException();

                b = dataset.get(j);
                distMatrix[i][j] = distance.distance(a, b);

                startj = j + 1;

                // calling back
                if (callbackNotNull) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        callback.callback(this);
                    }
                }
            }

            starti = i + 1;

            if (i <= last)
                startj = 0;

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
     * @throws Exception if an error occurs
     * @throws InterruptedException when the interrupted flag is set
     */
    private void computeMultiThreaded(int tnumber) throws Exception {

        TimeSeries a, b;

        int dsize = dataset.size();

        if (last < 0)
            last = dsize - 1;

        // computing number of cells
        int cnt;
        if (symmetrical) {
            int max = (last + 1) * (last + 2) / 2;
            int min = first * (first + 1) / 2;
            cnt = max - min;
        } else
            cnt = (last - first + 1) * dsize;

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
        if (!insideLoop)
            init();
        else if (callbackNotNull)
            callback.setCallbackCount(steps);

        // Executor initialization
        CallableDistance[] tasks = new CallableDistance[tnumber];
        int[] tasks_i = new int[tnumber];
        int[] tasks_j = new int[tnumber];
        for (int i = 0; i < tnumber; i++) {
            tasks[i] = new CallableDistance();
            tasks[i].setDistance(distance);
        }
        ExecutorService executor = Executors.newFixedThreadPool(tnumber);
        int counter = 0;

        // generating
        for (int i = starti; i <= last; i++) {

            a = dataset.get(i);

            int endj = symmetrical ? i + 1 : dsize;

            if (distMatrix[i] == null) {
                distMatrix[i] = new Double[endj];
                startj = 0;
            }

            for (int j = startj; j < endj; j++) {

                if (Thread.interrupted()) {
                    starti = i;
                    startj = j;
                    throw new InterruptedException();
                }

                b = dataset.get(j);

                tasks[counter].setTimeSeries1(a);
                tasks[counter].setTimeSeries2(b);

                tasks_i[counter] = i;
                tasks_j[counter] = j;

                counter++;
                if (counter == tnumber) {

                    List<Double> results;
                    try {
                        results = ThreadUtils.startCallables(executor, tasks, counter);
                    } catch (InterruptedException e) {
                        starti = tasks_i[0];
                        startj = tasks_j[0];
                        throw e;
                    }

                    for (int k = 0; k < counter; k++) {

                        int ti = tasks_i[k];
                        int tj = tasks_j[k];

                        distMatrix[ti][tj] = results.get(k);

                        // calling back
                        if (callbackNotNull) {
                            progress += stepSize;
                            if (progress >= steps) {
                                steps++;
                                starti = ti;
                                startj = tj + 1;
                                callback.callback(this);
                            }
                        }

                    }

                    counter = 0;

                }

            }

            if (i <= last)
                startj = 0;

        }

        // finalizing
        if (counter > 0) {

            List<Double> results;
            try {
                results = ThreadUtils.startCallables(executor, tasks, counter);
            } catch (InterruptedException e) {
                starti = tasks_i[0];
                startj = tasks_j[0];
                throw e;
            }

            for (int k = 0; k < counter; k++) {

                int ti = tasks_i[k];
                int tj = tasks_j[k];

                distMatrix[ti][tj] = results.get(k);

                // calling back
                if (callbackNotNull) {
                    progress += stepSize;
                    if (progress >= steps) {
                        steps++;
                        starti = ti;
                        startj = tj + 1;
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

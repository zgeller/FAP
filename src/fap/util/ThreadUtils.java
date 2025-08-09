/*   
 * Copyright 2024-2025 Zoltán Gellér
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

package fap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread utilities.
 * 
 * @author Zoltán Gellér
 * @version 2025.03.05.
 */
public final class ThreadUtils {

    private ThreadUtils() {
    }
    
    
    /**
     * The number of processors available to the Java virtual machine
     * ({@link Runtime#availableProcessors()
     * Runtime.getRuntime().availableProcessors()}).
     */
    private static int processorCount = Runtime.getRuntime().availableProcessors();

    /**
     * The allowed number of threads. Default value is the number of processors
     * available to the Java virtual machine ({@link #availableProcessors}).
     */
    private static int globalThreadLimit = processorCount;

    /**
     * Updates the current number of processors available to the Java virtual machine.
     */
    public static void updateProcessorCount() {
        processorCount = Runtime.getRuntime().availableProcessors();
    }
    
    /**
     * Returns the current number of processors available to the Java virtual machine.
     * 
     * @return the current number of processors available to the Java virtual machine
     */
    public static int getProcessorCount() {
        return processorCount;
    }
    
    /**
     * Sets the allowed number of threads.
     * 
     * <p>
     * Zero denotes the total number of processors available to the Java virtual
     * machine.
     * 
     * <p>
     * A negative value indicates how much to reduce the number of processors
     * available to the Java virtual machine. This option is useful in a situation
     * where we don't know the number of available processors, but we don't want to
     * overload all of them. For example, if {@code tnumber = -2} and the number of
     * available processors is 8, the allowed number of threads will be 6.
     * 
     * @param tnumber the allowed number of threads
     */
    public static void setGlobalThreadLimit(int tnumber) {

        int pnumber = Runtime.getRuntime().availableProcessors();

        if (tnumber <= 0) {
            globalThreadLimit = pnumber + tnumber;
            if (globalThreadLimit < 1)
                globalThreadLimit = 1;
        } else
            globalThreadLimit = tnumber;

    }

    /**
     * Returns the allowed number of threads.
     * 
     * @return the allowed number of threads
     */
    public static int getGlobalThreadLimit() {
        return globalThreadLimit;
    }

    /**
     * Limits the given number of threads according to the number of processors
     * available to the Java virtual machine ({@link #getProcessorCount()}) and the
     * global thread limit ({@link #getGlobalThreadLimit()}).
     *
     * <p>
     * Zero denotes the total number of processors available to the Java virtual
     * machine.
     * 
     * <p>
     * A negative value indicates how much to reduce number of processors available
     * to the Java virtual machine. This option is useful in a situation where we
     * don't know the number of available processors, but we don't want to overload
     * all of them. For example, if {@code tnumber = -2} and the number of available
     * processors is 8, the number of threads will be limited to 6 (i.e.
     * {@code tnumber = 6}.
     * 
     * @param tnumber the number of threads to limit
     * @return the allowed number of threads
     * 
     * @see setGlobalThreadLimit
     * @see getGlobalThreadLimit
     */
    public static int getThreadLimit(int tnumber) {

        int limit;

        if (tnumber <= 0) {
            limit = getProcessorCount() + tnumber;
            if (limit < 1)
                limit = 1;
        } else
            limit = tnumber;

        int globalThreadLimit = getGlobalThreadLimit();

        return limit > globalThreadLimit ? globalThreadLimit : limit;
        
    }

    /**
     * If the specified {@code executor} is {@code null} or it is shut down, creates
     * a new fixed thread pool executor service with {@code tnumber} threads.
     * 
     * <p>
     * If the specified {@code executor} is not {@code null} and it is not shut
     * down, sets its number of threads to {@code tnumber}.
     * 
     * <p>
     * In both cases, {@code tnumber} will be limited by
     * {@link ThreadUtils#getThreadLimit(int)}.
     * 
     * @param executor the executor service whose number of threads is to be set
     * @param tnumber  number of threads
     * @return the executor service
     */
    public static ThreadPoolExecutor init(ThreadPoolExecutor executor, int tnumber) {
        
        tnumber = getThreadLimit(tnumber);

        if (executor == null || executor.isShutdown())
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(tnumber);
        else
            executor.setCorePoolSize(tnumber);

        return executor;
        
    }
    
    /**
     * Creates a new work stealing pool with {@code tnumber} threads if any of the
     * following conditions are met:
     * <ul>
     *  <li> {@code executor} is {@code null}
     *  <li> {@code executor} is shut down
     *  <li> the parallelism level of {@code executor} is not equal to {@code tnumber}
     * (limited by {@link ThreadUtils#getThreadLimit(int)}).
     * </ul>
     *
     * <p>
     * The parallelism level of the new thread pool will be limited by
     * {@code ThreadUtils#getThreadLimit(int)}.
     * 
     * @param executor the {@link ForkJoinPool} whose level of parallelism is to be
     *                 set
     * @param tnumber  number of threads
     * @return the executor service
     */
    public static ForkJoinPool init(ForkJoinPool executor, int tnumber) {

        tnumber = getThreadLimit(tnumber);

        int parallelism = -1;
        if (executor instanceof ForkJoinPool pool)
            parallelism = pool.getParallelism();
        
        if (executor == null || executor.isShutdown() || parallelism != tnumber) {
            if (executor != null)
                executor.shutdown();
            executor = (ForkJoinPool) Executors.newWorkStealingPool(tnumber);
        }
        
        return executor;
    }

    /**
     * Shuts down the specified object if it implements the {@link Multithreaded}
     * interface.
     * 
     * @param obj the object to be shut down
     */
    public static void shutdown(Object obj) {
        if (obj instanceof Multithreaded mobj)
            mobj.shutdown();
    }
    
    
    /**
     * Shuts down the given executor service. It will wait one (1) second for the
     * executor service to complete execution.
     * 
     * @param executor the executor service to shut down
     */
    public static void shutdown(ExecutorService executor) {
        shutdown(executor, 1, TimeUnit.SECONDS);
    }

    /**
     * Shuts down the given executor service. It will wait {@code timeout}
     * {@code unit}(s) for the executor service to complete execution.
     * 
     * @param executor the executor service to shut down
     * @param timeout  the maximum time to wait
     * @param unit     the time unit
     */
    public static void shutdown(ExecutorService executor, long timeout, TimeUnit unit) {
        
        if (executor == null)
            return;
        
        executor.shutdown();
        
        try {
            
            if (!executor.awaitTermination(timeout, unit))
                executor.shutdownNow();
            
        } catch (InterruptedException e) {
            
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            
        }
        
    }

    /**
     * Executes the specified callables using the given executor service and returns
     * the list of results.
     * 
     * @param <T>       the type of the results
     * @param executor  the executor service
     * @param callables array of callables
     * @return the list of the results
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    public static <T> List<T> startCallables(ExecutorService executor, Callable<T>[] callables) throws Exception {
        return startCallables(executor, callables, 0);
    }

    /**
     * Executes the first {@code n} callables using the given executor service and
     * returns the list of results.
     * 
     * <dl>
     *  <dt> if {@code n <= 0},
     *   <dd> it will execute the first {@code Math.max(callables.length + n, 0)} callables
     *  <dt> if {@code n > 0},
     *   <dd> it will execute the first {@code Math.max(callables.lenght, n)} callables
     * </dl>
     * 
     * @param <T>       the type of the results
     * @param executor  the executor service
     * @param callables array of callables
     * @param n         number of callables to execute
     * @return the list of the results
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    public static <T> List<T> startCallables(ExecutorService executor, Callable<T>[] callables, int n) throws Exception {

        int len = callables.length;
        n = n <= 0 ? Math.max(len + n, 0) : Math.max(len, n);

        List<Future<T>> futures = new ArrayList<>(n);

        for (int i = 0; i < n; i++)
            futures.add(executor.submit(new CallableWrapper<T>(callables[i])));

        return waitForResults(executor, futures);

    }
    
    /**
     * Executes the specified callables using the given executor service and returns
     * the list of results.
     * 
     * @param <T>       the type of the results
     * @param executor  the executor service
     * @param callables list of callables
     * @return the list of the results
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    public static <T> List<T> startCallables(ExecutorService executor, List<? extends Callable<T>> callables) throws Exception {
        return startCallables(executor, callables, 0);
    }
    
    /**
     * Executes the first {@code n} callables using the given executor service and
     * returns the list of results.
     * 
     * <dl>
     *  <dt> if {@code n <= 0},
     *   <dd> it will execute the first {@code Math.max(callables.size() + n, 0)} callables
     *  <dt> if {@code n > 0},
     *   <dd> it will execute the first {@code Math.max(callables.size(), n)} callables
     * </dl>
     * 
     * @param <T>       the type of the results
     * @param executor  the executor service
     * @param callables list of callables
     * @param n         number of callables to execute
     * @return the list of the results
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    public static <T> List<T> startCallables(ExecutorService executor, List<? extends Callable<T>> callables, int n) throws Exception {

        int len = callables.size();
        n = n <= 0 ? Math.max(len + n, 0) : Math.max(len, n);
        
        List<Future<T>> futures = new ArrayList<>(n);

        for (int i = 0; i < n; i++)
            futures.add(executor.submit(new CallableWrapper<T>(callables.get(i))));
        
        return waitForResults(executor, futures);

    }
    
    /**
     * Executes the specified runnables using the given executor service.
     * 
     * @param executor  the executor service
     * @param runnables array of runnables to be executed
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    public static void startRunnables(ExecutorService executor, Runnable[] runnables) throws Exception {
        startRunnables(executor, runnables, 0);
    }
    

    /**
     * Executes the first {@code n} runnables using the given executor service.
     * 
     * <dl>
     *  <dt> if {@code n <= 0},
     *   <dd> it will execute the first {@code Math.max(callables.length + n, 0)} runnables
     *  <dt> if {@code n > 0},
     *   <dd> it will execute the first {@code Math.max(callables.lenght, n)} runnables
     * </dl>
     * 
     * @param executor  the executor service
     * @param callables array of runnables
     * @param n         number of runnables to execute
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    public static void startRunnables(ExecutorService executor, Runnable[] runnables, int n) throws Exception {

        int len = runnables.length;
        n = n <= 0 ? Math.max(len + n, 0) : Math.max(len, n);

        List<Future<?>> futures = new ArrayList<>(n);

        for (int i = 0; i < n; i++)
            futures.add(executor.submit(new RunnableWrapper(runnables[i])));

        waitForFutures(executor, futures);

    }

    /**
     * Executes the specified runnables using the given executor service.
     * 
     * @param executor  the executor service
     * @param runnables array of runnables
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    public static void startRunnables(ExecutorService executor, List<? extends Runnable> runnables) throws Exception {
        startRunnables(executor, runnables, 0);
    }
    
    /**
     * Executes the first {@code n} runnables using the given executor service.
     * 
     * <dl>
     *  <dt> if {@code n <= 0},
     *   <dd> it will execute the first {@code Math.max(callables.size() + n, 0)} callables
     *  <dt> if {@code n > 0},
     *   <dd> it will execute the first {@code Math.max(callables.size(), n)} callables
     * </dl>
     * 
     * @param executor  the executor service
     * @param runnables list of runnables
     * @param n         number of runnables to execute
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    public static void startRunnables(ExecutorService executor, List<? extends Runnable> runnables, int n) throws Exception {

        int len = runnables.size();
        n = n <= 0 ? Math.max(len + n, 0) : Math.max(len, n);
        
        List<Future<?>> futures = new ArrayList<>(n);

        for (int i = 0; i < n; i++)
            futures.add(executor.submit(new RunnableWrapper(runnables.get(i))));

        waitForFutures(executor, futures);
        
    }

    /**
     * Waits until all futures in the list return a result.
     * 
     * @param executor the executor service
     * @param futures  list of futures whose results should be awaited
     * @throws Exception            if an error occurs
     */
    public static void waitForFutures(ExecutorService executor, Collection<Future<?>> futures) throws Exception {

        if (futures == null)
            return;
        
        try {
            
            for (Future<?> future : futures)
                future.get();
            
        } catch (ExecutionException | CancellationException | InterruptedException e) {
            
            shutdown(executor);
            throw e;
            
        }
        
    }

    /**
     * Waits until all futures in the list return a result.
     * 
     * @param executor the executor service
     * @param futures  list of futures whose results should be awaited
     * @return the list of the results
     * @throws Exception if an error occurs
     */
    public static <T> List<T> waitForResults(ExecutorService executor, Collection<Future<T>> futures) throws Exception {

        if (futures == null)
            return Collections.<T>emptyList();
        
        List<T> results = new ArrayList<>(futures.size());
        
        try {
            
            for (Future<T> future : futures)
                results.add(future.get());
            
        } catch (ExecutionException | CancellationException | InterruptedException e) {
            
            shutdown(executor);
            throw e;
            
        }
        
        return results;
        
    }
    
    
    /**
     * A wrapper for {@link Callable} tasks that will not execute the task in case
     * if any of them generate an exception.
     * 
     * @param <T>
     */
    public static class CallableWrapper<T> implements Callable<T> {

        private static volatile boolean exception;
        
        private Callable<T> task;

        /**
         * Constructs a new {@code RunnableWrapper} with the specified {@code task}.
         * 
         * @param task the task to be executed
         */
        public CallableWrapper(Callable<T> task) {
            this.task = task;
        }
        
        
        @Override
        public T call() throws Exception {
            if (exception)
                return null;
            else 
                try {
                    return task.call();
                }
                catch (Exception e) {
                    exception = true;
                    throw e;
                }
        }
        
    }
    
    /**
     * A wrapper for {@link Runnable} tasks that will not execute the task in case
     * if any of them generate an exception.
     */
    public static class RunnableWrapper implements Runnable {

        private static volatile boolean exception;
        
        private Runnable task;

        /**
         * Constructs a new {@code RunnableWrapper} with the specified {@code task}.
         * 
         * @param task the task to be executed
         */
        public RunnableWrapper(Runnable task) {
            this.task = task;
        }
        
        @Override
        public void run() {
            if (!exception)
                try {
                    task.run();
                }
                catch (Exception e) {
                    exception = true;
                    throw e;
                }
        }
        
    }
    
}

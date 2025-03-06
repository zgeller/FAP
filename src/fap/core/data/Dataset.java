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

package fap.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 * A time-series dataset implemented in the form of an {@link ArrayList} or a
 * {@link LinkedList} of {@link TimeSeries time series}. Which list type will be
 * used to store the time series is determined at instantiation.
 * 
 * @author Zoltán Gellér
 * @version 2025.03.06.
 * @see TimeSeries
 * @see List
 * @see Serializable
 */
public class Dataset implements List<TimeSeries>, Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * The list that stores the time series of this dataset.
     */
    private List<TimeSeries> list;

    /**
     * Constructs a new dataset utilizing an {@code ArrayList} for storing time
     * series.
     */
    public Dataset() {
        this(false);
    }

    /**
     * Constructs a new dataset utilizing an {@code ArrayList} for storing time
     * series with the specified initial capacity.
     * 
     * @param initialCapacity the initial capacity of the underlying
     *                        {@code ArrayList}
     */
    public Dataset(int initialCapacity) {
        list = new ArrayList<>(initialCapacity);
    }

    /**
     * Constructs a new dataset containing the elements of the specified collection.
     * Time series are stored in an {@code ArrayList}.
     *
     * @param c the collection whose elements are to be placed into this dataset
     * @throws NullPointerException if the specified collection is null
     */
    public Dataset(Collection<? extends TimeSeries> c) throws NullPointerException {
        this(false, c);
    }

    /**
     * Constructs a new dataset containing the elements of the specified list of
     * datasets. Time series are stored in an {@code ArrayList}.
     * 
     * @param list the list of datasets whose elements are to be placed into this
     *             dataset
     */
    public Dataset(List<Dataset> list) {
        this(false, list);
    }

    /**
     * Constructs a new dataset containing the elements of the specified list of
     * datasets except the dataset with the given index. Time series are stored in
     * an {@code ArrayList}.
     * 
     * @param list   the list of datasets whose elements are to be placed into this
     *               dataset
     * @param except the index of the dataset whose elements should not be added to
     *               this dataset
     * @throws NullPointerException if the specified list is null
     */
    public Dataset(List<Dataset> list, int except) throws NullPointerException {
        this(false, list, except);
    }

    /**
     * Constructs a new dataset utilizing an {@code ArrayList} or a
     * {@code LinkedList} for storing time series, depending on the
     * {@code linkedList} parameter.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the time series
     */
    public Dataset(boolean linkedList) {
        if (linkedList)
            list = new LinkedList<>();
        else
            list = new ArrayList<>();
    }

    /**
     * Constructs a new dataset containing the elements of the specified collection.
     * Time series are stored in an {@code ArrayList} or {@code LinkedList} or a
     * {@code LinkedList}, depending on the {@code linkedList} parameter.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the time series
     * @param c          the collection whose elements are to be placed into this
     *                   dataset
     * @throws NullPointerException if the specified collection is null
     */
    public Dataset(boolean linkedList, Collection<? extends TimeSeries> c) throws NullPointerException {
        if (linkedList)
            list = new LinkedList<>(c);
        else
            list = new ArrayList<>(c);

    }

    /**
     * Constructs a new dataset containing the elements of the specified list of
     * datasets. Time series are stored in an {@code ArrayList} or
     * {@code LinkedList} or a {@code LinkedList}, depending on the
     * {@code linkedList} parameter.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the time series
     * @param list       the list of datasets whose elements are to be placed into
     *                   this dataset
     * @throws NullPointerException if the specified list is null
     */
    public Dataset(boolean linkedList, List<Dataset> list) throws NullPointerException {

        this(linkedList);

        for (Dataset dataset : list)
            this.addAll(dataset);

    }

    /**
     * Constructs a new dataset containing the elements of the specified list of
     * datasets except the dataset with the given index. Time series are stored in
     * an {@code ArrayList} or a {@code LinkedList}, depending on the
     * {@code linkedList} parameter.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the time series
     * @param list       the list of datasets whose elements are to be placed into
     *                   this dataset
     * @param except     the index of the dataset whose elements should not be added
     *                   to this dataset
     * @throws NullPointerException if the specified list is null
     */
    public Dataset(boolean linkedList, List<Dataset> list, int except) throws NullPointerException {

        this(linkedList);

        // before
        for (int i = 0; i < except; i++)
            this.addAll(list.get(i));

        // after
        for (int i = except + 1; i < list.size(); i++)
            this.addAll(list.get(i));

    }

    /**
     * Return a new datasets of the same type as this dataset.
     * 
     * @return a new datasets of the same type as this dataset
     */
    private Dataset getDataset(int initialCapacity) {
        Dataset dataset;
        if (list instanceof ArrayList<?>) {
            if (initialCapacity < 0)
                dataset = new Dataset();
            else
                dataset = new Dataset(initialCapacity);
        }
        else
            dataset = new Dataset(true);
        return dataset;
    }
    
    /**
     * Finds how many time series of this dataset are labeled with the given label
     * of the specified list and returns the result as a list. Formally, the ith
     * element of the resulting list denotes the number of time series of this
     * dataset with the ith label of the {@code labels} list.
     * 
     * @param labels list of labels
     * @return the distribution of the time series of this dataset according to the
     *         given list of labels
     */
    public List<Integer> getDistribution(List<Double> labels) {

        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < labels.size(); i++)
            list.add(0);

        for (TimeSeries ts : this) {

            int index = labels.indexOf(ts.getLabel());
            int elem = list.get(index);
            elem++;
            list.set(index, elem);

        }

        return list;

    }

    /**
     * Finds how many time series of this dataset are labeled with the distinct
     * labels found in the dataset.
     * 
     * @return the distribution of the time series of this dataset according to the
     *         distinct labels in this dataset
     */
    public List<Integer> getDistribution() {
        return getDistribution(getDistinctLabels());
    }

    /**
     * Returns the list of distinct time series labels found in this dataset.
     * 
     * @return the list of distinct time series labels found in this dataset
     */
    public List<Double> getDistinctLabels() {

        List<Double> list = new ArrayList<Double>();

        for (TimeSeries ts : this) {

            double label = ts.getLabel();

            if (list.indexOf(label) == -1)
                list.add(label);

        }

        return list;

    }

    /**
     * Returns the array of time series labels of this dataset in the order of the
     * time seires in this dataset.
     * 
     * @return the array of time series labels of this dataset in the order of the
     *         time seires in this dataset
     */
    public double[] getLabels() {

        int len = this.size();

        double[] labels = new double[len];

        for (int i = 0; i < len; i++)
            labels[i] = this.get(i).getLabel();

        return labels;

    }

    /**
     * Divides this dataset into subsets so that every element of a given subset
     * belong to the same class (have the same label).
     * 
     * @return list of subsets of this dataset such that every element of a given
     *         subset belong to the same class (have the same label)
     */
    public List<Dataset> getSubsetsByLabels() {
        return getSubsetsByLabels(null);
    }

    /**
     * Divides this dataset into subsets so that every element of a given subset
     * belong to the same class (have the same label).
     * 
     * <p>
     * If the given random number generator is not {@code null}, it will shuffle
     * each subset.
     * 
     * @param rnd a random number generator
     * @return list of subsets of this dataset such that every element of a given
     *         subset belong to the same class (have the same label)
     */
    public List<Dataset> getSubsetsByLabels(Random rnd) {

        List<Double> labels = this.getDistinctLabels();

        List<Dataset> list = new ArrayList<>(labels.size());

        for (int i = 0; i < labels.size(); i++)
            list.add(getDataset(-1));

        for (TimeSeries ts : this) {
            int index = labels.indexOf(ts.getLabel());
            Dataset dataset = list.get(index);
            dataset.add(ts);
        }

        if (rnd != null)
            for (Dataset ds : list)
                Collections.shuffle(ds, rnd);

        return list;

    }

    /**
     * Returns a new dataset in which the time series of this dataset are arranged
     * by their labels.
     * 
     * @return a new dataset in which the time series of this dataset are arranged
     *         by their labels
     */
    public Dataset getArrangedByLabels() {
        return getArrangedByLabels(null);
    }

    /**
     * Returns a new dataset in which the time series of this dataset are arranged
     * by their labels. If the given random number generator is not {@code null}, it
     * will shuffle each group of time series.
     * 
     * @param rnd a random number generator
     * @return a new dataset in which the time series of this dataset are arranged
     *         by their labels
     */
    public Dataset getArrangedByLabels(Random rnd) {

        Dataset arrangedDataset = getDataset(-1);

        List<Dataset> list = getSubsetsByLabels(rnd);

        for (Dataset ds : list)
            arrangedDataset.addAll(ds);

        return arrangedDataset;

    }
    
    /**
     * Splits this dataset into {@code k} stratified subsets of approximately equal
     * sizes.
     * 
     * @param k the number of subsets, must be in the range {@code [2..size()]}
     * @return list of {@code k} stratified subsets of this dataset
     * @throws IllegalArgumentException if {@code k < 2} or {@code k > size()}
     */
    public List<Dataset> split(int k) throws IllegalArgumentException {
        return this.split(k, null, true);
    }
    
    /**
     * Splits this dataset into {@code k} subsets of approximately equal sizes.
     * 
     * @param k          the number of subsets, must be in the range
     *                   {@code [2..size()]}
     * @param stratified indicates whether subsets should be stratified
     * @return list of {@code k} subsets of this dataset
     * @throws IllegalArgumentException if {@code k < 2} or {@code k > size()}
     */
    public List<Dataset> split(int k, boolean stratified) throws IllegalArgumentException {
        return this.split(k, null, stratified);
    }
    
    /**
     * Splits this dataset into {@code k} stratified subsets of approximately equal
     * sizes. If the given random number generator is not {@code null}, it will
     * shuffle the groups of time series which belong to the same class before
     * splitting the dataset.
     * 
     * @param k   the number of subsets, must be in the range {@code [2..size()]}
     * @param rnd a random number generator
     * @return list of {@code k} stratified subsets of this dataset
     * @throws IllegalArgumentException if {@code k < 2} or {@code k > size()}
     */
    public List<Dataset> split(int k, Random rnd) throws IllegalArgumentException {
        return this.split(k, rnd, true);
    }
    
    /**
     * Splits this dataset into {@code k} subsets of approximately equal sizes. 
     * 
     * <p>
     * If the given random number generator is not {@code null}:
     * 
     * <dl>
     * 
     *   <dt> {@code stratified = true}:
     *   <dd> each group of time series which belong to the same class is shuffled
     *        before splitting the dataset
     *        
     *   <dt> {@code stratified = false}:
     *   <dd> the elements of the subsets are chosen randomly from the dataset (rather
     *        than in the order that they are present in the dataset)
     *        
     * </dl>
     * 
     * @param k          the number of subsets, must be in the range
     *                   {@code [2..size()]}
     * @param rnd        a random number generator
     * @param stratified indicates whether subsets should be stratified
     * @return list of {@code k} subsets of this dataset
     * @throws IllegalArgumentException if {@code k < 2} or {@code k > size()}
     */
    public List<Dataset> split(int k, Random rnd, boolean stratified) throws IllegalArgumentException {
        if (stratified)
            return this.getStratifiedSplit(k, rnd);
        else {
            if (rnd == null)
                return this.getSimpleSplit(k);
            else
                return this.getRandomSplit(k, rnd);
        }
    }

    /**
     * Splits this dataset into {@code k} subsets of approximately equal sizes.
     * 
     * @param k          the number of subsets, must be in the range
     *                   {@code [2..size()]}
     * @return list of {@code k} subsets of this dataset
     * @throws IllegalArgumentException if {@code k < 2} or {@code k > size()}
     */
    private List<Dataset> getSimpleSplit(int k) throws IllegalArgumentException {
        
        int dsize = this.size();
        
        if (k < 1 || k > dsize)
            throw new IllegalArgumentException("k must be in the range [1..size()]");
        
        List<Dataset> list = new ArrayList<>(k); 
        for (int i = 0; i < k; i++)
            list.add(getDataset(-1));
        
        int limit = dsize / k;
        int remainder = dsize % k;
        if (remainder > 0)
            limit++;
        
        int index = 0;
        int tindex = 0;
        
        for (TimeSeries ts : this) {
            
            list.get(tindex).add(ts);
            index++;
            
            if (index == limit) {
                
                index = 0;
                tindex++;
                
                if (remainder > 0) {
                    
                    remainder--;
                    
                    if (remainder == 0) {
                        limit--;
                        remainder = -1;
                    }
                    
                }
                
            }
            
        }
        
        return list;
    }
    
    /***
     * Randomnly divides this dataset into {@code k} subsets of approximately equal
     * sizes using the given random number generator.
     * 
     * @param k   the number of subsets, must be in the range {@code [1..size()]}
     * @param rnd a random number generator
     * @return list of subsets
     * @throws IllegalArgumentException if {@code k < 1} or {@code k > size()}
     */
    private List<Dataset> getRandomSplit(int k, Random rnd) throws IllegalArgumentException {

        int dsize = this.size();

        if (k < 1 || k > dsize)
            throw new IllegalArgumentException("k must be in the range [1..size()]");

        List<Dataset> list = new ArrayList<>(k);

        int foldSize = dsize / k;

        list.add(getDataset(this.size()));
        for (int i = 1; i < k; i++)
            list.add(getDataset(foldSize));

        Dataset first = list.get(0);
        first.addAll(this);

        for (int i = 1; i < k; i++) {

            Dataset ith = list.get(i);

            for (int j = 0; j < foldSize; j++) {

                int index = rnd.nextInt(first.size());
                TimeSeries ts = first.get(index);
                first.remove(index);
                ith.add(ts);

            }

        }

        if (!first.isEmpty() && first.size() + 1 > foldSize) {

            int nk = first.size() - foldSize;

            for (int i = 1; i < nk; i++) {

                Dataset ith = list.get(i);
                int index = rnd.nextInt(first.size());
                TimeSeries series = first.get(index);
                first.remove(index);
                ith.add(series);

            }

        }

        return list;

    }
    
    /**
     * Divides this dataset into {@code k} stratified subsets of approximately equal
     * sizes. If the given random number generator is not {@code null}, it shuffles
     * each group of time series which belong to the same class before splitting the
     * dataset.
     * 
     * @param k   the number of subsets, must be in the range {@code [2..size()]}
     * @param rnd a random number generator
     * @return list of stratified subsets
     * @throws IllegalArgumentException if {@code k < 2} or {@code k > size()}
     */
    private List<Dataset> getStratifiedSplit(int k, Random rnd) throws IllegalArgumentException {

        int listSize = this.size();

        if (k < 2 || k > listSize)
            throw new IllegalArgumentException("k must be in the range [2..size()].");

        List<Dataset> list = new ArrayList<Dataset>(k);
        for (int i = 0; i < k; i++)
            list.add(getDataset(-1));

        Dataset arrangedDataset = this.getArrangedByLabels(rnd);

        for (int i = 0; i < k; i++) {

            Dataset ith = list.get(i);

            for (int j = i; j < arrangedDataset.size(); j += k) {

                TimeSeries ts = arrangedDataset.get(j);
                ith.add(ts);

            }

        }

        return list;
    }

    /**
     * Divides this dataset into two stratified subsets. The first subset will
     * contain the specified {@code percentage} of the time series of the dataset
     * and the second one will contain the rest of the time series.
     * 
     * @param percentage the percentage of time series that are to be included in
     *                   the first subset, must be in the range {@code [0..100]}
     * @return list of two stratified subsets of this dataset
     * @throws IllegalArgumentException if {@code percentage < 0} or
     *                                  {@code percentage > 100}
     */
    public List<Dataset> divide(double percentage) throws IllegalArgumentException {
        return divide(percentage, null, true);
    }
    
    /**
     * Divides this dataset into two subsets. The first subset will contain the
     * specified {@code percentage} of the time series of the dataset and the second
     * one will contain the rest of the time series.
     * 
     * @param percentage the percentage of time series that are to be included in
     *                   the first subset, must be in the range {@code [0..100]}
     * @param stratified indicates whether subsets should be stratified
     * @return list of two subsets of this dataset
     * @throws IllegalArgumentException if {@code percentage < 0} or
     *                                  {@code percentage > 100}
     */
    public List<Dataset> divide(double percentage, boolean stratified) throws IllegalArgumentException {
        return divide(percentage, null, stratified);
    }
    
    /**
     * Divides this dataset into two stratified subsets using the given random
     * number generator. The first subset will contain the specified
     * {@code percentage} of the time series of the dataset and the second one will
     * contain the rest of the time series.
     * 
     * @param percentage the percentage of time series that are to be included in
     *                   the first subset, must be in the range {@code [0..100]}
     * @param rnd        a random number generator
     * @return list of two stratified subsets of this dataset
     * @throws IllegalArgumentException if {@code percentage < 0} or
     *                                  {@code percentage > 100}
     */
    public List<Dataset> divide(double percentage, Random rnd) throws IllegalArgumentException {
        return divide(percentage, rnd, true);
    }
    
    /**
     * Divides this dataset into two subsets using the given random number
     * generator. The first subset will contain the specified {@code percentage} of
     * the time series of the dataset and the second one will contain the rest of
     * the time series.
     * 
     * @param percentage the percentage of time series that are to be included in
     *                   the first subset, must be in the range {@code [0..100]}
     * @param rnd        a random number generator
     * @param stratified indicates whether subsets should be stratified
     * @return list of two subsets of this dataset
     * @throws IllegalArgumentException if {@code percentage < 0} or
     *                                  {@code percentage > 100}
     */
    public List<Dataset> divide(double percentage, Random rnd, boolean stratified) {
        if (stratified)
            return getStratifiedPercentageSplit(percentage, rnd);
        else
            return getPercentageSplit(percentage, rnd);
    }

    /**
     * Randomly divides this dataset into two subsets using the given random number
     * generator. The first subset will contain the specified {@code percentage} of
     * the time series of the dataset and the second one will contain the rest of
     * the time series.
     * 
     * @param percentage the percentage of time series that are to be included in
     *                   the first subset, must be in the range {@code [0..100]}
     * @param rnd        a random number generator
     * @return list of subsets
     * @throws IllegalArgumentException if {@code percentage < 0} or
     *                                  {@code percentage > 100}
     */
    private List<Dataset> getPercentageSplit(double percentage, Random rnd) throws IllegalArgumentException {

        if (percentage < 0 || percentage > 100)
            throw new IllegalArgumentException("Percentage must be in the range [0..100].");

        List<Dataset> list = new ArrayList<Dataset>(2);

        Dataset first = getDataset(-1);
        Dataset second = getDataset(this.size());
        second.addAll(this);
        list.add(first);
        list.add(second);

        long count = Math.round(this.size() * percentage / 100d);

        for (long i = 0; i < count; i++) {

            int index;
            if (rnd != null)
                index = rnd.nextInt(second.size());
            else
                index = 0;

            TimeSeries ts = second.get(index);
            second.remove(index);
            first.add(ts);

        }

        return list;
    }

    /**
     * Divides this dataset into two stratified subsets. The first subset will
     * contain the specified {@code percentage} of the series of the dataset and the
     * second one will contain the rest of the series. If the given random number
     * generator is not {@code null}, it will shuffle the groups of time series
     * which belong to the same class before splitting.
     * 
     * @param percentage the percentage of time series that are to be included in
     *                   the first subset, must be in the range {@code [0..100]}
     * @param rnd        a random number generator
     * @return list of stratified subsets
     * @throws IllegalArgumentException if {@code percentage < 0} or
     *                                  {@code percentage > 100}
     */
    private List<Dataset> getStratifiedPercentageSplit(double percentage, Random rnd)
            throws IllegalArgumentException {

        if (percentage < 0 || percentage > 100)
            throw new IllegalArgumentException("Percentage must be in the range [0..100].");

        List<Dataset> list = new ArrayList<Dataset>(2);

        Dataset first = getDataset(-1);
        Dataset second = getDataset(-1);
        list.add(first);
        list.add(second);

        List<Dataset> subsets = getSubsetsByLabels(rnd);
        
        int size = (int) (this.size() * percentage / 100d);

        for (Dataset ds : subsets) {

            long count = Math.round(ds.size() * percentage / 100d);

            if (first.size() < size) {
                int dif = size - first.size();
                if (count > dif)
                    count = dif;
                for (long i = 0; i < count; i++)
                    first.add(ds.remove(0));
            }

            second.addAll(ds);

        }

        return list;

    }

    /**
     * Returns the average mean value of the time series in this dataset.
     * 
     * @return the average mean value of the time series in this dataset
     */
    public double getAvgMean() {

        double mean = 0;

        for (TimeSeries ts : this)
            mean += ts.getMeanY();

        return mean / this.size();

    }

    /**
     * Returns the average sample standard deviation of the time series in this
     * dataset.
     * 
     * @return the average sample standard deviation of the time series in this
     *         dataset.
     */
    public double getAvgStdDev() {

        double stdDev = 0;
        
        for (TimeSeries ts : this)
            stdDev += ts.getStDevY();

        return stdDev / this.size();

    }

    /**
     * Sorts the time series of this dataset.
     */
    public void sortTimeSeries() {

        for (TimeSeries ts : this)
            ts.sort();

    }

    /**
     * Returns the minimum and maximum length of the time series.
     * 
     * @return the minimum and maximum length of the time series
     */
    public List<Integer> getMinMaxLength() {

        int minLen = Integer.MAX_VALUE;
        int maxLen = -1;

        List<Integer> list = new ArrayList<Integer>();

        for (TimeSeries ts : this) {

            int len = ts.size();

            if (len > maxLen)
                maxLen = len;

            if (len < minLen)
                minLen = len;

        }

        list.add(minLen);
        list.add(maxLen);

        return list;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<TimeSeries> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(TimeSeries e) {
        return list.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends TimeSeries> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends TimeSeries> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public TimeSeries get(int index) {
        return list.get(index);
    }

    @Override
    public TimeSeries set(int index, TimeSeries element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, TimeSeries element) {
        list.add(index, element);
    }

    @Override
    public TimeSeries remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<TimeSeries> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<TimeSeries> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<TimeSeries> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TimeSeries ts: this) {
            sb.append(ts);
            sb.append("%n");
        }
        return sb.toString();
    }
}

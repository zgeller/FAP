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

package fap.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * A time series is in the form of an {@link ArrayList} or a {@link LinkedList}
 * of {@link DataPoint data points}. The specific list type used to store the
 * time series is determined at instantiation.
 * 
 * <p>
 * Each time series may optionally be assigned a class label, a unique index,
 * and a collection of representations. The index serves as a unique identifier
 * within the dataset scope, though ensuring its uniqueness is the user’s
 * responsibility. When provided, this index allows distance measures to cache
 * results—an optional feature that avoids redundant computations and
 * accelerates execution. However, providing this index is mandatory when using
 * distance and neighbor matrices, as kNN classifiers rely on them for mapping.
 * 
 * @author Zoltán Gellér
 * @version 2025.08.14.
 * @see DataPoint
 * @see List
 * @see Serializable
 */
public class TimeSeries implements List<DataPoint>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The index of this time series. Default value is {@code -1}.
     */
    private int index = -1;

    /**
     * The label (class) of this time series. Default value is {@code 0}.
     */
    private double label;

    /**
     * The list that stores the data points of this time series.
     */
    private List<DataPoint> list;

    /**
     * The collection of representations of this time series.
     */
    private Map<Class<? extends Representation>, Representation> representations = new HashMap<Class<? extends Representation>, Representation>();

    /**
     * Constructs a new time series utilizing an {@code ArrayList} for storing data
     * points, with default {@link #label} and {@link #index} values.
     */
    public TimeSeries() {
        this(false);
    }

    /**
     * Constructs a new time series with the specified label utilizing an
     * {@code ArrayList} for storing data points, with default {@link #index} value.
     * 
     * @param label the label of the time series
     */
    public TimeSeries(double label) {
        this(false, label);
    }

    /**
     * Constructs a new time series with the specified {@code label} and
     * {@code index} utilizing an {@code ArrayList} for storing data points.
     * 
     * @param label the label of the time series
     * @param index the index of the time series
     */
    public TimeSeries(double label, int index) {
        this(false, label, index);
    }

    /**
     * Constructs a new time series utilizing an {@code ArrayList} for storing the
     * specified data points, with default {@link #label} and {@link #index} values.
     * 
     * @param points the data points to be stored in the time series
     */
    public TimeSeries(DataPoint[] points) {
        this(false, points);
    }

    /**
     * Constructs a new time series with the specified {@code label} and default
     * {@link #index} value, utilizing an {@code ArrayList} for storing the
     * specified data points.
     * 
     * @param label  the label of the time series
     * @param points the data points to be stored in the time series
     */
    public TimeSeries(double label, DataPoint[] points) {
        this(false, label, points);
    }

    /**
     * Constructs a new time series with the specified {@code label} and
     * {@code index} utilizing an {@code ArrayList} for storing the specified data
     * points.
     * 
     * @param label  the label of the time series
     * @param index  the index of the time series
     * @param points the data points to be stored in the time series
     */
    public TimeSeries(double label, int index, DataPoint[] points) {
        this(false, label, index, points);
    }

    /**
     * Constructs a new time series with the specified {@code label} and default
     * {@link #index} value, utilizing an {@code ArrayList} for storing the
     * specified data points.
     *
     * <p>
     * The time series will be populated with data points created from the specified
     * {@code yValues} as y-coordinates. The x-coordinate of the first point will be
     * zero (0), and for each subsequent point it will increase by one (1).
     * 
     * @param label   the label of the time series
     * @param yValues the y-coordinates of the data points
     */
    public TimeSeries(double label, double... yValues) {
        this(false, label, yValues);
    }

    /**
     * Constructs a new time series with the specified {@code label} and
     * {@code index} utilizing an {@code ArrayList} for storing the specified data
     * points.
     *
     * <p>
     * The time series will be populated with data points created from the specified
     * {@code yValues} as y-coordinates. The x-coordinate of the first point will be
     * zero (0), and for each subsequent point it will increase by one (1).
     * 
     * @param label   the label of the time series
     * @param index   the index of the time series
     * @param yValues the y-coordinates of the data points
     */
    public TimeSeries(double label, int index, double... yValues) {
        this(false, label, index, yValues);
    }

    /**
     * Constructs a new time series utilizing an {@code ArrayList} for storing data
     * points with the specified initial capacity and default {@link #label} and
     * {@link #index} values.
     * 
     * @param initialCapacity the initial capacity of the underlying
     *                        {@code ArrayList}
     */
    public TimeSeries(int initialCapacity) {
        list = new ArrayList<>(initialCapacity);
    }

    /**
     * Constructs a new time series containing the elements of the specified
     * collection.
     * 
     * <p>
     * If {@code c} is a non-null {@code TimeSeries}, data points are stored in the
     * same list type as {@code c}; otherwise, they are stored in an
     * {@code ArrayList}.
     * 
     * <p>
     * If {@code c} is a non-null {@code TimeSeries}, the new time series will have
     * the same label and index as {@code c}; otherwise {@link #label} and
     * {@link #index} will have default values.
     *
     * @param c the collection whose elements are to be placed into this time series
     */
    public TimeSeries(Collection<? extends DataPoint> c) {
        this(false, c);
    }

    /**
     * Constructs a new time series utilizing an {@code ArrayList} or a
     * {@code LinkedList} for storing data points, depending on the
     * {@code linkedList} parameter; with default {@link #label} and {@link #index}
     * values.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     */
    public TimeSeries(boolean linkedList) {
        if (linkedList)
            list = new LinkedList<>();
        else
            list = new ArrayList<>();
    }

    /**
     * Constructs a new time series with the specified {@code label} and default
     * {@link #index} value, utilizing an {@code ArrayList} or a {@code LinkedList}
     * for storing data points, depending on the {@code linkedList} parameter.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param label      the label of the time series
     */
    public TimeSeries(boolean linkedList, double label) {
        this(linkedList);
        this.setLabel(label);
    }

    /**
     * Constructs a new time series with the specified {@code label} and
     * {@code index} utilizing an {@code ArrayList} or a {@code LinkedList} for
     * storing data points, depending on the {@code linkedList} parameter.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param label      the label of the time series
     * @param index      the index of the time series
     */
    public TimeSeries(boolean linkedList, double label, int index) {
        this(linkedList, label);
        this.setIndex(index);
    }

    /***
     * Constructs a new time series utilizing an {@code ArrayList} or a
     * {@code LinkedList} for storing the specified data points, depending on the
     * {@code linkedList} parameter; with default {@link #label} and {@link #index}
     * values.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param points     the data points to be stored in the time series
     */
    public TimeSeries(boolean linkedList, DataPoint[] points) {
        this(linkedList);
        if (points != null)
            for (DataPoint dp: points)
                this.add(dp);
    }

    /**
     * Constructs a new time series with the specified {@code label} and default
     * {@link #index} value, utilizing an {@code ArrayList} or a {@code LinkedList}
     * for storing the specified data points, depending on the {@code linkedList}
     * parameter.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param label      the label of the time series
     * @param points     the data points to be stored in the time series
     */
    public TimeSeries(boolean linkedList, double label, DataPoint[] points) {
        this(linkedList, points);
        this.setLabel(label);
    }

    /**
     * Constructs a new time series with the specified {@code label} and
     * {@code index} utilizing an {@code ArrayList} or a {@code LinkedList} for
     * storing the specified data points, depending on the {@code linkedList}
     * parameter.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param label      the label of the time series
     * @param index      the index of the time series
     * @param points     the data points to be stored in the time series
     */
    public TimeSeries(boolean linkedList, double label, int index, DataPoint[] points) {
        this(linkedList, label, points);
        this.setIndex(index);
    }

    /**
     * Constructs a new time series with the specified {@code label} and default
     * {@link #index} value, utilizing an {@code ArrayList} or a {@code LinkedList}
     * for storing data points, depending on the {@code linkedList} parameter.
     * 
     * <p>
     * The time series will be populated with data points created from the specified
     * {@code yValues} as y-coordinates. The x-coordinate of the first point will be
     * zero (0), and for each subsequent point it will increase by one (1).
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param label      the label of the time series
     * @param yValues    the y-coordinates of the data points
     */
    public TimeSeries(boolean linkedList, double label, double... yValues) {
        this(linkedList, label);
        if (yValues != null) {
            int x = 0;
            for (double y : yValues)
                this.add(new DataPoint(x++, y));
        }
    }

    /**
     * Constructs a new time series with the specified {@code label} and
     * {@code index} utilizing an {@code ArrayList} or a {@code LinkedList} for
     * storing data points, depending on the {@code linkedList} parameter.
     * 
     * <p>
     * The time series will be populated with data points created from the specified
     * {@code yValues} as y-coordinates. The x-coordinate of the first point will be
     * zero (0), and for each subsequent point it will increase by one (1).
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false}) should be used to
     *                   store the data points
     * @param label      the label of the time series
     * @param index      the index of the time series
     * @param yValues    the y-coordinates of the data points
     */
    public TimeSeries(boolean linkedList, double label, int index, double... yValues) {
        this(linkedList, label, yValues);
        this.setIndex(index);
    }

    /**
     * Constructs a new time series containing copies of the data points of the
     * specified collection.
     * 
     * <p>
     * Data points are stored in an {@code ArrayList} or a {@code LinkedList},
     * depending on the {@code linkedList} parameter.
     * 
     * <p>
     * If {@code c} is a non-null {@code TimeSeries}, the new time series will have
     * the same label and index as {@code c}; otherwise {@link #label} and
     * {@link #index} will have default values.
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param c          the collection whose elements are to be placed into this
     *                   time series
     */
    public TimeSeries(boolean linkedList, Collection<? extends DataPoint> c) throws NullPointerException {

        if (linkedList)
            list = new LinkedList<>();

        else if (c == null)
            list = new ArrayList<>();

        else {

            list = new ArrayList<>(c.size());

            // data points
            for (DataPoint dp : c)
                list.add(new DataPoint(dp));

            // label and index
            if (c instanceof TimeSeries cts) {
                this.setLabel(cts.getLabel());
                this.setIndex(cts.getIndex());
            }

        }

    }

    /**
     * Returns {@code true} if the data points are stored in an {@code ArrayList}.
     * 
     * @return {@code true} if the data points are stored in an {@code ArrayList}
     */
    public boolean isArrayList() {
        return list instanceof ArrayList<?>;
    }

    /**
     * Adds a new representation of this time series. Overwrites previous
     * representation of the same representation class.
     * 
     * @param repr the representation to add
     */
    public void addRepresentation(Representation repr) {
        representations.put(repr.getClass(), repr);
    }

    /**
     * Removes specific representation.
     * 
     * @param <T>       representation type to remove
     * @param reprClass class of representation type to remove
     */
    public <T extends Representation> void removeRepresentation(Class<T> reprClass) {
        representations.remove(reprClass);
    }

    /**
     * Access specific representation of the time series.
     * 
     * @param <T>       representation type
     * @param reprClass class of representation type
     * @return representation of specific type, or null if one doesn't exist
     */
    @SuppressWarnings("unchecked")
    public <T extends Representation> T getRepresentation(Class<T> reprClass) {
        return (T) representations.get(reprClass);
    }

    /**
     * Returns all representations of this time series.
     * 
     * @return all representations of the time series
     */
    public Collection<Representation> getAllRepresentations() {
        return representations.values();
    }

    /**
     * Returns the index of this time series.
     * 
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of this time series.
     * 
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the label of this time series.
     * 
     * @return label label of this time series
     */
    public double getLabel() {
        return label;
    }

    /**
     * Sets the label of this time series.
     * 
     * @param label the new label
     */
    public void setLabel(double label) {
        this.label = label;
    }

    /**
     * Returns the maximum value of the y-coordinates of the data points in this
     * time series, or {@code Double.NaN} if the series contains no data points.
     * 
     * @return the maximum value of the y-coordinates of the data points in this
     *         time series, or {@code Double.NaN} if the series contains no data
     *         points
     */
    public double maxY() {
        
        if (this.isEmpty())
            return Double.NaN;

        double maxy = Double.NEGATIVE_INFINITY;

        for (DataPoint dp : this) {
            double y = dp.getY();
            if (y > maxy)
                maxy = y;
        }

        return maxy;

    }

    /**
     * Returns the maximum absolute value of the y-coordinates of the data points in
     * this time series, or {@code Double.NaN} if the series contains no data
     * points.
     * 
     * @return the maximum absolute value of the y-coordinates of the data points in
     *         this time series, or {@code Double.NaN} if the series contains no
     *         data points
     */
    public double maxAbsY() {

        if (this.isEmpty())
            return Double.NaN;
        
        double maxy = 0;

        for (DataPoint dp : this) {
            double y = Math.abs(dp.getY());
            if (y > maxy)
                maxy = y;
        }

        return maxy;

    }

    /**
     * Returns the minimum value of the y-coordinates of the data points in this
     * time series, or {@code Double.NaN} if the series contains no data points.
     * 
     * @return the minimum value of the y-coordinates of the data points in this
     *         time series, or {@code Double.NaN} if the series contains no data
     *         points
     */
    public double minY() {

        if (this.isEmpty())
            return Double.NaN;
        
        double miny = Double.POSITIVE_INFINITY;

        for (DataPoint dp : this) {
            double y = dp.getY();
            if (y < miny)
                miny = y;
        }

        return miny;

    }

    /**
     * Returns the minimum absolute value of the y-coordinates of the data points in
     * this time series, or {@code Double.NaN} if the series contains no data
     * points.
     * 
     * @return the minimum absolute value of the y-coordinates of the data points in
     *         this time series, or {@code Double.NaN} if the series contains no
     *         data points
     */
    public double minAbsY() {
        
        if (this.isEmpty())
            return Double.NaN;

        double miny = Double.POSITIVE_INFINITY;

        for (DataPoint dp : this) {
            double y = Math.abs(dp.getY());
            if (y < miny)
                miny = y;
        }

        return miny;

    }

    /**
     * Returns the range of the y-coordinates ({@code maxY() - minY()}) of the data
     * points in this time series.
     * 
     * @return the range of the y-coordinates ({@code maxY() - minY()}) of the data
     *         points in this time series
     */
    public double rangeY() {
        return maxY() - minY();
    }

    /**
     * Returns the maximum value of the x-coordinates of the data points in this
     * time series, or {@code Double.NaN} if the series contains no data points.
     * 
     * @return the maximum value of the x-coordinates of the data points in this
     *         time series, or {@code Double.NaN} if the series contains no data
     *         points
     */
    public double maxX() {

        if (this.isEmpty())
            return Double.NaN;
        
        double maxx = Double.NEGATIVE_INFINITY;

        for (DataPoint dp : this) {
            double x = dp.getX();
            if (x > maxx)
                maxx = x;
        }

        return maxx;

    }

    /**
     * Returns the minimum value of the x-coordinates of the data points in this
     * time series, or {@code Double.NaN} if the series contains no data points.
     * 
     * @return the minimum value of the x-coordinates of the data points in this
     *         time series, or {@code Double.NaN} if the series contains no data
     *         points
     */
    public double minX() {

        if (this.isEmpty())
            return Double.NaN;
        
        double minx = Double.POSITIVE_INFINITY;

        for (DataPoint dp : this) {
            double x = dp.getX();
            if (x < minx)
                minx = x;
        }

        return minx;

    }

    /**
     * Returns the mean value of the y-coordinates of the data points in this time
     * series, or {@code Double.NaN} if the series contains no data points.
     * 
     * @return the mean value of the y-coordinates of the data points in this time
     *         series, or {@code Double.NaN} if the series contains no data points.
     */
    public double meanY() {

        if (this.isEmpty())
            return Double.NaN;
        
        double mean = 0;

        for (DataPoint dp : this)
            mean += dp.getY();

        return mean / size();

    }

    /**
     * Returns the median of the y-coordinates of the data points in this time
     * series, or {@code Double.NaN} if the series contains no data points.
     * 
     * @return the median of the y-coordinates of the data points in this time
     *         series, or {@code Double.NaN} if the series contains no data points.
     */
    public double medianY() {

        if (this.isEmpty())
            return Double.NaN;

        double median = Double.NaN;

        List<Double> values = new ArrayList<>(size());
        for (DataPoint dp : this)
            values.add(dp.getY());

        values.sort(null);
        int len = values.size();
        int middle = len / 2; // zero-bazed indexing

        if (len % 2 == 1)
            median = values.get(middle);

        else
            median = (values.get(middle - 1) + values.get(middle)) / 2;

        return median;

    }

    /**
     * Returns the sample standard deviation of the y-coordinates of the data points
     * in this time series, or {@code Double.NaN} if the series contains no data
     * points.
     * 
     * @return the sample standard deviation of the y-coordinates of the data points
     *         in this time series, or {@code Double.NaN} if the series contains no
     *         data points
     */
    public double stdevY() {
        return Math.sqrt(varianceY());
    }

    /**
     * Returns the population or sample standard deviation of the y-coordinates of
     * the data points in this time series, or {@code Double.NaN} if the series
     * contains no data points.
     * 
     * @param population indicates whether to calculate population ({@code true}) od
     *                   sample ({@code false}) standard deviation
     * @return the population or sample standard deviation of the y-coordinates of
     *         the data points in this time series, or {@code Double.NaN} if the
     *         series contains no data points
     */
    public double stdevY(boolean population) {
        return Math.sqrt(varianceY(population));
    }

    /**
     * Returns the sample variance of the y-coordinates of the data points in this
     * time series, or {@code Double.NaN} if the series contains no data points.
     * 
     * @return the sample variance of the y-coordinates of the data points of this
     *         time series, or {@code Double.NaN} if the series contains no data
     *         points
     */
    public double varianceY() {
        return varianceY(false);
    }

    /**
     * Returns the population or sample variance of the y-coordinates of the data
     * points in this time series, or {@code Double.NaN} if the series contains no
     * data points.
     * 
     * @param population indicates whether to use population ({@code true}) od
     *                   sample ({@code false}) standard deviation
     * @return the population or sample variance of the y-coordinates of the data
     *         points in this time series, or {@code Double.NaN} if the series
     *         contains no data points
     */
    public double varianceY(boolean population) {

        if (this.isEmpty())
            return Double.NaN;
        
        int n = size();
        if (!population)
            n--;
        
        if (n == 0)
            return 0;

        double mean = meanY();
        double variance = 0;

        for (DataPoint dp : this) {
            double delta = dp.getY() - mean;
            variance += delta * delta;
        }

        return variance / n;

    }

    /**
     * Sorts the data points in this time series in ascending order by their
     * x-coordinate.
     */
    public void sortByX() {
        Collections.sort(list);
    }

    /**
     * Returns an array containing the x-coordinates of the data points in this time
     * series, or {@code null} if there are no data points.
     * 
     * @return an array containing the x-coordinates of the data points in this
     *         series, or {@code null} if there are no data points.
     */
    public double[] getXValues() {

        if (this.isEmpty())
            return null;

        int len = this.size();
        double[] xValues = new double[len];

        for (int i = 0; i < len; i++)
            xValues[i] = this.getX(i);

        return xValues;
        
    }
    
    /**
     * Returns an array containing the y-coordinates of the data points in this time
     * series, or {@code null} if there are no data points.
     * 
     * @return an array containing the y-coordinates of the data points in this
     *         series, or {@code null} if there are no data points.
     */
    public double[] getYValues() {
        return getYValues(false);
    }

    /**
     * Returns an array containing the y-coordinates of the data points in this time
     * series, or {@code null} if there are no data points.
     * 
     * @param xsorted if {@code true}, data points are sorted by their x-coordinate
     * @return an array containing the y-coordinates of the data points in this time
     *         series, or {@code null} if there are no data points.
     */
    public double[] getYValues(boolean xsorted) {
        
        if (this.isEmpty())
            return null;

        List<DataPoint> points;

        if (xsorted) {
            points = new ArrayList<DataPoint>(this);
            Collections.sort(points);
        } else
            points = this;

        int len = points.size();
        double[] yValues = new double[len];

        for (int i = 0; i < len; i++)
            yValues[i] = points.get(i).getY();

        return yValues;

    }

    @Override
    public int size() {
        return list.size();
    }

    /**
     * Returns the number of data points of this time series. The same as
     * {@link #size()}.
     * 
     * @return the number of data points of this time series
     */
    public int length() {
        return size();
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
    public Iterator<DataPoint> iterator() {
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
    public boolean add(DataPoint dp) {
        return list.add(dp);
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
    public boolean addAll(Collection<? extends DataPoint> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends DataPoint> c) {
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

    /**
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    @Override
    public DataPoint get(int index) {
        return list.get(index);
    }

    /**
     * Returns the y-coordinate of the data point at the specified position in this
     * time series.
     * 
     * @param index index of the data point
     * @return the y-coordinate of the data point at the specified position in this
     *         time series
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public double getY(int index) throws IndexOutOfBoundsException {
        return get(index).getY();
    }

    /**
     * Returns the x-coordinate of the data point at the specified position in this
     * time series.
     * 
     * @param index index of the data point
     * @return the x-coordinate of the data point at the specified position in this
     *         time series
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public double getX(int index) throws IndexOutOfBoundsException {
        return get(index).getX();
    }

    /**
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    @Override
    public DataPoint set(int index, DataPoint dp) {
        return list.set(index, dp);
    }
    
    /**
     * Sets the y-coordinate of the data point at the specified position in this
     * time series.
     * 
     * @param index index of the data point whose y-coordinate to replace
     * @param y     the new value of the y-coordinate of the data point at the
     *              specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public void setY(int index, double y) {
        this.get(index).setY(y);
    }
    
    /**
     * Sets the y-coordinate of every data point in this time series to the
     * specified value.
     * 
     * @param y the value to assign to the y-coordinate of each data point
     */
    public void setY(double y) {
        for (DataPoint dp : this)
            dp.setY(y);
    }
    
    /**
     * Sets the x-coordinate of the data point at the specified position in this
     * time series.
     * 
     * @param index index of the data point whose x-coordinate to replace
     * @param x     the new value of the x-coordinate of the data point at the
     *              specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public void setX(int index, double x) {
        this.get(index).setX(x);
    }
    
    /**
     * Sets the x- and y-coordinates of the data point at the specified position in
     * this time series.
     * 
     * @param index index of the data point whose x- and y-coordinates to replace
     * @param x     the new value of the x-coordinate of the data point at the
     *              specified position
     * @param y     the new value of the y-coordinate of the data point at the
     *              specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public void setXY(int index, double x, double y) {
        DataPoint dp = this.get(index);
        dp.setX(x);
        dp.setY(y);
    }

    /**
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    @Override
    public void add(int index, DataPoint dp) {
        list.add(index, dp);
    }
    
    /**
     * Inserts a new data point with the specified x- and y-coordinates at the
     * specified position in this time series. Shifts the data point currently at
     * that position (if any) and any subsequent data points to the right (adds one
     * to their indices).
     * 
     * <p>
     * The same as {@code add(index, new DataPoint(x, y))}.
     * 
     * @param index index at which the specified data point is to be inserted
     * @param x     the value of the x-coordinate
     * @param y     the value of the y-coordinate
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public void add(int index, double x, double y) {
        this.add(index, new DataPoint(x, y));
    }

    /**
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    @Override
    public DataPoint remove(int index) {
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
    public ListIterator<DataPoint> listIterator() {
        return list.listIterator();
    }

    /**
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    @Override
    public ListIterator<DataPoint> listIterator(int index) {
        return list.listIterator(index);
    }

    /**
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *         ({@code fromIndex < 0 || toIndex > size ||
     *         fromIndex > toIndex})
     */
    @Override
    public List<DataPoint> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    /**
     * Shifts the x-coordinates of the data points in this time series by the given
     * factor.
     * 
     * @param factor the factor to shift the x-coordinates of the data points
     */
    public void shiftX(double factor) {
        for (DataPoint dp : this)
            dp.shiftX(factor);
    }
    
    /**
     * Shifts the y-coordinates of the data points in this time series by the given
     * factor.
     * 
     * @param factor the factor to shift the y-coordinates of the data points
     */
    public void shiftY(double factor) {
        for (DataPoint dp : this)
            dp.shiftY(factor);
    }
    
    /**
     * Shifts the coordinates of the data points in this time series by the given
     * factors.
     * 
     * @param xFactor the factor to shift the x-coordinates of the data points
     * @param yFactor the factor to shift the y-coordinates of the data points
     */
    public void shiftXY(double xFactor, double yFactor) {
        for (DataPoint dp : this)
            dp.shiftXY(xFactor, yFactor);
    }

    /**
     * Scales the x-coordinates of the data points in this time series by the given
     * factor.
     * 
     * @param factor the factor to scale the x-coordinates of the data points
     */
    public void scaleX(double factor) {
        for (DataPoint dp : this)
            dp.scaleX(factor);
    }
    
    /**
     * Scales the y-coordinates of the data points in this time series by the given
     * factor.
     * 
     * @param factor the factor to scale the y-coordinates of the data points
     */
    public void scaleY(double factor) {
        for (DataPoint dp : this)
            dp.scaleY(factor);
    }
    
    /**
     * Scales the coordinates of the data points of this time series by the given factors.
     * 
     * @param xFactor the factor to scale the x-coordinates of the data points
     * @param yFactor the factor to scale the y-coordinates of the data points
     */
    public void scaleXY(double xFactor, double yFactor) {
        for (DataPoint dp : this)
            dp.scaleXY(xFactor, yFactor);
    }
    
    /**
     * Divides the x-coordinates of the data points in this time series by the given
     * factor.
     * 
     * @param factor the factor to divide the x-coordinates of the data points
     */
    public void divideX(double factor) {
        for (DataPoint dp : this)
            dp.divideX(factor);
    }
    
    /**
     * Divides the y-coordinates of the data points in this time series by the given
     * factor.
     * 
     * @param factor the factor to divide the y-coordinates of the data points
     */
    public void divideY(double factor) {
        for (DataPoint dp : this)
            dp.divideY(factor);
    }
    
    /**
     * Divides the coordinates of the data points in this time series by the given
     * factors.
     * 
     * @param xFactor the factor to divide the x-coordinates of the data points
     * @param yFactor the factor to divide the y-coordinates of the data points
     */
    public void divideXY(double xFactor, double yFactor) {
        for (DataPoint dp : this)
            dp.divideXY(xFactor, yFactor);
    }
    
    /**
     * Normalizes the y-coordinates of this time series using the Z-score method:
     * 
     * <blockquote> <img src="doc-files/TimeSeries-zNorm.png"> </blockquote>
     * 
     * where <code>μ<sub>A</sub></code> is the mean value of {@code A}, and
     * <code>σ<sub>A</sub></code> the sample standard deviation of {@code A}.
     * 
     * <p>
     * <ul>
     *  <li> if <code>σ<sub>A</sub> = 0</code>, the y-coordinate of each data point
     *       will be set to 0 (zero).
     * </ul>
     */
    public void normalizeYZScore() {
        this.normalizeYZScore(false);
    }
    
    /**
     * Normalizes the y-coordinates of this time series using the Z-score method:
     * 
     * <blockquote> <img src="doc-files/TimeSeries-zNorm.png"> </blockquote>
     * 
     * where <code>μ<sub>A</sub></code> is the mean value of {@code A}, and
     * <code>σ<sub>A</sub></code> the population or sample standard deviation of
     * {@code A}, as indicated by the {@code population} parameter.
     * 
     * <p>
     * <ul>
     *  <li> if <code>σ<sub>A</sub> = 0</code>, the y-coordinate of each data point
     *       will be set to 0 (zero).
     * </ul>
     *
     * @param population indicates whether to use population ({@code true}) od
     *                   sample ({@code false}) standard deviation
     */
    public void normalizeYZScore(boolean population) {
        
        if (this.isEmpty())
            return;
        
        double stdev = this.stdevY(population);
        
        if (stdev != 0) {
            
            double mean = this.meanY();
            
            for (DataPoint dp : this)
                dp.setY((dp.getY() - mean) / stdev);
            
        }
        
        else
            this.setY(0);
            
    }
    
    
    /**
     * Normalizes the y-coordinates of this time series using the mean normalization
     * method:
     * 
     * <blockquote> <img src="doc-files/TimeSeries-meanNorm.png"> </blockquote>
     * 
     * where <code>μ<sub>A</sub></code> is the mean value of {@code A}.
     * 
     * <p>
     * <ul>
     *  <li> if <code><i>max</i>(A) = <i>min</i>(A)</code>, the y-coordinate of each
     *       data point will be set to 0 (zero).
     * </ul>
     * 
     */
    public void normalizeYMean() {
        
        double mean = this.meanY();
        double range = this.maxY() - this.minY();
        
        if (range != 0)
            for (DataPoint dp : this)
                dp.setY((dp.getY() - mean) / range);
        
        else
            this.setY(0);
        
    }
    
    /**
     * Normalizes the y-coordinates of this time series using the min-max method:
     * 
     * <blockquote> <img src="doc-files/TimeSeries-minmaxNorm-1.png"> </blockquote>
     * 
     * <p>
     * <ul>
     *  <li> if <code><i>max</i>(A) = <i>min</i>(A)</code>, the y-coordinate of each
     *       data point will be set to 0 (zero).
     * </ul>
     * 
     * <p>
     * The same as {@code normalizeYMinMax(0, 1)}.
     * 
     */
    public void normalizeYMinMax() {
        this.normalizeYMinMax(0, 1);
    }
    
    /**
     * Normalizes the y-coordinates of this time series using the min-max method:
     * 
     * <blockquote> <img src="doc-files/TimeSeries-minmaxNorm-2.png"> </blockquote>
     * 
     * <p>
     * <ul>
     *  <li> if <code><i>max</i>(A) = <i>min</i>(A)</code>, the y-coordinate of each
     *       data point will be set to <code><i>min</i></code>.
     * </ul>
     * 
     * @param min the lower bound
     * @param max the upper bound
     */
    public void normalizeYMinMax(double min, double max) {
        
        double miny = this.minY();
        double range = this.maxY() - this.minY();
        double delta = max - min;
        
        if (delta == 0 || range == 0)
            this.setY(min);
        
        else
            for (DataPoint dp : this)
                dp.setY(min + ((dp.getY() - miny) / range) * delta);
        
    }
    
    /**
     * Normalizes the y-coordinates of this time series using the maximum absolute scaling method:
     * 
     * <blockquote> <img src="doc-files/TimeSeries-maxabsNorm.png"> </blockquote>
     * <p>
     */
    public void normalizeYMaxAbs() {
        
        double maxAbs = this.maxAbsY();
        
        if (maxAbs > 0)
            this.divideY(maxAbs);
        
    }
    
    /**
     * Normalizes the y-coordinates of this time series using the decimal scaling method:
     *
     * <blockquote> <img src="doc-files/TimeSeries-decimalScaling.png">, </blockquote>
     * 
     * where {@code d} is the number of digits in the integer part of <code>max(|A|)</code>.
     * <p>
     * 
     */
    public void normalizeYDecimalScaling() {

        int k = Long.toString((long)this.maxAbsY()).length();

        double tenp = Math.pow(10, k);
        
        this.divideY(tenp);
        
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("index=" + getIndex() + ", label=" + getLabel() + ", data=(");
        for (DataPoint dp : this)
            sb.append(dp + ",");
        if (!this.isEmpty())
            sb.setLength(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }

    /**
     * Returns a new time series containing the specified {@code yVvalues} as
     * y-coordinates utilizing an {@code ArrayList} for storing data points, with
     * default {@link #label} and {@link #index} values.
     * 
     * <p>
     * The x-coordinate of the first point will be zero (0), and for each subsequent
     * point it will increase by one (1).
     * 
     * @param yValues the y-coordinates of the data points
     * @return a new time series containing the specified y-values
     */
    public static TimeSeries of(double... yValues) {
        return of(false, yValues);
    }

    /**
     * Returns a new time series containing the specified {@code yValues} as
     * y-coordinates utilizing an {@code ArrayList} or a {@code LinkedList} for
     * storing data points, depending on the {@code linkedList} parameter; with
     * default {@link #label} and {@link #index} values.
     * 
     * <p>
     * The x-coordinate of the first point will be zero (0), and for each subsequent
     * point it will increase by one (1).
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false}) should be used to
     *                   store the data points
     * @param yValues    the y-coordinates of the data points
     * @return a new time series containing the specified y-values
     */
    public static TimeSeries of(boolean linkedList, double... yValues) {

        TimeSeries ts;

        if (linkedList || yValues == null)
            ts = new TimeSeries(linkedList);
        else
            ts = new TimeSeries(yValues.length);

        if (yValues != null) {
            int x = 0;
            for (double y : yValues)
                ts.add(new DataPoint(x++, y));
        }

        return ts;

    }
    
    private static void print(double... array) {
        if (array != null)
            System.out.println(Arrays.toString(array));
        else
            System.out.println("null");
    }
    
    public static void main(String[] args) {
        TimeSeries a = TimeSeries.of(-2, 3, 4);
        TimeSeries ts = new TimeSeries(a);
        System.out.println("max      = " + ts.maxY());
        System.out.println("min      = " + ts.minY());
        System.out.println("range    = " + ts.rangeY());
        System.out.println("maxAbs   = " + ts.maxAbsY());
        System.out.println("minAbs   = " + ts.minAbsY());
        System.out.println("mean     = " + ts.meanY());
        System.out.println("variance = " + ts.varianceY());
        System.out.println("var_pop  = " + ts.varianceY(true));
        System.out.println("stdev    = " + ts.stdevY());
        System.out.println("std_pop  = " + ts.stdevY(true));
        System.out.println("median   = " + ts.medianY());
        
        ts.normalizeYZScore();
        System.out.println();
        System.out.println("ZScore");
        print(ts.getYValues());
        
        ts = new TimeSeries(a);
        ts.normalizeYZScore(true);
        System.out.println();
        System.out.println("ZScore(true)");
        print(ts.getYValues());
        
        ts = new TimeSeries(a);
        ts.normalizeYMean();
        System.out.println();
        System.out.println("Mean");
        print(ts.getYValues());
        
        ts = new TimeSeries(a);
        ts.normalizeYMinMax();;
        System.out.println();
        System.out.println("MinMax");
        print(ts.getYValues());

        ts = new TimeSeries(a);
        ts.normalizeYMinMax(5, 8);
        System.out.println();
        System.out.println("MinMax(5, 8)");
        print(ts.getYValues());
        
        ts = new TimeSeries(a);
        ts.normalizeYMaxAbs();
        System.out.println();
        System.out.println("MaxAbs");
        print(ts.getYValues());
        
        ts = new TimeSeries(a);
        ts.normalizeYDecimalScaling();
        System.out.println();
        System.out.println("DecimalScaling");
        print(ts.getYValues());
        
    }

}

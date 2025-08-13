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

package fap.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * A time series in in the form of an {@link ArrayList} or a {@link LinkedList}
 * of {@link DataPoint data points}. Which list type will be used to store the
 * time series is determined at instantiation.
 * 
 * @author Zoltán Gellér
 * @version 2025.08.13.
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
     * The time series will be populated with data points constructed from the
     * specified {@code y} values (coordinates). The {@code x} coordinate of the
     * first point will be zero (0), and for each subsequent point it will increase
     * by one (1).
     * 
     * @param label   the label of the time series
     * @param yValues the {@code y} coordinates of the data points
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
     * The time series will be populated with data points constructed from the
     * specified {@code y} values (coordinates). The {@code x} coordinate of the
     * first point will be zero (0), and for each subsequent point it will increase
     * by one (1).
     * 
     * @param label   the label of the time series
     * @param index   the index of the time series
     * @param yValues the {@code y} coordinates of the data points
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
            for (DataPoint dp : points)
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
     * The time series will be populated with data points constructed from the
     * specified {@code y} values (coordinates). The {@code x} coordinate of the
     * first point will be zero (0), and for each subsequent point it will increase
     * by one (1).
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param label      the label of the time series
     * @param yValues    the {@code y} coordinates of the data points
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
     * The time series will be populated with data points constructed from the
     * specified {@code y} values (coordinates). The {@code x} coordinate of the
     * first point will be zero (0), and for each subsequent point it will increase
     * by one (1).
     * 
     * @param linkedList determines whether a {@code LinkedList} ({@code true}) or
     *                   an {@code ArrayList} ({@code false} )should be used to
     *                   store the data points
     * @param label      the label of the time series
     * @param index      the index of the time series
     * @param yValues    the {@code y} coordinates of the data points
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
     * @param pos the index to set
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
     * Returns the maximum value of the {@code y} coordinates of the data points of
     * this time series.
     * 
     * @return the maximum value of the {@code y} coordinates of the data points of
     *         this time series
     */
    public double maxY() {

        double maxy = Double.NEGATIVE_INFINITY;

        for (DataPoint dp : this) {
            double y = dp.getY();
            if (y > maxy)
                maxy = y;
        }

        return maxy;

    }

    /**
     * Returns the maximum of the absolute {@code y} values of the data points of
     * this time series.
     * 
     * @return the maximum of the absolute {@code y} values of the data points of
     *         this time series
     */
    public double maxAbsY() {

        double maxy = 0;

        for (DataPoint dp : this) {
            double y = Math.abs(dp.getY());
            if (y > maxy)
                maxy = y;
        }

        return maxy;

    }

    /**
     * Returns the minimum value of the {@code y} coordinates of the data points of
     * this time series.
     * 
     * @return the minimum value of the {@code y} coordinates of the data points of
     *         this time series
     */
    public double minY() {

        double miny = Double.POSITIVE_INFINITY;

        for (DataPoint dp : this) {
            double y = dp.getY();
            if (y < miny)
                miny = y;
        }

        return miny;

    }

    /**
     * Returns the minimum of the absolute {@code y} values of the data points of
     * this time series.
     * 
     * @return the minimum of the absolute {@code y} values of the data points of
     *         this time series
     */
    public double minAbsY() {

        double miny = Double.POSITIVE_INFINITY;

        for (DataPoint dp : this) {
            double y = Math.abs(dp.getY());
            if (y < miny)
                miny = y;
        }

        return miny;

    }

    /**
     * Returns the range of the {@code y} coordinates
     * ({@code maxY() - minY()}) of the data points of this time series.
     * 
     * @return the range of the {@code y} coordinates
     *         ({@code maxY() - minY()}) of the data points of this time
     *         series
     */
    public double rangeY() {
        return maxY() - minY();
    }

    /**
     * Returns the maximum value of the {@code x} coordinates of the data points of
     * this time series.
     * 
     * @return the maximum value of the {@code x} coordinates of the data points of
     *         this time series
     */
    public double maxX() {

        double maxx = Double.NEGATIVE_INFINITY;

        for (DataPoint dp : this) {
            double x = dp.getX();
            if (x > maxx)
                maxx = x;
        }

        return maxx;

    }

    /**
     * Returns the minimum value of the {@code x} coordinates of the data points of
     * this time series.
     * 
     * @return the minimum value of the {@code x} coordinates of the data points of
     *         this time series
     */
    public double minX() {

        double minx = Double.POSITIVE_INFINITY;

        for (DataPoint dp : this) {
            double x = dp.getX();
            if (x < minx)
                minx = x;
        }

        return minx;

    }

    /**
     * Returns the mean value of the {@code y} coordinates of the data points of
     * this time series.
     * 
     * @return the mean value of the {@code y} coordinates of the data points of
     *         this time series
     */
    public double meanY() {

        double mean = 0;

        for (DataPoint dp : this)
            mean += dp.getY();

        return mean / size();

    }

    /**
     * Returns the median of the {@code y} coordinates of the data points of this
     * time series.
     * 
     * @return the median of the {@code y} coordinates of the data points of this
     *         time series
     */
    public double medianY() {

        double median = Double.NaN;

        List<Double> values = new ArrayList<>(size());
        for (DataPoint dp : this)
            values.add(dp.getY());

        if (!values.isEmpty()) {

            values.sort(null);
            int len = values.size();
            int middle = len / 2; // zero-bazed indexing

            if (len % 2 == 1)
                median = values.get(middle);

            else
                median = (values.get(middle - 1) + values.get(middle)) / 2;

        }

        return median;

    }

    /**
     * Returns the sample standard deviation of the {@code y} coordinates of the
     * data points of this time series.
     * 
     * @return the sample standard deviation of the {@code y} coordinates of the
     *         data points of this time series
     */
    public double stdevY() {
        return Math.sqrt(varianceY());
    }

    /**
     * Returns the population or sample standard deviation of the {@code y}
     * coordinates of the data points of this time series.
     * 
     * @param population {@code true} denotes population standard deviation,
     *                   {@code false} denotes sample standard deviation
     * @return the population or sample standard deviation of the {@code y}
     *         coordinates of the data points of this time series
     */
    public double stdevY(boolean population) {
        return Math.sqrt(varianceY(population));
    }

    /**
     * Returns the sample variance of the {@code y} coordinates of the data points
     * of this time series.
     * 
     * @return the sample variance of the {@code y} coordinates of the data points
     *         of this time series
     */
    public double varianceY() {
        return varianceY(false);
    }

    /**
     * Returns the population or sample variance of the {@code y} coordinates of the
     * data points of this time series.
     * 
     * @param population {@code true} denotes population variance, {@code false}
     *                   denotes sample variance
     * @return the population or sample variance of the {@code y} coordinates of the
     *         data points of this time series
     */
    public double varianceY(boolean population) {

        int n = size();
        if (!population)
            n--;

        double mean = meanY();
        double variance = 0;

        for (DataPoint dp : this) {
            double delta = dp.getY() - mean;
            variance += delta * delta;
        }

        return variance / n;

    }

    /**
     * Sorts the data points of this time series based on the time component, i.e.
     * the {@code x} coordinate.
     */
    public void sort() {
        Collections.sort(this);
    }

    /**
     * Returns an array containing the {@code y} coordinates of the data points of
     * this time series.
     * 
     * @return an array containing the {@code y} coordinates of the data points of
     *         this series
     */
    public double[] yValues() {
        return yValues(false);
    }

    /**
     * Returns an array containing the {@code y} coordinates of the data points of
     * this time series.
     * 
     * @param xsorted if {@code true}, data points are sorted by their {@code x}
     *                coordinate
     * @return an array containing the {@code y} coordinates of the data points of
     *         this time series
     */
    public double[] yValues(boolean xsorted) {

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
     * Returns the {@code y} coordinate of the data point at the specified position
     * in this time series.
     * 
     * @param index index of the data point
     * @return the {@code y} coordinate of the data point at the specified position
     *         in this time series
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public double getY(int index) throws IndexOutOfBoundsException {
        return get(index).getY();
    }

    /**
     * Returns the {@code x} coordinate of the data point at the specified position
     * in this time series.
     * 
     * @param index index of the data point
     * @return the {@code x} coordinate of the data point at the specified position
     *         in this time series
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
     * Sets the {@code y} coordinate of the data point at the specified position in
     * this time series.
     * 
     * @param index index of the data point whose {@code y} coordinate to replace
     * @param y     the new value of the {@code y} coordinate of the data point at
     *              the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public void setY(int index, double y) {
        this.get(index).setY(y);
    }
    
    /**
     * Sets the {@code x} coordinate of the data point at the specified position in
     * this time series.
     * 
     * @param index index of the data point whose {@code y} coordinate to replace
     * @param x     the new value of the {@code x} coordinate of the data point at
     *              the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public void setX(int index, double x) {
        this.get(index).setX(x);
    }
    
    /**
     * Sets the {@code x} and {@code y} coordinates of the data point at the
     * specified position in this time series.
     * 
     * @param index index of the data point whose {@code y} coordinate to replace
     * @param x     the new value of the {@code x} coordinate of the data point at
     *              the specified position
     * @param y     the new value of the {@code y} coordinate of the data point at
     *              the specified position
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
     * Inserts a new data point with the specified {@code x} and {@code y}
     * coordinates at the specified position in this time series. Shifts the data
     * point currently at that position (if any) and any subsequent data points to
     * the right (adds one to their indices).
     * 
     * <p>
     * The same as {@code add(index, new DataPoint(x, y))}.
     * 
     * @param index index at which the specified data point is to be inserted
     * @param x     the value of the x coordinate
     * @param y     the value of the y coordinate
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
     * Returns a new time series containing the specified {@code y} values utilizing
     * an {@code ArrayList} for storing data points, with default {@link #label} and
     * {@link #index} values.
     * 
     * <p>
     * The {@code x} coordinate of the first point will be zero (0), and for each
     * subsequent point it will increase by one (1).
     * 
     * @param yValues the {@code y} coordinates of the data points
     * @return a new time series containing the specified {@code y} values
     */
    public static TimeSeries of(double... yValues) {
        return of(false, yValues);
    }

    /**
     * Returns a new time series containing the specified {@code y} values utilizing
     * an {@code ArrayList} or a {@code LinkedList} for storing data points,
     * depending on the {@code linkedList} parameter; with default {@link #label}
     * and {@link #index} values.
     * 
     * <p>
     * The {@code x} coordinate of the first point will be zero (0), and for each
     * subsequent point it will increase by one (1).
     * 
     * @param linkedList
     * @param yValues    the {@code y} coordinates of the data points
     * @return a new time series containing the specified {@code y} values
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

}

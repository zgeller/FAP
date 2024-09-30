package fap.distance.util;

import java.io.Serializable;

import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Auxiliary class used by Itakura-constrained distance measures to generate and
 * store Itakura parallelograms.
 * 
 * <p>
 * References:
 * <ol>
 * <li> F. Itakura, Minimum prediction residual principle applied to speech
 *      recognition, IEEE Trans. Acoust. 23 (1975) 67–72. 
 *      <a href="https://doi.org/10.1109/TASSP.1975.1162641">
 *         https://doi.org/10.1109/TASSP.1975.1162641</a>.
 * </ol>
 * 
 * @author Zoltan Geller
 * @version 2024.09.01.
 * @see Serializable
 */
public class ItakuraParallelogram implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The length of the time series.
     */
    private transient int length;

    /**
     * The width of the warping window.
     */
    private transient int itWidth;

    /**
     * The Itakura parallelogram. The first row ({@code sei[0]}) contains the
     * starting and the second ({@code sei[1]}) the ending indices. The first
     * elements of these rows ({@code m[0][0]} and {@code m[1][0]}) do not belong to
     * the Itakura parallelogram.
     * 
     * @see ConstraintUtils#getItakuraParallelogram(int, int)
     */
    private transient int sei[][];

    /**
     * Returns the Itakura parallelogram. The first element of the arrays aren't
     * part of the warping window; they're used internally as start helper cell. The
     * nult row contains the Starting and the first one the Ending Indicies.
     * 
     * @param series1 the first time series
     * @param series2 the second data point series
     * @param r       the relative width of the warping (editing) window
     * @param w       the absolute width of the warping (editing) window
     * @return the Itakura parallelogram
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     * @see ConstraintUtils#getWarpingWindowWidth(DataPointSeries, DataPointSeries, int, int)
     * @see ConstraintUtils#getItakuraParallelogram(int, int)                                        
     */
    public synchronized int[][] getSEI(TimeSeries series1, TimeSeries series2, double r, int w) throws IncomparableTimeSeriesException {

        int itWidth = ConstraintUtils.getWarpingWindowWidth(series1, series2, r, w);

        int len = series1.length();

        // recalculate the Itakura parallelogram only if it's necessary
        if (sei == null || this.itWidth != itWidth || length != len) {

            this.itWidth = itWidth;
            this.length = len;
            sei = ConstraintUtils.getItakuraParallelogram(len, itWidth);

        }

        return sei;
    }

}

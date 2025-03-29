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

package fap.distance.util;

import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;

/**
 * Utility class for global constraints.
 * 
 * @author Zoltán Gellér
 * @version 2025.03.06.
 */
public class ConstraintUtils {

    private ConstraintUtils() {
    }
    
    /**
     * Calculates the width of the warping (editing) window:
     * 
     * <blockquote>
     * <dl>
     * <dt>if <code>w < 0</code> then
     * <dd><code>width = len * r / 100</code>
     * <dt>else
     * <dd><code>width = len</code>
     * </dl>
     * </blockquote>
     * 
     * <blockquote><b><code>width = min{width, len}</code></b></blockquote>
     * 
     * <p>
     * where {@code len} denotes the length of the series (they must the same
     * length).
     * 
     * @param data1 the first time series
     * @param data2 the second time series
     * @param r     the relative width of the warping (editing) window
     * @param w     the absolute width of the warping (editing) window
     * @return the width of the warping (editing) window
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     */
    public static int getWarpingWindowWidth(TimeSeries series1, TimeSeries series2, double r, int w)
                      throws IncomparableTimeSeriesException {

        int len1 = series1.length();
        int len2 = series2.length();

        IncomparableTimeSeriesException.checkLength(len1, len2);

        int scWidth;
        if (w < 0)
            scWidth = (int) (len1 * r / 100);
        else
            scWidth = w;

        if (scWidth > len1)
            scWidth = len1;

        return scWidth;
        
    }
    
    /**
     * Calculates the width of the warping (editing) window using the specified
     * time-series length ({@code len}) and the relative width of the warping
     * (editing) window ({@code r}).
     * 
     * @param len the length of the time series
     * @param r   the relative width of the warping (editing) window
     * @return the width of the warping (editing) window
     */
    public static int getWarpingWindowWidth(int len, double r) {
        
        int scWidth = (int) (len * r / 100);
        
        if (scWidth > len)
            scWidth = len;
        
        return scWidth;
        
    }

    /**
     * Finds the indices of the first and last columns of the rows of the warping
     * (editing) matrix belonging to the Itakura parallelogram of specified width
     * for time series of specified length.
     * 
     * <p>
     * The result is a matrix with two rows. The first row ({@code m[0]}) contains
     * the starting and the second ({@code m[1]}) the ending indices. The first
     * elements of these rows ({@code m[0][0]} and {@code m[1][0]}) do not belong to
     * the Itakura parallelogram, they represent auxiliary cells used by the
     * algorithm.
     *
     * <p>
     * For example, the matrix <blockquote>
     * {@code m[0] = [  *, 1, 1, 2, 2, 3, 3, 3, 4, 4, 6, 9,11]}<br>
     * {@code m[1] = [  *, 2, 4, 7, 9, 9,10,10,10,11,11,12,12]} </blockquote>
     * represents the following Itakura parallelogram:
     * <blockquote><img src="doc-files/ConstraintUtils-1.png"></blockquote>
     * 
     * @param len   the length of the time series
     * @param width the absolute width of the warping window
     * @return the Itakura parallelogram
     */
    public static int[][] getItakuraParallelogram(int len, int width) {

        int x = (len - width) / 2 + 1;
        int y = len - x + 1;

        int[][] sei = new int[2][len + 1]; // sei: start and end indices
        int[] startIndex = sei[0];
        int[] endIndex = sei[1];

        startIndex[0] = 1;
        endIndex[0] = 0;

        if (x >= y) // only the cells on the diagonal

            for (int i = 1; i <= len; i++) {
                startIndex[i] = i;
                endIndex[i] = i;
            }

        else {

            double k = (double) (x - 1) / (double) (y - 1); // the slope of the line x = ky
            int end = 0;
            int count = 0;

            for (int i = 1; i < y; i++) { // the y coordinate

                /*
                 * The starting index is found following the line x - 1 = k (i-1) that passes
                 * through the points (1, 1) and (x, y) where i denotes the row number of the
                 * distortion matrix:
                 */

                int start = (int) (Math.round(k * (i - 1)) + 1); // the x coordinate
                startIndex[i] = start;

                /*
                 * The end index on the line connecting (len, len) and (len - x + 1, len - y +
                 * 1) is calculated based on the obtained starting index of the account:
                 */
                endIndex[len - i + 1] = len - start + 1;

                /*
                 * The line joining the points (1, 1) and (len - x + 1, len - y + 1) is the
                 * mirroring of the line joining the points (1, 1) and (x, y) with respect to
                 * the diagonal joining the points (1, 1) and (len, len).
                 * 
                 * The end point of the ith row differs from the end point of the (i-1)th row as
                 * much as the end point of the ith column differs from the end point of the
                 * (i-1)th column. That difference is calculated using the count variable.
                 */

                if (start == startIndex[i - 1])
                    count++;

                else {

                    end++;
                    endIndex[end] = endIndex[end - 1] + count;
                    count = 1;

                }

            }

            /*
             * The starting and ending points on the main diagonal joining the points 
             * (1, len) and (len, 1).
             */
            startIndex[y] = x;
            endIndex[len - y + 1] = len - x + 1;

            if (x != startIndex[y - 1]) {

                end++;
                endIndex[end] = endIndex[end - 1] + count;

            }

            /*
             * The line joining the points (x, y) and (len, len).            
             */
            for (int i = 1; i <= end; i++)
                startIndex[len - i + 1] = len - endIndex[i] + 1;

        }

        return sei;

    }

    /**
     * Finds the indices of the first and last columns of the rows of the warping
     * (editing) matrix belonging to the Itakura parallelogram of specified width
     * for the specified data point series.
     * 
     * @param series1 the first time series
     * @param series2 the second time series
     * @param r       the relative width of the warping (editing) window
     * @param w       the absolute width of the warping (editing) window
     * @return        the Itakura parallelogram
     * @throws IncomparableTimeSeriesException if the time series are not the same
     *                                         length
     * @see #getWarpingWindowWidth(TimeSeries, TimeSeries, int, int)
     * @see #getItakuraParallelogram(int, int)
     */
    public static int[][] getItakuraParallelogram(TimeSeries series1, TimeSeries series2, double r, int w)
                          throws IncomparableTimeSeriesException {

        int wlen = getWarpingWindowWidth(series1, series2, r, w);
        int len = series1.length();

        return getItakuraParallelogram(len, wlen);
        
    }

//    public static void main(String[] args) {
//
//        int[][] para = getItakuraParallelogram(12, 6);
//
//        for (int i = 1; i < para[0].length; i++)
//            System.out.println(para[0][i] + "," + para[1][i]);
//
//    }

}

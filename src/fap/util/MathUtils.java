/*   
 * Copyright 2024 Zoltán Gellér, Brankica Bratić
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

import java.util.List;

/**
 * Math utilities.
 * 
 * @author Zoltán Gellér, Brankica Bratić
 * @version 2025.03.05.
 */
public final class MathUtils {

    private MathUtils() {
    }

    /***
     * The value that replaces the zero denominator.
     */
    private static double zeroDenominator = 1.0e-8;

    /**
     * Returns the value that replaces the zero denominator.
     * 
     * @return the value that replaces the zero denominator
     */
    public static double getZeroDenominator() {
        return zeroDenominator;
    }

    /**
     * Sets the value that should replace the zero denominator.
     * 
     * @param zeroDenominator the value that should replace the zero denominator
     */
    public static void setZeroDenominator(double zeroDenominator) {
        MathUtils.zeroDenominator = zeroDenominator;
    }

    /**
     * Checks whether the given {@code number} is power of {@code 2}.
     * 
     * @param number number
     * @return {@code true} if {@code number} is power of {@code 2}
     */
    public static boolean isPowerOf2(int number) {
        return number < 1 ? false : (number & -number) == number;
    }

    /**
     * Returns power of two ceiling (lowest integral power of two that is equal or
     * bigger than the given {@code number}).
     * 
     * @param number the number whose power of two ceiling is to be calculated
     * @return power of two ceiling (lowest integral power of two that is equal or
     *         bigger than the given number)
     */
    public static int integralPowerOfTwoCeil(int number) {
        return (int) Math.pow(2, Math.ceil(Math.log10(number) / Math.log10(2)));
    }

    /**
     * Calculates the mean value of the numbers in the given list.
     * 
     * @param values the list of numbers whose mean value is to be calculated
     * @return the mean value of the numbers in the given list
     * @throws IllegalArgumentException if {@code values} is {@code null} or if it
     *                                  is empty
     */
    public static double mean(List<Double> values) throws IllegalArgumentException {

        if (values == null || values.size() == 0)
            throw new IllegalArgumentException("The list must not be null, and must have at least one element.");

        double sum = 0;
        for (double value : values)
            sum += value;

        return sum / values.size();
        
    }

    /**
     * Calculates the sample standard deviation of the numbers in the given list.
     * 
     * @param values the list of numbers whose sample standard deviation is to be
     *               calculated
     * @return the sample standard deviation of the numbers in the given list
     */
    public static double std(List<Double> values) {

        return std(values, mean(values));

    }

    /**
     * Calculates the sample or population standard deviation of the numbers in the
     * given list.
     * 
     * @param values     the list of numbers whose sample or population standard
     *                   deviation is to be calculated
     * @param population indicates whether the population ({@code true}) standard
     *                   deviation should be calculated
     * @return the sample or population standard deviation of the numbers in the
     *         given list
     */
    public static double std(List<Double> values, boolean population) {

        return std(values, mean(values), population);

    }

    /**
     * Calculates the sample standard deviation of the numbers in the given list
     * relying on the specified mean value.
     * 
     * @param values the list of numbers whose sample standard deviation is to be
     *               calculated
     * @param mean   the mean value of the elements of the given list (used for
     *               faster calculation in case when the mean value is already
     *               known)
     * @return the sample standard deviation of the numbers in the given list
     */
    public static double std(List<Double> values, double mean) {

        return std(values, mean, false);

    }

    /**
     * Calculates the sample or population standard deviation of the numbers in the
     * given list relying on the specified mean value.
     * 
     * @param values     the list of numbers whose sample or population standard
     *                   deviation is to be calculated
     * @param mean       the mean value of the elements of the given list (used for
     *                   faster calculation in case when the mean value is already
     *                   known)
     * @param population indicates whether the population ({@code true}) standard
     *                   deviation should be calculated
     * @return the sample or population standard deviation of the numbers in the
     *         given list
     * @throws IllegalArgumentException if {@code values == null}, or
     *                                  {@code population == true} and the list is
     *                                  empty, or {@code population == false} and
     *                                  the list contains only one element
     */
    public static double std(List<Double> values, double mean, boolean population) throws IllegalArgumentException {

        if (values == null)
            throw new IllegalArgumentException("The list must not be null.");

        int n = values.size();
        if (!population) {
            n--;
            if (n == 0)
                throw new IllegalArgumentException("The list must contain at least two elements.");
        } else if (n == 0)
            throw new IllegalArgumentException("The list must contain at least one element.");

        double sum = 0;
        for (double value : values)
            sum += Math.pow(value, 2);

        return Math.sqrt(sum / n - Math.pow(mean, 2));

    }

    /**
     * Calculates correlation between two lists of {@code double} values. The lists
     * must be of the same size.
     * 
     * @param xValues the first list
     * @param yValues the second list
     * @return correlation between the two lists
     * @throws IllegalArgumentException if {@code xValues == null}, or
     *                                  {@code yValues == null}, or
     *                                  {@code xValues.size() != yValues.size()}
     */
    public static double correlation(List<Double> xValues, List<Double> yValues) {

        if (xValues == null || yValues == null)
            throw new IllegalArgumentException("The lists cannot be null.");

        if (xValues.size() != yValues.size())
            throw new IllegalArgumentException("The lists must be of the same size.");

        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;

        int n = xValues.size();

        for (int i = 0; i < n; i++) {
            double x = xValues.get(i);
            double y = yValues.get(i);

            sx += x;
            sy += y;
            sxx += x * x;
            syy += y * y;
            sxy += x * y;
        }

        double cov = sxy / n - sx * sy / n / n; // covariation
        double sigmax = Math.sqrt(sxx / n - sx * sx / n / n); // standard error of x
        double sigmay = Math.sqrt(syy / n - sy * sy / n / n); // standard error of y

        return cov / sigmax / sigmay; // correlation is just a normalized covariation
    }

    /**
     * 
     * @param xValues
     * @param yValues
     * @return
     */
    public static ExplicitLine findRegressionLine(List<Double> xValues, List<Double> yValues) {

        double correl = correlation(xValues, yValues);

        double xMean = mean(xValues);
        double yMean = mean(yValues);

        double m = correl * (std(yValues, yMean) / std(xValues, xMean)); // slope of the regression line
        double n = yMean - m * xMean;

        return new ExplicitLine(m, n);
    }
}

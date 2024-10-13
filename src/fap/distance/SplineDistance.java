/*   
 * Copyright 2024 Aleksa Todorović, Vladimir Kurbalija, Zoltán Gellér
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

package fap.distance;

import fap.core.data.TimeSeries;
import fap.exception.IncomparableTimeSeriesException;
import fap.representation.SplineRepresentation;
import fap.util.Polynomial;

/**
 * A distance measure based on {@link SplineRepresentation}.
 *
 * <p>
 * References:
 * <ol>
 * <li> V. Kurbalija, M. Ivanović, Z. Budimac, Case-based curve behaviour
 *      prediction, Softw. Pract. Exp. 39 (2009) 81–103. 
 *      <a href="https://doi.org/10.1002/spe.891">
 *         https://doi.org/10.1002/spe.891</a>.
 * </ol>
 * 
 * @author Aleksa Todorović, Vladimir Kurbalija, Zoltán Gellér
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 * @see SplineRepresentation
 */
public class SplineDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new Spline distance measure.
     */
    public SplineDistance() {
    }
    
    /**
     * Constructs a new Spline distance measure and sets whether to store distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public SplineDistance(boolean storing) {
        super(storing);
    }

    /**
     * @throws IncomparableTimeSeriesException // TODO: add a meaningful description
     */
    @Override
    public double distance(TimeSeries series1, TimeSeries series2) {

        // try to recall the distance
        double distance = this.recall(series1, series2);
        if (!Double.isNaN(distance))
            return distance;
        
        int ithis = 0;
        int itest = 0;
        double xmin;
        double tempsim = 0;

        SplineRepresentation repr1 = series1.getRepr(SplineRepresentation.class);
        SplineRepresentation repr2 = series2.getRepr(SplineRepresentation.class);

        if ((repr1 == null) || (repr2 == null))
            throw new IncomparableTimeSeriesException(); // TODO: add a meaningful message

        if (series1.getX(0) < series2.getX(0)) {
            xmin = series2.getX(0);
            while (series1.getX(ithis) < series2.getX(0))
                ithis++;
        } else {
            xmin = series1.getX(0);
            while (series2.getX(itest) < series1.getX(0))
                itest++;
        }

        while ((ithis < series1.length()) && (itest < series2.length())) {

            if (series2.getX(itest) == series1.getX(ithis))
                // if points have same x then we are looking for the smaller
                // next point
                if ((itest < series2.length() - 1) && (ithis < series1.length() - 1)) {

                    if (series2.getX(itest + 1) == series1.getX(ithis + 1)) {
                        tempsim = tempsim
                                + Polynomial.square(Polynomial.sub(repr2.getSpline(itest), repr1.getSpline(ithis)))
                                        .integral(series1.getX(ithis), series1.getX(ithis + 1));
                        itest++;
                        ithis++;
                    }

                    else if (series2.getX(itest + 1) < series1.getX(ithis + 1)) {
                        itest++;
                    }

                    else {
                        ithis++;
                    }
                }

                else {
                    // to break while
                    itest++;
                    ithis++;
                }

            else if (series2.getX(itest) < series1.getX(ithis)) {

                if ((itest < series2.length() - 1)
                        && (series2.getX(itest + 1) < series1.getX(ithis))) {
                    tempsim = tempsim
                            + Polynomial.square(Polynomial.sub(repr2.getSpline(itest), repr1.getSpline(ithis - 1)))
                                    .integral(series2.getX(itest), series2.getX(itest + 1));
                    itest++;
                }

                else if (itest >= series2.length() - 1) {
                    itest++; // to break while
                }

                else {
                    tempsim = tempsim
                            + Polynomial.square(Polynomial.sub(repr2.getSpline(itest), repr1.getSpline(ithis - 1)))
                                    .integral(series2.getX(itest), series1.getX(ithis));
                    itest++;
                }

            }

            else {

                if ((ithis < series1.length() - 1)
                        && (series1.getX(ithis + 1) < series2.getX(itest))) {
                    tempsim = tempsim
                            + Polynomial.square(Polynomial.sub(repr2.getSpline(itest - 1), repr1.getSpline(ithis)))
                                    .integral(series1.getX(ithis), series1.getX(ithis + 1));
                    ithis++;
                }

                else if (ithis >= series1.length() - 1) {
                    ithis++; // to break while
                }

                else {
                    tempsim = tempsim
                            + Polynomial.square(Polynomial.sub(repr2.getSpline(itest - 1), repr1.getSpline(ithis)))
                                    .integral(series1.getX(ithis), series2.getX(itest));
                    ithis++;
                }
            }
        }


        if (itest >= series2.length())
            distance = tempsim / (series2.getX(series2.length() - 1) - xmin);
        else
            distance = tempsim / (series1.getX(series1.length() - 1) - xmin);
        
        // save the distance into the memory
        this.store(series1, series2, distance);

        return distance;
    }

    @Override
    public Object makeACopy(boolean deep) {
        SplineDistance copy = new SplineDistance();
        init(copy, deep);
        return copy;
    }

}

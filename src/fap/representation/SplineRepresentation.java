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

package fap.representation;

import java.util.Iterator;

import fap.core.data.DataPoint;
import fap.core.data.TimeSeries;
import fap.core.data.Representation;
import fap.util.Polynomial;

/**
 * Representation of series which uses splines.
 * 
 * <p>
 * References:
 * <ol>
 *  <li> V. Kurbalija, M. Ivanović, Z. Budimac, Case-based curve behaviour
 *       prediction, Softw. Pract. Exp. 39 (2009) 81–103. 
 *       <a href="https://doi.org/10.1002/spe.891">
 *          https://doi.org/10.1002/spe.891</a>.
 * </ol>
 * 
 * @author Aleksa Todorović, Vladimir Kurbalija, Zoltán Gellér
 * @version 2024.09.14.
 */
public class SplineRepresentation implements Representation {

    private static final long serialVersionUID = 1L;
    
    private TimeSeries data;
    private Polynomial[] splines;
    private double max = 0.0; // maximal value used for calculating saturation
    private double saturation = 0.0; // time of the saturation
    private double scale = 1.0;

    public SplineRepresentation(TimeSeries series) {
        
        this.data = series;
        this.splines = new Polynomial[series.length()];
        
        for (int i = 0; i < series.length(); i++)
            splines[i] = new Polynomial(3); // this is start cubic spline
        
        initializeSplines();
        calculateMax();
        calculateSaturation();
        
    }

    public int getSplinesCount() {
        return splines.length;
    }

    public Polynomial getSpline(int i) {
        return Polynomial.mul(scale, splines[i]);
    }

    public double getMax() {
        return max;
    }

    public double getSaturation() {
        return saturation;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    @Override
    public double getValue(double x) {
        
        // returns P(x); if x is not in the interval then returns -1000
        for (int i = 0; i < data.length() - 1; i++)
            if ((x >= data.getX(i)) && (x <= data.getX(i + 1)))
                return scale * splines[i].value(x);
        
        return OUTBOUND_VALUE;
    }

    @Override
    public Object[] getRepresentation() {
        // TODO Auto-generated method stub
        return null;
    }

    private void initializeSplines() {
        
        // main diagonale in matrix of the system
        double[] mainDiag = new double[data.length()];
        
        // first derivations in points
        double[] d = new double[data.length()];
        
        // right side of the system
        double[] rightSide = new double[data.length()];
        
        for (int i = 0; i < data.length(); i++) {
            
            if ((i == 0) || (i == data.length() - 1))
                mainDiag[i] = 2;
            else
                mainDiag[i] = 4;
            
            if (i == 0)
                rightSide[i] = 3 * (data.getX(1) - data.getX(0));
            else if (i == data.length() - 1)
                rightSide[i] = 3 * (data.getX(i) - data.getX(i - 1));
            else
                rightSide[i] = 3 * (data.getX(i + 1) - data.getX(i - 1));
            
        }
        
        for (int i = 1; i < data.length(); i++) {
            mainDiag[i] = mainDiag[i] - 1 / mainDiag[i - 1];
            rightSide[i] = rightSide[i] - rightSide[i - 1] / mainDiag[i - 1];
        }
        
        d[data.length() - 1] = rightSide[data.length() - 1] / mainDiag[data.length() - 1];
        
        for (int i = data.length() - 2; i >= 0; i--)
            d[i] = (rightSide[i] - 1) / mainDiag[i];

        // calculating the coefficients of splines t is in interval [0,1]
        for (int i = 0; i < data.length() - 1; i++) {
            splines[i].coefficients[0] = data.getY(i);
            splines[i].coefficients[1] = d[i];
            splines[i].coefficients[2] = 3 * (data.getY(i + 1) - data.getY(i)) - 2 * d[i] - d[i + 1];
            splines[i].coefficients[3] = 2 * (data.getY(i) - data.getY(i + 1)) + d[i] + d[i + 1];
        }

        // making substitions t=(x-xa)/(xb-xa) in every spline so that x is in
        // the interval [start,end]
        for (int i = 0; i < data.length() - 1; i++) {
            Polynomial temp = new Polynomial(1);
            temp.coefficients[0] = -data.getX(i) / (data.getX(i + 1) - data.getX(i));
            temp.coefficients[1] = 1 / (data.getX(i + 1) - data.getX(i));
            splines[i] = Polynomial.PofP(splines[i], temp);
        }
        
    }

    void calculateMax() {
        
        boolean first = true;
        
        for (Iterator<DataPoint> iterator = data.iterator(); iterator.hasNext();) {
            
            double y = iterator.next().getY();
            
            if (first) {
                this.max = y;
                first = false;
            } else if (getMax() < y)
                this.max = y;
            
        }
        
    }

    void calculateSaturation() {
        
        int j = data.length() - 1;
        double last = data.getY(j);
        
        while ((j > 0) && (data.getY(j - 1) > 0.98 * last) && (data.getY(j - 1) < 1.02 * last))
            j--;

        this.saturation = data.getX(j);
        
    }

}

/*   
 * Copyright 2024-2025 Vladimir Kurbalija, Aleksa Todorović, Zoltán Gellér
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

/**
 * Polynomial with coefficients which are doubles.
 * 
 * @author Vladimir Kurbalija, Aleksa Todorović, Zoltán Gellér
 * @version 2025.03.06.
 */
public class Polynomial {

    /**
     * Coefficients of polynomial. Power of polynomial is one less than the number
     * of coefficients.
     */
    public double[] coefficients;

    /**
     * Empty constructor.
     */
    public Polynomial() {
        coefficients = new double[1];
    }

    /**
     * Constructor of new polynomial with specified power.
     * 
     * @param power requested power of polynomial
     */
    public Polynomial(int power) {
        if (power >= 0) {
            coefficients = new double[power + 1];
        } else {
            coefficients = null;
        }
    }

    /**
     * Textual representation of polynomial;
     */
    @Override
    public String toString() {
        String str = Double.toString(coefficients[0]);
        for (int i = 1; i < coefficients.length; i++) {
            str = Double.toString(coefficients[i]) + "x^" + i + " + " + str;
        }
        return str;
    }

    /**
     * Calculates inverse polynomial.
     * 
     * @param p polynomial to invert
     * @return inverse polynomial
     */
    public static Polynomial inv(Polynomial p) {
        if (p != null) {
            Polynomial result = new Polynomial(p.coefficients.length - 1);
            for (int i = 0; i < p.coefficients.length; i++) {
                result.coefficients[i] = -p.coefficients[i];
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Calculates sum of two polynomials.
     * 
     * @param p1 first polynomial
     * @param p2 second polynomial
     * @return sum of two polynomials
     */
    public static Polynomial add(Polynomial p1, Polynomial p2) {
        if ((p1 != null) && (p2 != null)) {
            if (p1.coefficients.length > p2.coefficients.length) {
                Polynomial result = new Polynomial(p1.coefficients.length - 1);
                for (int i = 0; i < p1.coefficients.length; i++) {
                    result.coefficients[i] = p1.coefficients[i];
                }
                for (int i = 0; i < p2.coefficients.length; i++) {
                    result.coefficients[i] += p2.coefficients[i];
                }
                return result;
            } else {
                Polynomial result = new Polynomial(p2.coefficients.length - 1);
                for (int i = 0; i < p2.coefficients.length; i++) {
                    result.coefficients[i] = p2.coefficients[i];
                }
                for (int i = 0; i < p1.coefficients.length; i++) {
                    result.coefficients[i] += p1.coefficients[i];
                }
                return result;
            }
        } else if (p2 != null) {
            Polynomial result = new Polynomial(p2.coefficients.length - 1);
            for (int i = 0; i < p2.coefficients.length; i++) {
                result.coefficients[i] = p2.coefficients[i];
            }
            return result;
        } else if (p1 != null) {
            Polynomial result = new Polynomial(p1.coefficients.length - 1);
            for (int i = 0; i < p1.coefficients.length; i++) {
                result.coefficients[i] = p1.coefficients[i];
            }
            return result;
        } else
            return null;
    }

    /**
     * Calculates difference of two polynomials.
     * 
     * @param p1 first polynomial
     * @param p2 second polynomial
     * @return difference of two polynomials
     */
    public static Polynomial sub(Polynomial p1, Polynomial p2) {
        return Polynomial.add(p1, Polynomial.inv(p2));
    }

    /**
     * Calculates product of polynomial and constant.
     * 
     * @param p polynomial
     * @param c constant
     * @return product of polynomial and constant
     */
    public static Polynomial mul(Polynomial p, double c) {
        return mul(c, p);
    }

    /**
     * Calculates product of polynomial and constant.
     * 
     * @param c constant
     * @param p polynomial
     * @return product of polynomial and constant
     */
    public static Polynomial mul(double c, Polynomial p) {
        if ((p != null) && (c != 0.0)) {
            Polynomial result = new Polynomial(p.coefficients.length - 1);
            for (int i = 0; i < p.coefficients.length; i++) {
                result.coefficients[i] = p.coefficients[i] * c;
            }
            return result;
        } else
            return null;
    }

    /**
     * Calculates product of two polynomials.
     * 
     * @param p1 first polynomial
     * @param p2 second polynomial
     * @return product of two polynomials
     */
    public static Polynomial mul(Polynomial p1, Polynomial p2) {
        if ((p1 != null) && (p2 != null)) {
            Polynomial result = new Polynomial(p1.coefficients.length + p2.coefficients.length - 2);
            for (int i = 0; i < p1.coefficients.length; i++) {
                for (int j = 0; j < p2.coefficients.length; j++) {
                    result.coefficients[i + j] += p1.coefficients[i] * p2.coefficients[j];
                }
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Calculates p1(p2(x))
     * 
     * @param p1 "outer" polynomial
     * @param p2 "inner" polynomial
     * @return p1(p2(x))
     */
    public static Polynomial PofP(Polynomial p1, Polynomial p2) {
        if (p1 != null) {
            Polynomial result = new Polynomial(0);
            result.coefficients[0] = p1.coefficients[0];
            if (p2 != null) {
                Polynomial temp = new Polynomial(p2.coefficients.length - 1);
                for (int i = 0; i < p2.coefficients.length; i++) {
                    temp.coefficients[i] = p2.coefficients[i];
                }
                for (int i = 1; i < p1.coefficients.length; i++) {
                    result = add(result, mul(p1.coefficients[i], temp));
                    temp = mul(temp, p2);
                }
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Calculates square of polynomial.
     * 
     * @param p polynomial which square is to be calculated
     * @return square of polynomial
     */
    public static Polynomial square(Polynomial p) {
        return mul(p, p);
    }

    /**
     * Evaluates polynomial for specified value of x.
     * 
     * @param x scalar
     * @return value of polynomial for specified scalar
     */
    public double value(double x) {
        double temp = 1;
        double res = 0;
        for (double coef: coefficients) {
            res += coef * temp;
            temp = temp * x;
        }
        return res;
    }

    /**
     * Value of definite integral of polynomial with lower bound start and upper
     * bound end.
     * 
     * @param start lower bound
     * @param end   upper bound
     * @return definite integral
     */
    public double integral(double a, double b) {
        if (coefficients != null) {
            Polynomial result = new Polynomial(this.coefficients.length);
            for (int i = 1; i < result.coefficients.length; i++) {
                result.coefficients[i] = this.coefficients[i - 1] / i;
            }
            return result.value(b) - result.value(a);
        } else {
            return 0;
        }
    }

}

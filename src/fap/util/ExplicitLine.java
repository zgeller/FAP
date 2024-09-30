package fap.util;

import fap.core.data.DataPoint;

/**
 * Model class for the explicit equation of line.
 * 
 * @author Brankica Bratic
 * @version 2017.03.15.
 */
public class ExplicitLine {

    /**
     * Slope of the line.
     */
    private double m;

    /**
     * Y intercept (the y coordinate of the location where the line crosses the y
     * axis).
     */
    private double n;

    /**
     * Creates a new line.
     * 
     * @param m slope of the line
     * @param n y intercept (the y coordinate of the location where the line crosses
     *          the y axis)
     */
    public ExplicitLine(double m, double n) {
        this.m = m;
        this.n = n;
    }

    /**
     * Creates a new line that crosses two given points.
     * 
     * @param point1 first point that line crosses
     * @param point2 second point that line crosses
     */
    public ExplicitLine(DataPoint point1, DataPoint point2) {
        this(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    /**
     * Creates a new line that crosses two given points.
     * 
     * @param x1 x coordinate of the first point
     * @param y1 y coordinate of the first point
     * @param x2 x coordinate of the second point
     * @param y2 y coordinate of the second point
     */
    public ExplicitLine(double x1, double y1, double x2, double y2) {
        m = (y1 - y2) / (x1 - x2);
        n = y1 - m * x1;
    }

    /**
     * Returns slope of the line.
     * 
     * @return slope of the line
     */
    public double getM() {
        return m;
    }

    /**
     * Returns y intercept.
     * 
     * @return y intercept
     */
    public double getN() {
        return n;
    }

    /**
     * Returns y value for the given x.
     * 
     * @param x x value for which resulting y value is calculated
     * @return y value for the given x
     */
    public double getY(double x) {
        return m * x + n;
    }

    @Override
    public String toString() {
        return "y = " + m + "x + " + n;
    }

}

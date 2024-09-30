package fap.util;

import fap.core.data.DataPoint;

/**
 * Line representation class.
 * 
 * @author Miklos Kalozi, Zoltan Geller
 * @version 2013.09.26.
 */
public class Line {

    /**
     * Coefficient of the line represented as Ax+By+C=0.
     */
    private double A, B, C;

    /**
     * Start data point of the line.
     */
    public DataPoint startdp;

    /**
     * End data point of the line.
     */
    public DataPoint enddp;

    /**
     * Empty constructor.
     */
    public Line() {
    }

    /**
     * Copy constructor.
     * 
     * @param line the line
     */
    public Line(Line line) {
        copy(line);
    }

    /**
     * Fits the line between two points.
     * 
     * @param startdp the start point
     * @param enddp   the end point
     */
    public void update(final DataPoint startdp, final DataPoint enddp) {
        this.startdp = startdp;
        this.enddp = enddp;
        A = enddp.getY() - startdp.getY();
        B = startdp.getX() - enddp.getX();
        C = -startdp.getX() * A - startdp.getY() * B;
    }

    /**
     * Updates the line using the given slope, y-intercept and the x coordinates of
     * the start and end point.
     * 
     * @param startx x coordinate of the start point
     * @param endx   x coordinates of the end point
     * @param beta   the slope (gradient) of the line
     * @param alpha  the y-intercept of the line
     */
    public void update(double startx, double endx, double beta, double alpha) {
        B = -1;
        A = beta;
        C = alpha;
        startdp = new DataPoint(startx, beta * startx + alpha);
        enddp = new DataPoint(endx, beta * endx + alpha);
    }

    /**
     * The value of y for the given x so that (x,y) is on this line.
     * 
     * @param x the x coordinate
     * @return the value of the y coordinate so that (x,y) is on this line
     */
    public double getY(double x) {
        return -(A * x + C) / B;
    }

    /**
     * Copies the parameters of the given line.
     * 
     * @param line the line
     */
    public void copy(Line line) {
        this.A = line.A;
        this.B = line.B;
        this.C = line.C;
        this.startdp = new DataPoint(line.startdp);
        this.enddp = new DataPoint(line.enddp);
    }

}
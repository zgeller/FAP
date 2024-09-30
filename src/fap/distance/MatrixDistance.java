package fap.distance;

import fap.core.data.TimeSeries;

/**
 * A distance measure that relies on a distance matrix and the indices of time
 * series.
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 */

public class MatrixDistance extends AbstractCopyableDistance {

    private static final long serialVersionUID = 1L;

    /**
     * The matrix containing the distances.
     */
    private transient double[][] distances;

    /**
     * Constructs a new Matrix distance measure.
     */
    public MatrixDistance() {
    }

    @Override
    public double distance(TimeSeries series1, TimeSeries series2) {

        int y = series1.getIndex();
        int x = series2.getIndex();

        if (distances[y].length > x)
            return distances[y][x];
        else
            return distances[x][y];

    }

    /**
     * Returns the matrix containing the distances.
     * 
     * @return the matrix containing the distances
     */
    public double[][] getDistances() {
        return distances;
    }

    /**
     * Sets the matrix containing the distances.
     * 
     * @param distances the matrix containing the distances
     */
    public void setDistances(double[][] distances) {
        this.distances = distances;
    }

    /**
     * Initializes the specified distance measure with the common data structures of this
     * distance.
     * 
     * @param copy the distance measure whose data structures is to be initialized
     */
    protected void init(MatrixDistance copy, boolean deep) {
        super.init(copy, deep);
        copy.setDistances(this.getDistances());
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        MatrixDistance copy = new MatrixDistance();
        init(copy, deep);
        return copy;
    }

}

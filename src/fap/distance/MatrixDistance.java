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

package fap.distance;

import fap.core.data.TimeSeries;

/**
 * A distance measure that relies on a distance matrix and the indices of time
 * series.
 * 
 * @author Zoltán Gellér
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

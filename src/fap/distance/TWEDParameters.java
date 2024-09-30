package fap.distance;

/**
 * Declares getters and setters for the parameters of TWED-based distance
 * measures.
 * 
 * @author Zoltan Geller
 * @version 2024.09.24.
 */
public interface TWEDParameters {
    
    /**
     * Returns the stiffness of the measure.
     * 
     * @return the stiffness of the measure
     */
    public double getNu();
    
    /**
     * Sets the value that is to be used to control the stiffness of the measure.
     * 
     * @param nu the value that is to be used to control the stiffness of the
     *           measure
     */
    public void setNu(double nu);

    /**
     * Returns the value used to calculate penalties for insert and delete
     * operations.
     * 
     * @return the value used to calculate penalties for insert and delete
     *         operations
     */
    public double getLambda();
    
    
    /**
     * Sets the the value that is to be used to calculate penalties for insert and
     * delete operations. Must be {@code lambda >= 0}.
     * 
     * @param lambda the value that is to be used to calculate penalties for insert
     *               and delete operations
     */
    public void setLambda(double lambda);

}

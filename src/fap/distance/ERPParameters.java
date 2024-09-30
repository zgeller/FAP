package fap.distance;

/**
 * Declares getters and setters for the parameters of ERP-based distance
 * measures.
 * 
 * @author Zoltan Geller
 * @version 2024.09.24.
 */
public interface ERPParameters {

    /**
     * Returns the value used to calculate the penalty for gaps.
     * 
     * @return the value used to calculate the penalty for gaps
     */
    public double getG();
    
    /**
     * Sets the value that is to be used to calculate the penalty for gaps.
     * 
     * @param g the value that is to be used to calculate the penalty for gaps
     */
    public void setG(double g);
    
}

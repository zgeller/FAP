/**
 * 
 */
package fap.util;

/**
 * Declares common methods for multithreaded classes.
 * 
 * @author Zoltan Geller
 * @version 2024.09.01.
 *
 */
public interface Multithreaded {

    /**
     * Sets the number of threads.
     * 
     * @param tnumber the number of threads
     */
    public void setNumberOfThreads(int tnumber);

    /**
     * Returns the number of threads.
     * 
     * @return the number of threads
     */
    public int getNumberOfThreads();
    
    /**
     * Shuts downs any executors and threads.
     */
    public void shutdown();

}

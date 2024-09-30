package fap.util;

/**
 * Declares common methods for copyable objects.
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 */
public interface Copyable {

    /**
     * Makes a new instance of the given class and initializes it with the
     * parameters of this object. It implies a deep copy.
     * 
     * @param deep indicates whether a deep copy should be made
     * @return a new instance of the given class initialized with the parameters of
     *         this object
     */
    public default Object makeACopy() {
        return makeACopy(true);
    }
    
    /**
     * Makes a new instance of the given class and initializes it with the
     * parameters of this object.
     * 
     * @param deep indicates whether a deep copy should be made
     * @return a new instance of the given class initialized with the parameters of
     *         this object
     */
    public Object makeACopy(boolean deep);

}

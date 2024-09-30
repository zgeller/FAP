package fap.trainer;

/**
 * Auxiliary interface for distance measure parameter setters.
 * 
 * @param <T> the type of the parameter to be set by this parameter setter
 * 
 * @author Zoltan Geller
 * @version 2024.09.23.
 * @see Modifier
 */
public interface DistanceModifier<T> extends Modifier<T> {

    @Override
    public default boolean affectsDistance() {
        return true;
    }
    
}

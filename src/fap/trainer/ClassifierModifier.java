package fap.trainer;

/**
 * Auxiliary interface for classifier parameter setters that do not affects the
 * underlying distance measure.
 * 
 * @param <T> the type of the parameter to be set by this parameter setter
 * 
 * @author Zoltan Geller
 * @version 2024.09.23.
 * @see Modifier
 */
public interface ClassifierModifier<T> extends Modifier<T> {

    @Override
    public default boolean affectsDistance() {
        return false;
    }
    
}

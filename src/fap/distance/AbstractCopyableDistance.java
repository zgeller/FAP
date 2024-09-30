package fap.distance;

import fap.core.distance.AbstractDistance;
import fap.util.Copyable;

/**
 * Defines common methods and fields for copyable distance measures.
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractDistance
 * @see Copyable
 */
public abstract class AbstractCopyableDistance extends AbstractDistance implements Copyable {

    private static final long serialVersionUID = 1L;

    /**
     * Empty constructor.
     */
    public AbstractCopyableDistance() {
    }
    
    /**
     * Constructor with the possibility to enable or disable storing distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractCopyableDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Initializes the specified distance measure with the common data structures of this
     * distance.
     * 
     * @param copy the distance measure whose data structures is to be initialized
     */
    protected void init(AbstractCopyableDistance copy, boolean deep) {
        copy.setStoring(this.isStoring());
    }

}

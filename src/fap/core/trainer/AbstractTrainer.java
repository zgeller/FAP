package fap.core.trainer;

/**
 * Defines common methods and fields for classifier trainers.
 * 
 * @author Zoltan Geller
 * @version 2024.09.23.
 * @see Trainer
 */
public abstract class AbstractTrainer implements Trainer {

    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates whether this trainer affects the distance measure.
     */
    protected boolean affectsDistance;

    /**
     * The expected error rate. Default value is {@code Double.POSITIVE_INFINITY}.
     */
    protected double expectedError = Double.POSITIVE_INFINITY;

    /**
     * Empty constructor.
     */
    public AbstractTrainer() {
    }
    
    /**
     * Constructor that sets whether the trainer affects the distance measure.
     * 
     * @param affectsDistance {@code true} if the trainer affacts the distance
     *                        measure
     */
    public AbstractTrainer(boolean affectsDistance) {
        this.setAffectsDistance(affectsDistance);
    }
    
    public void setAffectsDistance(boolean affectsDistance) {
        this.affectsDistance = affectsDistance;
    }

    @Override
    public double getExpectedError() {
        return expectedError;
    }
    
    @Override
    public boolean affectsDistance() {
        return affectsDistance;
    }
    
    /**
     * Initializes the specified trainer with the common data structures of this
     * trainer.
     * 
     * @param copy the classifier whose data structures is to be initialized
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(AbstractTrainer copy, boolean deep) {
        copy.setAffectsDistance(this.affectsDistance);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

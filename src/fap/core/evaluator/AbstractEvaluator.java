package fap.core.evaluator;

/**
 * Defines common methods and fields for classifier evaluators.
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see Evaluator
 */
public abstract class AbstractEvaluator implements Evaluator {

    private static final long serialVersionUID = 1L;

    /**
     * Average error rate. Default value is {@code 0.0}.
     */
    protected double error;

    /**
     * Number of misclassified time series. Default value is {@code 0}.
     */
    protected int misclassified;

    /**
     * Empty constructor.
     */
    public AbstractEvaluator() {
    }
    
    @Override
    public double getError() {
        return error;
    }

    @Override
    public int getMisclassified() {
        return misclassified;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName();
    }

}

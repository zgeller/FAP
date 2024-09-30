package fap.trainer;

import java.util.ArrayList;
import java.util.List;

/**
 * General trainer of {@code Double} parameters.
 * 
 * @author Zoltan Geller
 * @version 2024.09.24.
 * @see AbstractParameterTrainer
 * @see Modifier
 */
public class DoubleTrainer extends AbstractParameterTrainer<Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new single-threaded uninitialized Double parameter trainer.
     */
    public DoubleTrainer() {
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with the specified
     * parameter modifier.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                    of the parameter tuned by the trainer
     */
    public DoubleTrainer(Modifier<Double> modifier) {
        super(modifier);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads.
     * 
     * @param tnumber number of threads.
     */
    public DoubleTrainer(int tnumber) {
        super(tnumber);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads and the parameter modifier.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param tnumber  number of threads.
     */
    public DoubleTrainer(Modifier<Double> modifier, int tnumber) {
        super(modifier, tnumber);
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with parameter
     * values between {@code first} and {@code last}, in increments of 1.
     * 
     * @param first the first value to be evaluated, must be {@code first <= last}
     * @param last  the first value to be evaluated, must be {@code first <= last}
     */
    public DoubleTrainer(Double first, Double last) {
        this.setValues(first, last);
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with the specified
     * parameter modifier, and parameter values between {@code first} and
     * {@code last}, in increments of 1.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param first    the first value to be evaluated, must be
     *                 {@code first <= last}
     * @param last     the first value to be evaluated, must be
     *                 {@code first <= last}
     */
    public DoubleTrainer(Modifier<Double> modifier, Double first, Double last) {
        super(modifier);
        this.setValues(first, last);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads and parameter values between {@code first} and {@code last}, in
     * increments of 1.
     * 
     * @param first   the first value to be evaluated, must be {@code first <= last}
     * @param last    the first value to be evaluated, must be {@code first <= last}
     * @param tnumber number of threads.
     */
    public DoubleTrainer(Double first, Double last, int tnumber) {
        super(tnumber);
        this.setValues(first, last);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads, the parameter modifier, and parameter values between {@code first}
     * and {@code last}, in increments of 1.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param first    the first value to be evaluated, must be
     *                 {@code first <= last}
     * @param last     the first value to be evaluated, must be
     *                 {@code first <= last}
     * @param tnumber  number of threads.
     */
    public DoubleTrainer(Modifier<Double> modifier, Double first, Double last, int tnumber) {
        super(modifier, tnumber);
        this.setValues(first, last);
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with parameter
     * values between {@code first} and {@code last}, in increments of
     * {@code increment}.
     * 
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     */
    public DoubleTrainer(Double first, Double last, Double increment) {
        this.setValues(first, last, increment);
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with the specified
     * parameter modifier, and parameter values between {@code first} and
     * {@code last}, in increments of {@code increment}.
     * 
     * @param modifier  the parameter modifier, which is to be used to set the value
     *                  of the parameter tuned by the trainer
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     */
    public DoubleTrainer(Modifier<Double> modifier, Double first, Double last, Double increment) {
        super(modifier);
        this.setValues(first, last, increment);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads and parameter values between {@code first} and {@code last}, in
     * increments of {@code increment}.
     * 
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     * @param tnumber   number of threads.
     */
    public DoubleTrainer(Double first, Double last, Double increment, int tnumber) {
        super(tnumber);
        this.setValues(first, last, increment);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads, the parameter modifier, and parameter values between {@code first}
     * and {@code last}, in increments of {@code increment}.
     * 
     * @param modifier  the parameter modifier, which is to be used to set the value
     *                  of the parameter tuned by the trainer
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     * @param tnumber   number of threads.
     */
    public DoubleTrainer(Modifier<Double> modifier, Double first, Double last, Double increment, int tnumber) {
        super(modifier, tnumber);
        this.setValues(first, last, increment);
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with the specified
     * parameter values.
     * 
     * @param values the list of values to be evaluated
     */
    public DoubleTrainer(List<Double> values) {
        super(values);
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with the specified
     * parameter modifier and the parameter values.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param values   the list of values to be evaluated
     */
    public DoubleTrainer(Modifier<Double> modifier, List<Double> values) {
        super(modifier, values);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads, and the parameter values.
     * 
     * @param values  the list of values to be evaluated
     * @param tnumber number of threads.
     */
    public DoubleTrainer(List<Double> values, int tnumber) {
        super(values, tnumber);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads, the parameter modifier, and the parameter values.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param values   the list of values to be evaluated
     * @param tnumber  number of threads.
     */
    public DoubleTrainer(Modifier<Double> modifier, List<Double> values, int tnumber) {
        super(modifier, values, tnumber);
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with the specified
     * parameter values.
     * 
     * @param values the array of values to be evaluated
     */
    public DoubleTrainer(Double[] values) {
        super(values);
    }

    /**
     * Constructs a new single-threaded Double parameter trainer with the specified
     * parameter modifier, and the parameter values.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param values   the array of values to be evaluated
     */
    public DoubleTrainer(Modifier<Double> modifier, Double[] values) {
        super(modifier, values);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads, and the parameter values.
     * 
     * @param values  the array of values to be evaluated
     * @param tnumber number of threads.
     */
    public DoubleTrainer(Double[] values, int tnumber) {
        super(values, tnumber);
    }

    /**
     * Constructs a new Double parameter trainer with the specified number of
     * threads, the parameter modifier, and the parameter values.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the trainer
     * @param values   the array of values to be evaluated
     * @param tnumber  number of threads.
     */
    public DoubleTrainer(Modifier<Double> modifier, Double[] values, int tnumber) {
        super(modifier, values, tnumber);
    }

    @Override
    public void setValues(Double first, Double last) throws IllegalArgumentException {
        this.setValues(first, last, 1d);
    }

    @Override
    public void setValues(Double first, Double last, Double increment) throws IllegalArgumentException {

        super.setValues(first, last, increment);

        int len = (int) ((last - first) / increment) + 1;

        values = new ArrayList<>(len);
        for (int i = 0; i < len; i++)
            values.add(first + i * increment);

    }
    
    /**
     * Initializes the specified trainer with the common data structures of this
     * trainer.
     * 
     * @param copy the classifier whose data structures is to be initialized
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(DoubleTrainer copy, boolean deep) {
        super.init(copy, deep);
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        DoubleTrainer copy = new DoubleTrainer();
        this.init(copy, deep);
        return copy;
    }

}

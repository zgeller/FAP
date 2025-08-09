/*   
 * Copyright 2024-2025 Zoltán Gellér
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fap.tuner;

import java.util.ArrayList;
import java.util.List;

/**
 * General tuner of {@code Integer} parameters.
 * 
 * @author Zoltán Gellér
 * @version 2025.04.22.
 * @see AbstractParameterTuner
 * @see Modifier
 */
public class IntegerTuner extends AbstractParameterTuner<Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new single-threaded uninitialized {@code Integer} parameter tuner.
     */
    public IntegerTuner() {
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with the specified
     * parameter modifier.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the tuner
     */
    public IntegerTuner(Modifier<Integer> modifier) {
        super(modifier);
    }

    /**
     * Constructs a new {@code Integer} parameter tuner with the specified number of
     * threads.
     * 
     * @param tnumber number of threads.
     */
    public IntegerTuner(int tnumber) {
        super(tnumber);
    }

    /**
     * Constructs a new {@code Integer} parameter tuner with the specified number of
     * threads and the parameter modifier.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the tuner
     * @param tnumber  number of threads.
     */
    public IntegerTuner(Modifier<Integer> modifier, int tnumber) {
        super(modifier, tnumber);
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with parameter
     * values between {@code first} and {@code last}, in increments of 1.
     * 
     * @param first the first value to be evaluated, must be {@code first <= last}
     * @param last  the first value to be evaluated, must be {@code first <= last}
     */
    public IntegerTuner(Integer first, Integer last) {
        this.setValues(first, last);
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with the specified
     * parameter modifier, and parameter values between {@code first} and
     * {@code last}, in increments of 1.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the tuner
     * @param first    the first value to be evaluated, must be
     *                 {@code first <= last}
     * @param last     the first value to be evaluated, must be
     *                 {@code first <= last}
     */
    public IntegerTuner(Modifier<Integer> modifier, Integer first, Integer last) {
        super(modifier);
        this.setValues(first, last);
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with parameter
     * values between {@code first} and {@code last}, in increments of
     * {@code increment}.
     * 
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     */
    public IntegerTuner(Integer first, Integer last, Integer increment) {
        this.setValues(first, last, increment);
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with the specified
     * parameter modifier, and parameter values between {@code first} and
     * {@code last}, in increments of {@code increment}.
     * 
     * @param modifier  the parameter modifier, which is to be used to set the value
     *                  of the parameter tuned by the tuner
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     */
    public IntegerTuner(Modifier<Integer> modifier, Integer first, Integer last, Integer increment) {
        super(modifier);
        this.setValues(first, last, increment);
    }

    /**
     * Constructs a new {@code Integer} parameter tuner with the specified number of
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
    public IntegerTuner(Integer first, Integer last, Integer increment, int tnumber) {
        super(tnumber);
        this.setValues(first, last, increment);
    }

    /**
     * Constructs a new {@code Integer} parameter tuner with the specified number of
     * threads, the parameter modifier, and parameter values between {@code first}
     * and {@code last}, in increments of {@code increment}.
     * 
     * @param modifier  the parameter modifier, which is to be used to set the value
     *                  of the parameter tuned by the tuner
     * @param first     the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param last      the first value to be evaluated, must be
     *                  {@code first <= last}
     * @param increment the increment
     * @param tnumber   number of threads.
     */
    public IntegerTuner(Modifier<Integer> modifier, Integer first, Integer last, Integer increment, int tnumber) {
        super(modifier, tnumber);
        this.setValues(first, last, increment);
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with the specified
     * parameter values.
     * 
     * @param values the list of values to be evaluated
     */
    public IntegerTuner(List<Integer> values) {
        super(values);
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with the specified
     * parameter modifier.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the tuner
     * @param values   the list of values to be evaluated
     */
    public IntegerTuner(Modifier<Integer> modifier, List<Integer> values) {
        super(modifier, values);
    }

    /**
     * Constructs a new {@code Integer} parameter tuner with the specified number of
     * threads, and the parameter values.
     * 
     * @param values  the list of values to be evaluated
     * @param tnumber number of threads.
     */
    public IntegerTuner(List<Integer> values, int tnumber) {
        super(values, tnumber);
    }

    /**
     * Constructs a new {@code Integer} parameter tuner with the specified number of
     * threads, the parameter modifier, and the parameter values.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the tuner
     * @param values   the list of values to be evaluated
     * @param tnumber  number of threads.
     */
    public IntegerTuner(Modifier<Integer> modifier, List<Integer> values, int tnumber) {
        super(modifier, values, tnumber);
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with the specified
     * parameter values.
     * 
     * @param values the array of values to be evaluated
     */
    public IntegerTuner(Integer[] values) {
        super(values);
    }

    /**
     * Constructs a new single-threaded {@code Integer} parameter tuner with the specified
     * parameter modifier, and the parameter values.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the tuner
     * @param values   the array of values to be evaluated
     */
    public IntegerTuner(Modifier<Integer> modifier, Integer[] values) {
        super(modifier, values);
    }

    /**
     * Constructs a new {@code Integer} parameter tuner with the specified number of
     * threads, and the parameter values.
     * 
     * @param values  the array of values to be evaluated
     * @param tnumber number of threads.
     */
    public IntegerTuner(Integer[] values, int tnumber) {
        super(values, tnumber);
    }

    /**
     * Constructs a new {@code Integer} parameter tuner with the specified number of
     * threads, the parameter modifier, and the parameter values.
     * 
     * @param modifier the parameter modifier, which is to be used to set the value
     *                 of the parameter tuned by the tuner
     * @param values   the array of values to be evaluated
     * @param tnumber  number of threads.
     */
    public IntegerTuner(Modifier<Integer> modifier, Integer[] values, int tnumber) {
        super(modifier, values, tnumber);
    }

    @Override
    public void setValues(Integer first, Integer last) throws IllegalArgumentException {
        this.setValues(first, last, 1);
    }

    @Override
    public void setValues(Integer first, Integer last, Integer increment) throws IllegalArgumentException {

        super.setValues(first, last, increment);

        int len = (last - first) / increment + 1;

        values = new ArrayList<>(len);
        for (int i = 0; i < len; i++)
            values.add(first + i * increment);

    }

    /**
     * Initializes the specified tuner with the common data structures of this
     * tuner.
     * 
     * @param copy the classifier whose data structures is to be initialized
     * @param deep indicates whether a deep copy should be made
     */
    protected void init(IntegerTuner copy, boolean deep) {
        super.init(copy, deep);
    }
    
    @Override
    public Object makeACopy(boolean deep) {
        IntegerTuner copy = new IntegerTuner();
        this.init(copy, deep);
        return copy;
    }

}

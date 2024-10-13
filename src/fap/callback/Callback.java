/*   
 * Copyright 2024 Zoltán Gellér
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

package fap.callback;

/**
 * Declares common methods for callback objects.
 *
 * <p>
 * Callbackable operations should call the {@link #callback(Object)
 * callback} method of the specified {@code Callback} object at regular
 * intervals.
 * 
 * <p>
 * The {@code Callback} object reports the desired number of callbacks via the
 * {@link #getDesiredCallbackNumber()} method.
 * 
 * <p>
 * Depending on the total number of steps, the callbackable operation can report
 * the possible number of callbacks via the
 * {@link #setPossibleCallbackNumber(int) setPossibleCallbackNumber}.
 * 
 * @author Zoltán Gellér
 * @version 2024.08.18.
 * @see Callbackable
 */
public interface Callback {

    /**
     * Returns the desired number of callbacks.
     * 
     * @return the desired number of callbacks
     */
    public int getDesiredCallbackNumber();

    /**
     * Sets the possible number of callbacks. Callbackable operations should report
     * the possible number of callbacks depending on the total number of steps
     * required for their execution.
     * 
     * @param cbNumber the possible number of callbacks
     */
    public void setPossibleCallbackNumber(int cbNumber);

    /**
     * Returns the possible number of callbacks.
     * 
     * @return the possible number of callbacks
     */
    public int getPossibleCallbackNumber();

    /**
     * Returns how many times was the callback method invoked (i.e., the value of
     * the callback counter).
     * 
     * @return how many times was the callback method invoked
     */
    public int getCallbackCount();

    /**
     * Sets the value of the callback counter.
     * 
     * @param cbCount the value of the callback counter.
     */
    public void setCallbackCount(int cbCount);

    /**
     * The callback method. The {@link Callbackable} object should call this method
     * regularly during the execution of its lengthy operation.
     * 
     * @param obj the {@code Callbackable} object that is calling back
     * @throws Exception if an error occurs
     */
    public void callback(Object obj) throws Exception;

    /**
     * Maps the value of the callback counter from the interval
     * {@code [0...possible number of callbacks]} to the interval
     * {@code [0...desired desired number of callbacks]}.
     * 
     * @return the mapped value
     */
    public int getProgress();

    /**
     * Maps {@code cbCount} from the interval
     * {@code [0...possible number of callbacks]} to interval
     * {@code [0...desired number of callbacks]}.
     * 
     * @param cbCount the value to be mapped from the interval
     *                {@code [0...possible number of callbacks]} to the interval
     *                {@code [0...desired number of callbacks]}.
     * @return the mapped value
     */
    public int getProgress(int cbCount);

}

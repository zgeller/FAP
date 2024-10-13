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
 * Declares common methods for callbackable operations.
 * 
 * <p>
 * Callbackable operations should call the {@link Callback#callback(Object)
 * callback} method of the specified {@link Callback} object at regular
 * intervals.
 * 
 * <p>
 * The {@code Callback} object reports the desired number of callbacks via the
 * {@link Callback#getDesiredCallbackNumber() getDesiredCallbackNumber} method.
 * 
 * <p>
 * Depending on the total number of steps, the callbackable operation can report
 * the possible number of callbacks via the
 * {@link Callback#setPossibleCallbackNumber(int) setPossibleCallbackNumber}
 * method of the {@code Callback} object.
 * 
 * @author Zoltán Gellér
 * @version 2024.08.17.
 * @see Callback
 */
public interface Callbackable {

    /**
     * Sets the callback object.
     * 
     * @param callback the callback object
     */
    public void setCallback(Callback callback);

    /**
     * Returns the callback object.
     * 
     * @return the callback object
     */
    public Callback getCallback();

}

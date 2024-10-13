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

package fap.util;

/**
 * Defines common methods for classes that implements resumable operations.
 * 
 * <p>
 * Resumeable operations should be able to resume execution from (or close to)
 * the point where they were interrupted. Furthermore, they should be able to
 * {@link #reset} their state and thus restart the operation.
 * 
 * @author Zoltán Gellér
 * @version 2024.07.18.
 */
public interface Resumable {

    /**
     * Resets the operation for reuse the object.
     */
    public void reset();

    /**
     * Indicates whether the resumable operation has completed.
     * 
     * @return {@code true} if the operation has completed
     */
    public boolean isDone();

    /**
     * Indicates whether the resumable operation is still in progress.
     * 
     * @return {@code true} if the resumable operation is still in progress
     */
    public boolean isInProgress();

}

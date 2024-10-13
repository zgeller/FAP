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
 * Declares common methods for copyable objects.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.17.
 */
public interface Copyable {

    /**
     * Makes a new instance of the given class and initializes it with the
     * parameters of this object. It implies a deep copy.
     * 
     * @param deep indicates whether a deep copy should be made
     * @return a new instance of the given class initialized with the parameters of
     *         this object
     */
    public default Object makeACopy() {
        return makeACopy(true);
    }
    
    /**
     * Makes a new instance of the given class and initializes it with the
     * parameters of this object.
     * 
     * @param deep indicates whether a deep copy should be made
     * @return a new instance of the given class initialized with the parameters of
     *         this object
     */
    public Object makeACopy(boolean deep);

}

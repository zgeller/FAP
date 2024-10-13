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

package fap.trainer;

/**
 * Auxiliary interface for distance measure parameter setters.
 * 
 * @param <T> the type of the parameter to be set by this parameter setter
 * 
 * @author Zoltán Gellér
 * @version 2024.09.23.
 * @see Modifier
 */
public interface DistanceModifier<T> extends Modifier<T> {

    @Override
    public default boolean affectsDistance() {
        return true;
    }
    
}

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

package fap.distance.elastic;

/**
 * Declares getters and setters for the parameters of TWED-based distance
 * measures.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.24.
 */
public interface TWEDParameters {
    
    /**
     * Returns the stiffness of the measure.
     * 
     * @return the stiffness of the measure
     */
    public double getNu();
    
    /**
     * Sets the value that is to be used to control the stiffness of the measure.
     * 
     * @param nu the value that is to be used to control the stiffness of the
     *           measure
     */
    public void setNu(double nu);

    /**
     * Returns the value used to calculate penalties for insert and delete
     * operations.
     * 
     * @return the value used to calculate penalties for insert and delete
     *         operations
     */
    public double getLambda();
    
    
    /**
     * Sets the the value that is to be used to calculate penalties for insert and
     * delete operations. Must be {@code lambda >= 0}.
     * 
     * @param lambda the value that is to be used to calculate penalties for insert
     *               and delete operations
     */
    public void setLambda(double lambda);

}

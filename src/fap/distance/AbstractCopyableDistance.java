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

package fap.distance;

import fap.core.distance.AbstractDistance;
import fap.util.Copyable;

/**
 * Defines common methods and fields for copyable distance measures.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see AbstractDistance
 * @see Copyable
 */
public abstract class AbstractCopyableDistance extends AbstractDistance implements Copyable {

    private static final long serialVersionUID = 1L;

    /**
     * Empty constructor.
     */
    public AbstractCopyableDistance() {
    }
    
    /**
     * Constructor with the possibility to enable or disable storing distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractCopyableDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Initializes the specified distance measure with the common data structures of this
     * distance.
     * 
     * @param copy the distance measure whose data structures is to be initialized
     */
    protected void init(AbstractCopyableDistance copy, boolean deep) {
        copy.setStoring(this.isStoring());
    }

}

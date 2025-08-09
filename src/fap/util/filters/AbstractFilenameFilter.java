/*   
 * Copyright 2016 Zoltán Gellér
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

package fap.util.filters;

import java.io.FilenameFilter;

/**
 * Abstract {@code FilenameFilter} class. Defines basic fields and methods. By
 * default, it doesn't accept directories.
 * 
 * @author Zoltán Gellér
 * @version 2016.03.18.
 * @see java.io.FilenameFilter
 */
public abstract class AbstractFilenameFilter implements FilenameFilter {

    /**
     * Determines whether to accept folders too. Default value is {@code false}
     */
    private boolean acceptFolders = false;

    /**
     * Returns the value of {@code acceptFolder}.
     * 
     * @return the acceptFolders
     */
    public boolean isAcceptFolders() {
        return acceptFolders;
    }

    /**
     * Sets the {@code acceptFolders}
     * 
     * @param acceptFolders the acceptFolders to set
     */
    public void setAcceptFolders(boolean acceptFolders) {
        this.acceptFolders = acceptFolders;
    }

}

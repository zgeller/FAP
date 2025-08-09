/*   
 * Copyright 2013 Zoltán Gellér
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

import javax.swing.filechooser.FileFilter;

/**
 * Abstract {@code FileFilter} class. Defines basic fields and methods. By
 * default, it accepts directories.
 * 
 * @author Zoltán Gellér
 * @version 2013.09.19.
 * @see javax.swing.filechooser.FileFilter
 * @see java.io.FileFilter
 */
public abstract class AbstractFileFilter extends FileFilter implements
		java.io.FileFilter {

    /**
     * The description.
     */
    private String description = "";
	
    /**
     * Determines whether to accept folders too. Default value is {@code true}
     */
    private boolean acceptFolders = true;

    /**
     * Sets the description of the filter.
     * 
     * @param description - the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        if (description != null)
            return this.description;
        else
            return "";
    }

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

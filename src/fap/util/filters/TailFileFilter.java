/*   
 * Copyright 2019 Zoltán Gellér
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

import java.io.File;

import fap.util.StringUtils;


/**
 * {@code TailFileFilter}
 * 
 * @author Zoltán Gellér
 * @version 2019.09.19.
 * @see AbstractFileFilter
 */
public class TailFileFilter extends AbstractFileFilter {
	
    /**
     * Array of tails.
     */
    private String[] tails;
	
    /**
     * Indicates whether to ignore case. Default value is {@code true}.
     */
    private boolean ignoreCase = true;
	
    /**
     * Creates a new {@code TailFileFilter} with an array of tails, without a
     * description and with {@code ignoreCase=true}.
     * 
     * @param tails array of tails
     */
    public TailFileFilter(String... tails) {
        this(null, true, tails);
    }
	
    /**
     * Creates a new {@code TailFileFilter} with an array of tails without a
     * description.
     * 
     * @param ignoreCase the ignoreCase
     * @param tails      array of tails
     */
    public TailFileFilter(boolean ignoreCase, String... tails) {
        this(null, ignoreCase, tails);
    }
	
    /**
     * Creates a new {@code TailFileFilter} an array of tails and with a
     * description.
     * 
     * @param description the description
     * @param ignoreCase  the ignoreCase
     * @param tails       array of tails
     */
    public TailFileFilter(String description, boolean ignoreCase, String... tails) {
        this.setDescription(description);
        this.setIgnoreCase(ignoreCase);
        this.setTails(tails);
    }

    /**
     * Returns the tails.
     * 
     * @return the tails
     */
    public String[] getTails() {
        return tails;
    }

    /**
     * Sets the tails.
     * 
     * @param tails the tails to set
     */
    public void setTails(String... tails) {
        this.tails = tails;
    }
	
    /**
     * Returns the ignoreCase.
     * 
     * @return the ignoreCase
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Sets the ignoreCase.
     * 
     * @param ignoreCase the ignoreCase to set
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean accept(File file) {

        if (tails == null)
            return true;

        if (file.isDirectory())
            return isAcceptFolders();

        String fname = file.getAbsolutePath();

        if (ignoreCase) {
            for (String suffix : tails)
                if (StringUtils.endsWithIgnoreCase(fname, suffix))
                    return true;
        }

        else {
            for (String suffix : tails)
                if (suffix != null && fname.endsWith(suffix))
                    return true;
        }

        return false;
        
    }
	
}

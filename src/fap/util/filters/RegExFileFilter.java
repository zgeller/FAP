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

import java.io.File;
import java.util.regex.Pattern;

/**
 * {@code RegExFileFilter}
 * 
 * @author Zoltán Gellér
 * @version 2013.09.19.
 * @see AbstractFileFilter
 */
public class RegExFileFilter extends AbstractFileFilter {

    /**
     * The pattern.
     */
    private Pattern pattern;	
	
    /**
     * Creates a new {@code RegExFileFilter} object with the given pattern.
     * 
     * @param pattern a {@code Pattern} object
     */
    public RegExFileFilter(Pattern pattern) {
        this(null, pattern);
    }

    /**
     * Creates a new {@code RegExFileFilter} object with the given description and
     * pattern.
     * 
     * @param description the description
     * @param pattern     the pattern
     */
    public RegExFileFilter(String description, Pattern pattern) {
        this.setDescription(description);
        this.setPattern(pattern);
    }
	
    /**
     * Creates a new {@code RegExFileFilter} object with the given regular
     * expression.
     * 
     * @param regex the regular expression
     */
    public RegExFileFilter(String regex) {
        this(null, regex);
    }
	
    /**
     * Creates a new {@code RegExFileFilter} object with the given description and
     * regular expression.
     * 
     * @param description the description
     * @param regex       the regular expression
     */
    public RegExFileFilter(String description, String regex) {
        this.setDescription(description);
        pattern = Pattern.compile(regex);
    }
	
    /**
     * Returns the pattern.
     * 
     * @return the pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Sets the pattern.
     * 
     * @param pattern the pattern to set
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
	
    /**
     * Sets the pattern.
     * 
     * @param regex the regular expression to set
     */
    public void setPattern(String regex) {
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean accept(File file) {

        if (pattern == null)
            return true;

        if (file.isDirectory())
            return isAcceptFolders();

        String fname = file.getAbsolutePath();

        return pattern.matcher(fname).matches();
        
    }

}
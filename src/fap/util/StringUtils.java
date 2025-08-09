/*   
 * Copyright 2024-2025 Zoltán Gellér
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
 * String utilities.
 * 
 * @author Zoltán Gellér
 * @version 2025.03.06.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Checks if {@code str} ends with the given {@code suffix}, ignores case.
     * 
     * @param str    the string
     * @param suffix the suffix
     * @return {@code true} if {@code str} ends with the given {@code suffix},
     *         ignores case.
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {

        if (suffix == null || str == null)
            return false;

        int beginIndex = str.length() - suffix.length();
        return beginIndex < 0 ? false : str.substring(beginIndex).equalsIgnoreCase(suffix);
        
    }

    /**
     * Attempts to convert the given {@code String} to an {@code int}. If
     * successful, it returns the absolute value of the number. If unsuccessful, it
     * returns the absolute value of the provided default value, {@code def}.
     * 
     * @param str the string to be converted into a non-negative integer
     * @param def the number whose absolute value is returned on failure
     * @return the absolute value of the number contained in the string or the
     *         absolute value of the parameter {@code def}
     */
    public static int parseAbs(String str, int def) {
        try {
            return Math.abs(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return Math.abs(def);
        }
    }

}

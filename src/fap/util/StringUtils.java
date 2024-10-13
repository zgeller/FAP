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
 * String utilities.
 * 
 * @author Zoltán Gellér
 * @version 2024.08.26.
 */
public final class StringUtils {

    private StringUtils() {

    }

    /**
     * Tests if the string {@code str} ends with the specified suffix ignoring the
     * case.
     * 
     * @param str    - the string
     * @param suffix - the suffix
     * @return {@code true} if the string {@code str} ends with the given
     *         {@code suffix} ignoring the case.
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {

        if (suffix == null || str == null)
            return false;

        int beginIndex = str.length() - suffix.length();
        return beginIndex < 0 ? false : str.substring(beginIndex).equalsIgnoreCase(suffix);
    }

    /**
     * Tries to converts the given {@code String} to an {@code int} number. In case
     * of success, it will return the absolute value of the number. In case of
     * failure, it will return the given default value.
     * 
     * @param str the string to be converted into an integer number
     * @param def the default value to be returned in case of failure
     * @return the absolute value of the converted integer or the default value in
     *         case of failure
     */
    public static int parseAbs(String str, int def) {
        int tmp = def;
        try {
            tmp = Integer.parseInt(str);
        } catch (NumberFormatException e) {
        }
        return Math.abs(tmp);
    }

}

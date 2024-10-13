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

package fap.core.exception;

/**
 * General fap runtime exception.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.11.
 * @see RuntimeException
 */
public class CoreRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new core runtime exception with {@code null} as its message.
     */
    public CoreRuntimeException() {
    }

    /**
     * Constructs a new core runtime exception with the specified message.
     * 
     * @param msg the message
     */
    public CoreRuntimeException(String msg) {
        super(msg);
    }

}

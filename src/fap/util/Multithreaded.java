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
 * Declares common methods for multithreaded classes.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.01.
 *
 */
public interface Multithreaded {

    /**
     * Sets the number of threads.
     * 
     * @param tnumber the number of threads
     */
    public void setNumberOfThreads(int tnumber);

    /**
     * Returns the number of threads.
     * 
     * @return the number of threads
     */
    public int getNumberOfThreads();
    
    /**
     * Shuts downs any executors and threads.
     */
    public void shutdown();

}

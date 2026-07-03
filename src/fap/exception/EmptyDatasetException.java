/*   
 * Copyright 2024-2026 Zoltán Gellér
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

package fap.exception;

import fap.data.Dataset;

/**
 * Runtime exception thrown when the specified dataset cannot be empty but it is empty.
 * 
 * @author Zoltán Gellér
 * @version 2026.07.03.
 * @see CoreRuntimeException
 */
public class EmptyDatasetException extends CoreRuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new EmptyDataset exception with {@code null} as its message.
     */
    public EmptyDatasetException() {
    }
    
    /**
     * Constructs a new EmptyDataset exception with the specified message.
     * 
     * @param msg the message
     */
    public EmptyDatasetException(String msg) {
        super(msg);
    }
    
    /**
     * Checks if the specified {@code dataset} is empty and throws an
     * {@code EmptyDataset} exception if it is empty.
     * 
     * @param dataset the dataset to be checked
     * @throws EmptyDatasetException if the {@code dataset} is empty
     */
    public static void check(Dataset dataset) {
        if (dataset.isEmpty())
            throw new EmptyDatasetException("The dataset cannot be empty.");
    }

    /**
     * Checks if the specified {@code dataset} is empty and throws an
     * {@code EmptyDataset} exception if it is empty.
     * 
     * @param dataset the dataset to be checked
     * @param dsname  the name of of the dataset
     * @throws EmptyDatasetException if the {@code dataset} is empty
     */
    public static void check(Dataset dataset, String dsname) {
        if (dataset.isEmpty())
            throw new EmptyDatasetException("The " + dsname + " dataset cannot be empty.");
    }

}

/*   
 * Copyright 2024 Aleksa Todorović, Zoltán Gellér
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

package fap.core.predictor;

import fap.core.data.Dataset;
import fap.core.data.TimeSeries;

/**
 * Generic parent for all predictors which predict output based on input series
 * list and one testing series.
 * 
 * @author Aleksa Todorović, Zoltán Gellér
 * @version 2024.09.17.
 * @see Predictor
 */
public abstract class OutputPredictor<T extends TimeSeries> implements Predictor {

    private static final long serialVersionUID = 1L;

    /**
     * Testing input series.
     */
    protected T testInputSeries;

    /**
     * List of input series.
     */
    protected Dataset inputSeries;

    /**
     * Constructor.
     * 
     * @param testInputSeries testing input series
     * @param dataset    list of input series
     */
    protected OutputPredictor(T testInputSeries, Dataset inputSeries) {
        this.testInputSeries = testInputSeries;
        this.inputSeries = inputSeries;
    }

}

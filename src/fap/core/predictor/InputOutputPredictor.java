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

import java.util.Map;

import fap.core.data.Dataset;
import fap.core.data.TimeSeries;

/**
 * Generic parent for all predictors which predict output based on input series
 * list, corresponding outputs and one testing series.
 * 
 * @author Aleksa Todorović, Zoltán Gellér
 * @version 2024.09.17.
 * @see Predictor
 */
public abstract class InputOutputPredictor implements Predictor {

    private static final long serialVersionUID = 1L;

    /**
     * Testing input series.
     */
    protected TimeSeries testInputSeries;

    /**
     * Dataset.
     */
    protected Dataset dataset;

    /**
     * Map of output series for input series. Keys are input series, values are
     * output series.
     */
    protected Map<TimeSeries, TimeSeries> inputOutputMapping;

    /**
     * Constructor
     * 
     * @param testInputSeries    testing input series
     * @param dataset            input dataset
     * @param inputOutputMapping map of output series for input series
     */
    protected InputOutputPredictor(TimeSeries testInputSeries, 
                                   Dataset dataset, 
                                   Map<TimeSeries, TimeSeries> inputOutputMapping) {
        this.testInputSeries = testInputSeries;
        this.dataset = dataset;
        this.inputOutputMapping = inputOutputMapping;
    }

}

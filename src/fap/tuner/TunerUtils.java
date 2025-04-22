/*   
 * Copyright 2025 Zoltán Gellér
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

package fap.tuner;

import fap.core.evaluator.Evaluator;

/**
 * Tuner utilities.
 * 
 * @author Zoltán Gellér
 * @version 2025.04.22.
 */
public final class TunerUtils {
    
    private TunerUtils() {
    }

    /**
     * Chains the given parameter tuners and returns the first one, i.e.:
     * 
     * <p>
     * <pre><code>
     *  firstTuner.setSubtuner(tuners[0]);
     *  
     *  tuners[0].setSubtuner(tuners[1]);
     *  ...
     *  tuners[n-1].setSubtuner(tuners[n]);
     *  
     *  tuners[n].setEvaluator(evaluator);
     * </code></pre>
     * 
     * <p>
     * asuming that {@code tuners.length = n}.
     * 
     * <p>
     * If {@code tuners} is empty, the {@code evaluator} will be passed to the {@code firstTuner}.
     * 
     * @param <T>        the type of the parameter that is to be tuned by the first
     *                   tuner
     * @param evaluator  the evaluator
     * @param firstTuner the first tuner
     * @param tuners     the tuners to be chained
     * @return the first tuner
     */
    public static <T extends Comparable<T>> ParameterTuner<T> chain(Evaluator evaluator, ParameterTuner<T> firstTuner, ParameterTuner<?>... tuners) {
        
        final int last = tuners.length - 1;
        
        if (last >= 0) {
            for (int i = 0; i < last; i++)
                tuners[i].setSubtuner(tuners[i+1]);
            tuners[last].setEvaluator(evaluator);
            firstTuner.setSubtuner(tuners[0]);
        }
        
        else
            firstTuner.setEvaluator(evaluator);
        
        return firstTuner;
        
    }
    
}

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

import java.util.Collection;

import fap.callback.Callbackable;
import fap.core.classifier.Classifier;
import fap.core.evaluator.Evaluator;
import fap.core.tuner.Tuner;

/**
 * Evaluator utilities.
 * 
 * @author Zoltán Gellér
 * @version 2025.04.22.
 */
public final class Copier {

    private Copier() {
    }
    
    /**
     * Makes {@code n} copies of the specified {@code tuner} and {@code classifier}
     * and adds them to the {@code tuners} and {@code classifiers} lists.
     * 
     * <p>
     * If the {@code tuner} ({@code classifier}) implements the
     * {@link Multithreaded} interface, sets the number of threads of the copies to
     * 1.
     * 
     * <p>
     * If the {@code tuner} ({@code classifier}) implements the {@link Callbackable}
     * interface, initializes the callback of the copies to {@code null}.
     * 
     * <p>
     * If the tuner does not affect the distance measure, the copies of the
     * classifier will not be deep.
     * 
     * @param tuner       the tuner whose copies should be made
     * @param classifier  the classifier whose copies should be made
     * @param tuners      the list to which the copies of the {@code tuner} should
     *                    be added
     * @param classifiers the list to which the copies of the {@code classifier}
     *                    should be added
     * @param n           number of copies
     */
    public static void makeCopies(Tuner tuner, Classifier classifier, Collection<Tuner> tuners, Collection<Classifier> classifiers, int n) {
        
        for (int i = 0; i < n; i++) {
            
            if (tuner != null) {
                Tuner tCopy = (Tuner) ((Copyable)tuner).makeACopy();
                tuners.add(tCopy);
            }
            
            // deep copy of the classifier is needed only if the tuner affects the
            // underlying distance measure
            boolean deepCopy = tuner != null && tuner.affectsDistance();
            Classifier cCopy = (Classifier) ((Copyable)classifier).makeACopy(deepCopy);
            classifiers.add(cCopy);
            
        }
        
    }

    /**
     * Makes {@code n} copies of the specified {@code tuner} and adds them to the
     * {@code tuners} list.
     * 
     * <p>
     * If the {@code tuner} implements the {@link Multithreaded} interface, sets
     * the number of threads of the copies to 1.
     * 
     * <p>
     * If the {@code tuner} implements the {@link Callbackable} interface,
     * initializes the callback of the copies to {@code null}.
     * 
     * @param tuner  the tuner whose copies should be made
     * @param tuners the list to which the copies of the {@code tuner} should be
     *                 added
     * @param n        number of copies
     */
    public static void makeCopies(Tuner tuner, Collection<Tuner> tuners, int n) {
        
        if (tuner == null)
            return;
        
        for (int i = 0; i < n; i++) {
            Tuner tCopy = (Tuner) ((Copyable)tuner).makeACopy();
            tuners.add(tCopy);
        }
        
    }
    
    /**
     * Makes {@code n} copies of the specified {@code classifier} and adds them to
     * the {@code classifiers} list.
     * 
     * <p>
     * If the {@code classifier} implements the {@link Multithreaded} interface,
     * sets the number of threads of the copies to 1.
     * 
     * <p>
     * If the {@code classifier} implements the {@link Callbackable} interface,
     * initializes the callback of the copies to {@code null}.
     * 
     * @param classifier  the classifier whose copies should be made
     * @param classifiers the list to which the copies of the {@code classifier}
     *                    should be added
     * @param deep        indicates whether a deep copy should be made
     * @param n           number of copies
     */
    public static void makeCopies(Classifier classifier, Collection<Classifier> classifiers, boolean deep, int n) {

        if (classifier == null)
            return;
        
        for (int i = 0; i < n; i++) {
            Classifier cCopy = (Classifier) ((Copyable)classifier).makeACopy(deep);
            classifiers.add(cCopy);
        }
        
    }
    
    /**
     * Makes {@code n} copies of the specified {@code evaluator} and adds them to
     * the {@code evaluators} list.
     * 
     * <p>
     * If the {@code evaluator} implements the {@link Multithreaded} interface, sets
     * the number of threads of the copies to 1.
     * 
     * <p>
     * If the {@code evaluator} implements the {@link Callbackable} interface,
     * initializes the callback of the copies to {@code null}.
     * 
     * @param evaluator  the evaluator whose copies should be made
     * @param evaluators the list to which the copies of the {@code evaluator}
     *                   should be added
     * @param n          number of copies
     */
    public static void makeCopies(Evaluator evaluator, Collection<Evaluator> evaluators, int n) {
        
        if (evaluator == null)
            return;
        
        for (int i = 0; i < n; i++) {
            Evaluator eCopy = (Evaluator) ((Copyable)evaluator).makeACopy();
            evaluators.add(eCopy);
        }
        
    }
    

}

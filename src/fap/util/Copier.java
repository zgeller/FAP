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

import java.util.Collection;

import fap.callback.Callbackable;
import fap.core.classifier.Classifier;
import fap.core.evaluator.Evaluator;
import fap.core.trainer.Trainer;

/**
 * Evaluator utilities.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.22.
 */
public final class Copier {

    /**
     * Makes {@code n} copies of the specified {@code trainer} and
     * {@code classifier} and adds them to the {@code trainers} and
     * {@code classifiers} lists.
     * 
     * <p>
     * If the {@code trainer} ({@code classifier}) implements the
     * {@link Multithreaded} interface, sets the number of threads of the copies to
     * 1.
     * 
     * <p>
     * If the {@code trainer} ({@code classifier}) implements the
     * {@link Callbackable} interface, initializes the callback of the copies to
     * {@code null}.
     * 
     * <p>
     * If the trainer implements the DistanceBasedTrainer interface and does not
     * affect the distance measure, the copies of the classifier will not be deep.
     * 
     * @param trainer     the trainer whose copies should be made
     * @param classifier  the classifier whose copies should be made
     * @param trainers    the list to which the copies of the {@code trainer} should
     *                    be added
     * @param classifiers the list to which the copies of the {@code classifier}
     *                    should be added
     * @param n           number of copies
     */
    public static void makeCopies(Trainer trainer, Classifier classifier, Collection<Trainer> trainers, Collection<Classifier> classifiers, int n) {
        
        for (int i = 0; i < n; i++) {
            
            if (trainer != null) {
                Trainer tCopy = (Trainer) ((Copyable)trainer).makeACopy();
                trainers.add(tCopy);
            }
            
            // deep copy of the classifier is needed only if the trainer affects the
            // underlying distance measure
            boolean deepCopy = trainer != null && trainer.affectsDistance();
            Classifier cCopy = (Classifier) ((Copyable)classifier).makeACopy(deepCopy);
            classifiers.add(cCopy);
            
        }
        
    }

    /**
     * Makes {@code n} copies of the specified {@code trainer} and adds them to the
     * {@code trainers} list.
     * 
     * <p>
     * If the {@code trainer} implements the {@link Multithreaded} interface, sets
     * the number of threads of the copies to 1.
     * 
     * <p>
     * If the {@code trainer} implements the {@link Callbackable} interface,
     * initializes the callback of the copies to {@code null}.
     * 
     * @param trainer  the trainer whose copies should be made
     * @param trainers the list to which the copies of the {@code trainer} should be
     *                 added
     * @param n        number of copies
     */
    public static void makeCopies(Trainer trainer, Collection<Trainer> trainers, int n) {
        
        if (trainer == null)
            return;
        
        for (int i = 0; i < n; i++) {
            Trainer tCopy = (Trainer) ((Copyable)trainer).makeACopy();
            trainers.add(tCopy);
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

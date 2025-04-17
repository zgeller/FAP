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

package fap.trainer;

import fap.classifier.nn.KNNClassifier;
import fap.classifier.nn.MacleodKNNClassifier;
import fap.classifier.nn.ZavrelKNNClassifier;
import fap.core.classifier.Classifier;
import fap.core.classifier.DistanceBasedClassifier;
import fap.core.distance.Distance;
import fap.distance.ConstrainedDistance;
import fap.distance.ERPParameters;
import fap.distance.MinkowskiDistance;
import fap.distance.TWEDParameters;
import fap.distance.ThresholdDistance;

/**
 * Auxiliary class that defines common modifiers.
 *
 * @author Zoltán Gellér
 * @version 2024.09.24.
 * @see ClassifierModifier
 * @see DistanceModifier
 * @see Modifier
 */
public final class Modifiers {

    /**
     * A modifier for setting the number of nearest neighbors of kNN classifiers.
     * 
     * @see KNNClassifier
     */
    public static final ClassifierModifier<Integer> KNN = new ClassifierModifier<>() {

        @Override
        public void set(Classifier classifier, Integer value) {
            ((KNNClassifier) classifier).setK(value);
        }

    };

    /**
     * A modifier for setting the value of the {@code s} parameter of weighted kNN
     * classifiers utilizing Macleod's weighting function.
     * 
     * @see MacleodKNNClassifier
     */
    public static final ClassifierModifier<Integer> MACLEOD_S = new ClassifierModifier<>() {

        @Override
        public void set(Classifier classifier, Integer value) {
            ((MacleodKNNClassifier) classifier).setS(value);
        }

    };
    
    /**
     * A modifier for setting the value of the {@code alpha} parameter of weighted kNN
     * classifiers utilizing Macleod's weighting function.
     * 
     * @see MacleodKNNClassifier
     */
    public static final ClassifierModifier<Double> MACLEOD_ALPHA = new ClassifierModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            ((MacleodKNNClassifier) classifier).setAlpha(value);
        }

    };

    /**
     * A modifier for setting the value of the {@code alpha} parameter of weighted kNN
     * classifiers utilizing Zavrel's weighting function.
     * 
     * @see ZavrelKNNClassifier
     */
    public static final ClassifierModifier<Double> ZAVREL_ALPHA = new ClassifierModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            ((ZavrelKNNClassifier) classifier).setAlpha(value);
        }

    };

    /**
     * A modifier for setting the value of the {@code beta} parameter of weighted kNN
     * classifiers utilizing Zavrel's weighting function.
     * 
     * @see ZavrelKNNClassifier
     */
    public static final ClassifierModifier<Double> ZAVREL_BETA = new ClassifierModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            ((ZavrelKNNClassifier) classifier).setBeta(value);
        }

    };
    
    /**
     * A modifier for setting the relative warping (editing) window width of
     * constrained distance measures.
     * 
     * @see ConstrainedDistance
     */
    public static final DistanceModifier<Double> ELASTICITY = new DistanceModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            Distance distance = ((DistanceBasedClassifier) classifier).getDistance();
            ((ConstrainedDistance) distance).setR(value);
        }

    };
    
    /**
     * A modifier for setting the absolute warping (editing) window width of
     * constrained distance measures.
     * 
     * @see ConstrainedDistance
     */
    public static final DistanceModifier<Integer> ABSOLUT_ELASTICITY = new DistanceModifier<>() {

        @Override
        public void set(Classifier classifier, Integer value) {
            Distance distance = ((DistanceBasedClassifier) classifier).getDistance();
            ((ConstrainedDistance) distance).setW(value);
        }

    };
    
    /**
     * A modifier for setting the matching threshold of threashold-based distance
     * measures.
     * 
     * @see ThresholdDistance
     */
    public static final DistanceModifier<Double> THRESHOLD = new DistanceModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            Distance distance = ((DistanceBasedClassifier) classifier).getDistance();
            ((ThresholdDistance) distance).setEpsilon(value);
        }

    };
    
    /**
     * A modifier for setting the value of the {@code p} parameter of the Minkowski
     * distance measures.
     * 
     * @see MinkowskiDistance
     */
    public static final DistanceModifier<Double> MINKOWSKI_P = new DistanceModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            Distance distance = ((DistanceBasedClassifier) classifier).getDistance();
            ((MinkowskiDistance) distance).setP(value);
        }

    };
    
    /**
     * A modifier for setting the value of the {@code g} parameter of the ERP-based 
     * distance measures.
     * 
     * @see ERPParameters
     */
    public static final DistanceModifier<Double> ERP_G = new DistanceModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            Distance distance = ((DistanceBasedClassifier) classifier).getDistance();
            ((ERPParameters) distance).setG(value);
        }

    };
    
    /**
     * A modifier for setting the value of the {@code nu} parameter of the TWED-based 
     * distance measures.
     * 
     * @see TWEDParameters
     */
    public static final DistanceModifier<Double> TWED_NU = new DistanceModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            Distance distance = ((DistanceBasedClassifier) classifier).getDistance();
            ((TWEDParameters) distance).setNu(value);
        }

    };

    /**
     * A modifier for setting the value of the {@code lambda} parameter of the TWED-based 
     * distance measures.
     * 
     * @see TWEDParameters
     */
    public static final DistanceModifier<Double> TWED_LAMBDA = new DistanceModifier<>() {

        @Override
        public void set(Classifier classifier, Double value) {
            Distance distance = ((DistanceBasedClassifier) classifier).getDistance();
            ((TWEDParameters) distance).setLambda(value);
        }

    };
    
    
}

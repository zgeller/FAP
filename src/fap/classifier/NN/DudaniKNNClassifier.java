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

package fap.classifier.NN;

import java.util.HashMap;

import fap.classifier.NN.util.LinkedDistanceNode;
import fap.classifier.NN.util.SortedList;
import fap.core.data.TimeSeries;
import fap.core.distance.Distance;

/**
 * Weighted kNN classifier utilizing Dudani's weighting function. The class of a
 * time series is determined by weighted voting of its k-nearest neighbours in
 * the training set. Weights are calculated as defined by Dudani: 
 * 
 * <blockquote> <img src="doc-files/DudaniKNNClassifier-1.png">
 * </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> S.A. Dudani, The Distance-Weighted k-Nearest-Neighbor Rule, IEEE Trans.
 *       Syst. Man. Cybern. SMC-6 (1976) 325–327.
 *       <a href="https://doi.org/10.1109/TSMC.1976.5408784">
 *          https://doi.org/10.1109/TSMC.1976.5408784</a>.
 *  <li> T.-L. Pao, Y.-T. Chen, J.-H. Yeh, Y.-M. Cheng, Y.-Y. Lin, A Comparative
 *       Study of Different Weighting Schemes on KNN-Based Emotion Recognition in
 *       Mandarin Speech, in: D.-S. Huang, L. Heutte, M. Loog (Eds.), Adv. Intell.
 *       Comput. Theor. Appl. With Asp. Theor. Methodol. Issues, Springer Berlin
 *       Heidelberg, Berlin, Heidelberg, 2007: pp. 997–1005.
 *       <a href="https://doi.org/10.1007/978-3-540-74171-8_101">
 *          https://doi.org/10.1007/978-3-540-74171-8_101</a>.
 *  <li> J. Zavrel, An Empirical Re-Examination of Weighted Voting for k-NN, in:
 *       Proc. 7th Belgian-Dutch Conf. Mach. Learn., 1997: pp. 139–148.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2024.09.17.
 * @see KNNClassifier
 */
public class DudaniKNNClassifier extends KNNClassifier {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs a new single-threaded weighted kNN classifier utilizing Dudani's
     * weighting function, with the default number of nearest neighbours
     * ({@link KNNClassifier#k k}).
     */
	public DudaniKNNClassifier() {
	}

	/**
     * Constructs a new single-threaded weighted kNN classifier utilizing Dudani's
     * weighting function, with the given number of nearest neighbours ({@code k}).
     * 
     * @param k number of nearest neighbours, must be {@code >= 1}
     */
	public DudaniKNNClassifier(int k) {
		super(k);
	}
	
	/**
     * Constructs a new weighted kNN classifier utilizing Dudani's weighting
     * function, with the given number of nearest neighbours ({@code k}), and number
     * of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbours, must be {@code >= 1}
     * @param tnumber number of threads
     */
	public DudaniKNNClassifier(int k, int tnumber) {
	   super(k, tnumber); 
	}

    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing Dudani's
     * weighting function, with the given distance measure ({@code distance}) and
     * with the default number of nearest neighbours ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public DudaniKNNClassifier(Distance distance) {
        super(distance);
    }
	
	/**
     * Constructs a new single-threaded weighted kNN classifier utilizing Dudani's
     * weighting function, with the given distance measure ({@code distance}) and
     * number of nearest neighbours ({@code k}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     */
	public DudaniKNNClassifier(Distance distance, int k) {
		super(distance, k);
	}
	
    /**
     * Constructs a new weighted kNN classifier utilizing Dudani's weighting
     * function, with the given distance measure ({@code distance}), number of
     * nearest neighbours ({@code k}), and number of threads ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     * @param tnumber  number of threads
     */
	public DudaniKNNClassifier(Distance distance, int k, int tnumber) {
	    super(distance, k, tnumber);
	}

	/**
	 * Finds the best label among the nearest neighbours. Weights are calculated as
	 * defined by Dudani.
	 * 
	 * @param list sorted list of the nearest neighbours
	 * @return the best label
	 */
	@Override
	protected double getBestLabel(SortedList<TimeSeries> list) {

		LinkedDistanceNode<TimeSeries> firstNode = list.getFirst();
		LinkedDistanceNode<TimeSeries> lastNode = list.getLast();

		double lastDist = lastNode.distance;
		double diff = lastDist - firstNode.distance;

		double bestLabel;

		if (diff == 0) {

			LinkedDistanceNode<TimeSeries> node = firstNode;

			HashMap<Double, Integer> neighbours = new HashMap<Double, Integer>();

			bestLabel = node.obj.getLabel();
			int bestWeight = 1;

			neighbours.put(bestLabel, bestWeight);
			node = node.next;

			while (node != null) {

				double label = node.obj.getLabel();
				int weight = 1;

				if (neighbours.containsKey(label))
					weight += neighbours.get(label);
				neighbours.put(label, weight);

				if (weight > bestWeight) {
					bestLabel = label;
					bestWeight = weight;
				}

				node = node.next;
			}

		}

		else {

			LinkedDistanceNode<TimeSeries> node = firstNode;

			HashMap<Double, Double> neighbours = new HashMap<Double, Double>();

			bestLabel = firstNode.obj.getLabel();
			double bestWeight = (lastDist - node.distance) / diff;

			neighbours.put(bestLabel, bestWeight);
			node = node.next;

			while (node != null) {

				double label = node.obj.getLabel();
				double weight = (lastDist - node.distance) / diff;

				if (neighbours.containsKey(label))
					weight += neighbours.get(label);
				neighbours.put(label, weight);

				if (weight > bestWeight) {
					bestLabel = label;
					bestWeight = weight;
				}

				node = node.next;
			}
		}

		return bestLabel;
	}

    @Override
    public Object makeACopy(boolean deep) {
        DudaniKNNClassifier copy = new DudaniKNNClassifier();
        init(copy, deep);
        return copy;
    }
	
}

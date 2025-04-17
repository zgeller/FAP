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

package fap.classifier.nn;

import java.util.HashMap;
import java.util.Map;

import fap.classifier.nn.util.LinkedDistanceNode;
import fap.classifier.nn.util.SortedList;
import fap.core.data.TimeSeries;
import fap.core.distance.Distance;

/**
 * Weighted kNN classifier utilizing the dual-uniform weighting function. The
 * class of a time series is determined by weighted voting of its k-nearest
 * neighbors in the training set. Weights are calculated using the dual-uniform
 * weighting function:
 * 
 * <blockquote> <img src="doc-files/DualUniformKNNClassifier-1.png">
 * </blockquote>
 * 
 * <p>
 * References:
 * <ol>
 *  <li> J. Gou, T. Xiong, Y. Kuang, A Novel Weighted Voting for K-Nearest
 *       Neighbor Rule, J. Comput. 6 (2011) 833–840.
 *       <a href="https://doi.org/10.4304/jcp.6.5.833-840">
 *          https://doi.org/10.4304/jcp.6.5.833-840</a>.
 * </ol>
 * 
 * @author Zoltán Gellér
 * @version 2025.03.05.
 * @see KNNClassifier
 */
public class DualUniformKNNClassifier extends KNNClassifier {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * dual-uniform weighting function, with the default number of nearest
     * neighbours ({@link KNNClassifier#k k}).
     */
	public DualUniformKNNClassifier() {
	}

	/**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * dual-uniform weighting function, with the given number of nearest neighbors
     * ({@code k}).
     * 
     * @param k number of nearest neighbours, must be {@code >= 1}
     */
	public DualUniformKNNClassifier(int k) {
		super(k);
	}
	
    /**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * dual-uniform weighting function, with the given distance measure
     * ({@code distance}) and with the default number of nearest neighbors
     * ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public DualUniformKNNClassifier(Distance distance) {
        super(distance);
    }
	
    /**
     * Constructs a new weighted kNN classifier utilizing the dual-uniform weighting
     * function, with the given number of nearest neighbors ({@code k}), and number
     * of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbors, must be {@code >= 1}
     * @param tnumber number of threads
     */
	public DualUniformKNNClassifier(int k, int tnumber) {
	    super(k, tnumber);
	}

	/**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * dual-uniform weighting function, with the given distance measure
     * ({@code distance}) and number of nearest neighbors ({@code k}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     */
	public DualUniformKNNClassifier(Distance distance, int k) {
		super(distance, k);
	}
	
    /**
     * Constructs a new weighted kNN classifier utilizing the dual-uniform weighting
     * function, with the given distance measure ({@code distance}), number of
     * nearest neighbors ({@code k}), and number of threads ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbors, must be {@code >= 1}
     * @param tnumber  number of threads
     */
	public DualUniformKNNClassifier(Distance distance, int k, int tnumber) {
	    super(distance, k, tnumber);
	}

	/**
	 * Finds the best label among the nearest neighbors. Weights are calculated
	 * using the dual weighted function based on the uniform function.
	 * 
	 * @param list sorted list of the nearest neighbors
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

			Map<Double, Integer> neighbours = new HashMap<Double, Integer>();

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

			Map<Double, Double> neighbours = new HashMap<Double, Double>();

			bestLabel = firstNode.obj.getLabel();
			double bestWeight = (lastDist - node.distance) / diff;

			neighbours.put(bestLabel, bestWeight);
			node = node.next;
			int index = 2;

			while (node != null) {

				double label = node.obj.getLabel();
				double weight = (lastDist - node.distance) / (index * diff);

				if (neighbours.containsKey(label))
					weight += neighbours.get(label);
				neighbours.put(label, weight);

				if (weight > bestWeight) {
					bestLabel = label;
					bestWeight = weight;
				}

				node = node.next;
				index++;
			}
		}

		return bestLabel;
	}

    @Override
    public Object makeACopy(boolean deep) {
        DualUniformKNNClassifier copy = new DualUniformKNNClassifier();
        init(copy, deep);
        return copy;
    }
	
}

package fap.classifier.NN;

import java.util.HashMap;

import fap.classifier.NN.util.LinkedDistanceNode;
import fap.classifier.NN.util.SortedList;
import fap.core.data.TimeSeries;
import fap.core.distance.Distance;

/**
 * Weighted kNN classifier utilizing the dual-uniform weighting function. The
 * class of a time series is determined by weighted voting of its k-nearest
 * neighbours in the training set. Weights are calculated using the dual-uniform
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
 * @author Zoltan Geller
 * @version 2024.09.17.
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
     * dual-uniform weighting function, with the given number of nearest neighbours
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
     * ({@code distance}) and with the default number of nearest neighbours
     * ({@link KNNClassifier#k k}).
     * 
     * @param distance distance measure
     */
    public DualUniformKNNClassifier(Distance distance) {
        super(distance);
    }
	
    /**
     * Constructs a new weighted kNN classifier utilizing the dual-uniform weighting
     * function, with the given number of nearest neighbours ({@code k}), and number
     * of threads ({@code tnumber}).
     * 
     * @param k       number of nearest neighbours, must be {@code >= 1}
     * @param tnumber number of threads
     */
	public DualUniformKNNClassifier(int k, int tnumber) {
	    super(k, tnumber);
	}

	/**
     * Constructs a new single-threaded weighted kNN classifier utilizing the
     * dual-uniform weighting function, with the given distance measure
     * ({@code distance}) and number of nearest neighbours ({@code k}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     */
	public DualUniformKNNClassifier(Distance distance, int k) {
		super(distance, k);
	}
	
    /**
     * Constructs a new weighted kNN classifier utilizing the dual-uniform weighting
     * function, with the given distance measure ({@code distance}), number of
     * nearest neighbours ({@code k}), and number of threads ({@code tnumber}).
     * 
     * @param distance distance measure
     * @param k        number of nearest neighbours, must be {@code >= 1}
     * @param tnumber  number of threads
     */
	public DualUniformKNNClassifier(Distance distance, int k, int tnumber) {
	    super(distance, k, tnumber);
	}

	/**
	 * Finds the best label among the nearest neighbours. Weights are calculated
	 * using the dual weighted function based on the uniform function.
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

package fap.classifier.NN;

import java.util.List;

import fap.classifier.NN.util.DistanceNode;
import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.core.distance.Distance;
import fap.exception.EmptyDatasetException;
import fap.exception.IncomparableTimeSeriesException;
import fap.util.ThreadUtils;

/**
 * 1NN (Nearest Neighbours) classifier.
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractNNClassifier
 */
public class NNClassifier extends AbstractNNClassifier {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new single-threaded 1NN classifier, without a distance measure.
     */
    public NNClassifier() {
    }
    
    /**
     * Constructs a new 1NN classifier with the specified number of threads ({@code tnumber}).
     * 
     * @param tnumber number of threads
     */
    public NNClassifier(int tnumber) {
        super(tnumber);
    }

    /**
     * Constructs a new single-threaded 1NN classifier, with the specified distance
     * measure ({@code distance}).
     * 
     * @param distance distance measure
     */
    public NNClassifier(Distance distance) {
        super(distance);
    }
    
    /**
     * Constructs a new 1NN classifier with the specified distance measure and
     * number of threads.
     * 
     * @param distance distance measure
     * @param tnumber  number of threads
     */
    public NNClassifier(Distance distance, int tnumber) {
        super(distance, tnumber);
    }

    @Override
    public void initialize(Dataset trainset) throws Exception {
        super.initialize(trainset);
        findKNeighbours(trainset, 1);
    }

    /**
     * Finds the the nearest neighbour (and its distance) of the specified time
     * series ({@code series}) in the given training set ({@code trainset}).
     * 
     * @param series   the time series whose nearest neighbor (and its distance) is
     *                 to be found
     * @param trainset the training set
     * @return a {@link DistanceNode} object containing the nearest neighbour of
     *         {@code series} in {@code trainset} and its distance
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    protected DistanceNode<TimeSeries> findNearestNeighbour(TimeSeries series, 
                                                            Dataset trainset) 
                                       throws Exception {

        double minDist = Double.POSITIVE_INFINITY;
        TimeSeries nearestNeighbour = null;

        // if the matrix of distances doesn't exists, we must use the distance measure
        if (distances == null)

            for (TimeSeries ts : trainset) {

                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                double dist = distance.distance(series, ts); // might throw IncomparableTimeSeriesException

                if (dist < minDist) {
                    minDist = dist;
                    nearestNeighbour = ts;
                }
                
            }

        // if the matrix of distances exists, we use it instead of the distance measure
        else {

            int sindex = series.getIndex();

            for (TimeSeries ts : trainset) {

                if (Thread.currentThread().isInterrupted())
                    throw new InterruptedException();

                double dist;

                int tindex = ts.getIndex();

                if (tindex < distances[sindex].length)
                    dist = distances[sindex][tindex];
                else
                    dist = distances[tindex][sindex];

                if (dist < minDist) {
                    minDist = dist;
                    nearestNeighbour = ts;
                }
                
            }

        }

        return new DistanceNode<TimeSeries>(nearestNeighbour, minDist);

    }

    /**
     * @throws EmptyDatasetException           if the training set is empty
     * @throws IncomparableTimeSeriesException if the series is incomparable with a
     *                                         series from the training set
     * @throws InterruptedException            if the thread has been interrupted
     */
    @Override
    public double classify(TimeSeries series) throws Exception {

        EmptyDatasetException.check(trainset);
        
        double label;

        // if the sorted list of nearest neighbours exists
        if (kNeighbours != null)
            label = kNeighbours.get(series.getIndex()).getFirst().obj.getLabel();

        // if the sorted list of nearest neighbours doesn't exist
        else {

            int tnumber = ThreadUtils.getThreadLimit(this.getNumberOfThreads());

            // if the number of threads is 1 or the matrix of distances exists
            if (tnumber < 2 || distances != null)
                label = findNearestNeighbour(series, trainset).obj.getLabel();
            else
                label = findNearestNeighbourMultithreaded(series, trainset, tnumber).obj.getLabel();

        }

        return label;
    }
    
    /**
     * Finds the nearest neighbour (and its distance) of the specified time series
     * ({@code series}) in the training set ({@code trainset}) relying on
     * {@code tnumber} of threads.
     * 
     * @param series   the time series to be classified
     * @param trainset the training set
     * @param tnumber  number of threads
     * @return a {@link DistanceNode} object containing the nearest neighbour of
     *         {@code series} in the training set ({@code trainset}) and its
     *         distance
     * @throws InterruptedException if the thread has been interrupted
     * @throws Exception            if an error occurs
     */
    protected DistanceNode<TimeSeries> findNearestNeighbourMultithreaded(TimeSeries series,
                                                                         Dataset trainset,
                                                                         int tnumber) 
                                       throws Exception {

        List<Double> results = this.findDistances(series, trainset, tnumber);
        
        double minDist = Double.POSITIVE_INFINITY;
        TimeSeries nearestNeighbour = null;
        
        for (int i = 0; i < results.size(); i++) {
            double dist = results.get(i);
            if (dist < minDist) {
                minDist = dist;
                nearestNeighbour = trainset.get(i);
            }
        }
        
        return new DistanceNode<TimeSeries>(nearestNeighbour, minDist);
    }

    @Override
    public Object makeACopy(boolean deep) {
        NNClassifier copy = new NNClassifier();
        init(copy, deep);
        return copy;
    }
     
}

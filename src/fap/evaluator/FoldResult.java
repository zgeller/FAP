package fap.evaluator;

import java.util.List;

import fap.core.data.Dataset;

/**
 * Auxiliary class for storing fold-related data.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 */
public class FoldResult {

    /**
     * The test set.
     */
    public transient Dataset testset;
    
    /**
     * The training set.
     */
    public transient Dataset trainset;
    
    /**
     * The error rate.
     */
    public double error;
    
    /**
     * The expected error rate (the lowest error rate on the training set). Default
     * value is {@code NaN}.
     */
    public double expectedError = Double.NaN;
    
    /**
     * The list of the values of the parameters that produced the smallest
     * classification error on the training set.
     */
    public List<? extends Comparable<?>> bestParams;
    
    /**
     * The number of misclassified time series.
     */
    public int misclassified;
    

}
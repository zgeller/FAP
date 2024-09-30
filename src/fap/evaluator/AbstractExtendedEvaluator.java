package fap.evaluator;

import java.util.concurrent.ThreadPoolExecutor;

import fap.callback.Callback;
import fap.callback.Callbackable;
import fap.core.evaluator.AbstractEvaluator;
import fap.util.Multithreaded;
import fap.util.Resumable;
import fap.util.ThreadUtils;

/**
 * Defines common methods and fields for callbackable, resumable, multithreaded
 * classifier evaluators.
 * 
 * @author Zoltan Geller
 * @version 2024.09.19.
 * @see AbstractEvaluator
 * @see Evaluator
 * @see Callbackable
 * @see Resumable
 * @see Multithreaded
 */
public abstract class AbstractExtendedEvaluator extends AbstractEvaluator implements Callbackable, Resumable, Multithreaded {

    private static final long serialVersionUID = 1L;

    /**
     * The executor service used for implementing multithreaded classification.
     */
    protected ThreadPoolExecutor executor;

    /**
     * The Callback object.
     */
    protected transient Callback callback;
    
    /**
     * The number of threads. Default value is {@code 1}.
     */
    protected int numberOfThreads = 1;
    
    /**
     * Indicates whether the testing is completed. Default value is {@code false}.
     */
    protected boolean done = false;

    /**
     * Indicates whether the testing has started. Default value is {@code false}.
     */
    protected boolean insideLoop = false;

    /**
     * Emptyt constructor (single-threaded).
     */
    public AbstractExtendedEvaluator() {
    }
    
    /**
     * Constructor with the number of threads.
     * 
     * @param tnumber number of threads.
     */
    public AbstractExtendedEvaluator(int tnumber) {
        this.setNumberOfThreads(tnumber);
    }
    
    /**
     * Initializes the executor service.
     * 
     * @param tnumber the number of threads
     * @see ThreadUtils#init
     */
    protected void initExecutor(int tnumber) {
        executor = ThreadUtils.init(executor, tnumber);
    }
    
    @Override
    public void setNumberOfThreads(int tnumber) {
        this.numberOfThreads = tnumber;
        initExecutor(tnumber);
    }

    @Override
    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }

    @Override
    public void shutdown() {
        if (executor != null)
            executor.shutdown();
    }
    
    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public Callback getCallback() {
        return this.callback;
    }

    @Override
    public void reset() {
        this.done = false;
        this.insideLoop = false;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public boolean isInProgress() {
        return this.insideLoop;
    }

}

package fap.distance;

/**
 * Defines basic methods for constrained elastic distance measures.
 * 
 * @author Zoltan Geller
 * @version 2024.09.17.
 * @see AbstractCopyableDistance
 * @see ConstrainedDistance
 */
public abstract class AbstractConstrainedDistance extends AbstractCopyableDistance implements ConstrainedDistance {

    private static final long serialVersionUID = 1L;

    /**
     * The relative width of the warping (editing) window (as a percentage of the
     * length of the time series). A negative value indicates that the absolute
     * width ({@link #w}) should be used. Default value is {@code 100}.
     */
    private double r = 100;

    /**
     * The absolute width of the warping (editing) window. A negative value
     * indicates that the relative width ({@link #r}) should be used. Default value
     * is {@code -1}.
     */
    private int w = -1;
    
    /**
     * Empty constructor.
     */
    public AbstractConstrainedDistance() {
    }
    
    /**
     * Constructor with the possibility to enable or disable storing distances.
     * 
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractConstrainedDistance(boolean storing) {
        super(storing);
    }
    
    /**
     * Constructor with warping-window width.
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series)
     */
    public AbstractConstrainedDistance(double r) {
        this.setR(r);
    }
    
    /**
     * Constructor with the warping-window width and the possibility to enable or
     * disable storing distances.
     * 
     * @param r       the relative width of the warping (editing) window (as a
     *                percentage of the length of the time series)
     * @param storing {@code true} if storing distances should be enabled
     */
    public AbstractConstrainedDistance(double r, boolean storing) {
        super(storing);
        this.setR(r);
    }

    /**
     * Sets the relative width of the warping (editing) window (as a percentage of
     * the length of the time series). Must be in the range {@code [0..100]}. The
     * absolute width is set to -1 (i.e. {@link #w} {@code = -1}).
     * 
     * @param r the relative width of the warping (editing) window (as a percentage
     *          of the length of the time series), must be in range {@code [0..100]}
     * @throws IllegalArgumentException if {@code r} is not in the range
     *                                  {@code [0..100]}
     */
    @Override
    public void setR(double r) {

        if (r < 0 || r > 100)
            throw new IllegalArgumentException("r must be in the range [0..100]");
        
        if (this.r != r) {
            this.clearStorage();
            this.r = r;
            this.w = -1;
        }

    }

    @Override
    public double getR() {
        return this.r;
    }

    /**
     * Sets the absolute width of the warping (editing) window. Must be
     * {@code w >= 0}. The relative width is set to -1 (i.e. {@link #r}
     * {@code = -1}).
     * 
     * @param w the absolute width of the warping (editing) window, must be
     *          {@code >= 0}
     * @throws IllegalArgumentException if {@code w < 0}
     */
    @Override
    public void setW(int w) {

        if (w < 0)
            throw new IllegalArgumentException("Must be w >= 0.");

        if (this.w != w) {
            this.clearStorage();
            this.w = w;
            this.r = -1;
        }

    }

    @Override
    public int getW() {
        return this.w;
    }

    /**
     * Initializes the specified distance meausre with the common data structures of this
     * distance measure.
     * 
     * @param copy the distance meausre whose data structures is to be initialized
     */
    protected void init(AbstractConstrainedDistance copy, boolean deep) {
        super.init(copy, deep);
        double r = this.getR();
        int w = this.getW();
        if (r >= 0)
            copy.setR(r);
        if (w >= 0)
            copy.setW(w);
    }

    @Override
    public String toString() {
        return super.toString() + ", r=" + getR() + ", w=" + getW();
    }
    
}

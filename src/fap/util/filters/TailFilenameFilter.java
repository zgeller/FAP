package fap.util.filters;

import java.io.File;

import fap.util.StringUtils;

/**
 * {@code TailFilenameFilter}
 * 
 * @author Zoltan Geller
 * @version 2016.03.18.
 * @see AbstractFilenameFilter
 */
public class TailFilenameFilter extends AbstractFilenameFilter {
	
    /**
     * Array of tails. Default value is {@code null}.
     */
    private String[] tails = null;
	
    /**
     * Indicates whether to ignore case. Default value is {@code true}.
     */
    private boolean ignoreCase = true;
	
    /**
     * Creates a new {@code TailFilenameFilter} with an array of tails and with
     * {@code ignoreCase=true}.
     * 
     * @param tails array of tails
     */
    public TailFilenameFilter(String... tails) {
        this(true, tails);
    }
	
    /**
     * Creates a new {@code TailFilenameFilter} with an array of tails.
     * 
     * @param ignoreCase the ignoreCase
     * @param tails      array of tails
     */
    public TailFilenameFilter(boolean ignoreCase, String... tails) {
        this.setIgnoreCase(ignoreCase);
        this.setTails(tails);
    }

    /**
     * Returns the tails.
     * 
     * @return the tails
     */
    public String[] getTails() {
        return tails;
    }

    /**
     * Sets the tails.
     * 
     * @param tails the tails to set
     */
    public void setTails(String... tails) {
        this.tails = tails;
    }
	
    /**
     * Returns the ignoreCase.
     * 
     * @return the ignoreCase
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Sets the ignoreCase.
     * 
     * @param ignoreCase the ignoreCase to set
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
	
    @Override
    public boolean accept(File dir, String name) {

        if (tails == null)
            return true;

        if (new File(dir, name).isDirectory())
            return isAcceptFolders();

        if (ignoreCase) {
            for (String suffix : tails)
                if (StringUtils.endsWithIgnoreCase(name, suffix))
                    return true;
        }

        else {
            for (String suffix : tails)
                if (suffix != null && name.endsWith(suffix))
                    return true;
        }

        return false;

    }

}
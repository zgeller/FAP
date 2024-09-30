package fap.util.filters;

import java.io.FilenameFilter;

/**
 * Abstract {@code FilenameFilter} class. Defines basic fields and methods. By
 * default, it doesn't accept directories.
 * 
 * @author Zoltan Geller
 * @version 2016.03.18.
 * @see java.io.FilenameFilter
 */
public abstract class AbstractFilenameFilter implements FilenameFilter {

    /**
     * Determines whether to accept folders too. Default value is {@code false}
     */
    private boolean acceptFolders = false;

    /**
     * Returns the value of {@code acceptFolder}.
     * 
     * @return the acceptFolders
     */
    public boolean isAcceptFolders() {
        return acceptFolders;
    }

    /**
     * Sets the {@code acceptFolders}
     * 
     * @param acceptFolders the acceptFolders to set
     */
    public void setAcceptFolders(boolean acceptFolders) {
        this.acceptFolders = acceptFolders;
    }

}

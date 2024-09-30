package fap.util.filters;

import javax.swing.filechooser.FileFilter;

/**
 * Abstract {@code FileFilter} class. Defines basic fields and methods. By
 * default, it accepts directories.
 * 
 * @author Zoltan Geller
 * @version 2013.09.19.
 * @see javax.swing.filechooser.FileFilter
 * @see java.io.FileFilter
 */
public abstract class AbstractFileFilter extends FileFilter implements
		java.io.FileFilter {

    /**
     * The description.
     */
    private String description = "";
	
    /**
     * Determines whether to accept folders too. Default value is {@code true}
     */
    private boolean acceptFolders = true;

    /**
     * Sets the description of the filter.
     * 
     * @param description - the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        if (description != null)
            return this.description;
        else
            return "";
    }

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

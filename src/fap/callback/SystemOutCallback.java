package fap.callback;

/**
 * A simple extension of the {@code AbstractCallback} class. Just prints out
 * characters through the standard output.
 * 
 * @author Zoltan Geller
 * @version 2024.09.11.
 * @see AbstractCallback
 */
public class SystemOutCallback extends AbstractCallback {

    /**
     * The character to print out. Default value is {@code *}.
     */
    private char character = '*';

    /**
     * Constructs a new SystemOut callback with the default desired number of
     * callbacks.
     */
    public SystemOutCallback() {
    }

    /**
     * Constructs a new SystemOut callback with the specified desired number of
     * callbacks.
     * 
     * @param desiredCBNumber the desired number of callbacks
     */
    public SystemOutCallback(int desiredCBNumber) {
        super(desiredCBNumber);
    }

    /**
     * Creates a new {@code SystemOutCallback} object.
     * 
     * @param desiredCBNumber the number of desired callbacks
     * @param character       the character to print out
     */
    public SystemOutCallback(int desiredCBNumber, char character) {
        super(desiredCBNumber);
        setCharacter(character);
    }
    
    /**
     * Returns the character.
     * 
     * @return the character
     */
    public char getCharacter() {
        return character;
    }

    /**
     * Sets the character.
     * 
     * @param character the character to set
     */
    public void setCharacter(char character) {
        this.character = character;
    }

    @Override
    public void setCallbackCount(int cbCount) {
        super.setCallbackCount(cbCount);
        if (cbCount < 0)
            System.out.println();
        else {
            int progress = this.getProgress();
            for (int i = 0; i < progress; i++)
                System.out.print(character);
        }
    }

    @Override
    public void callback(Object object) throws Exception {
        int lastProgress = this.getProgress();
        super.callback(object);
        int newProgress = this.getProgress();
        for (int i = lastProgress; i < newProgress; i++)
            System.out.print(character);
    }

}

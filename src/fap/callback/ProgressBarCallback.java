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

package fap.callback;

import javax.swing.JProgressBar;

/**
 * A simple extension of the {@code AbstractCallback} class using a progress
 * bar.
 * 
 * @author Zoltán Gellér
 * @version 2024.09.11.
 * @see AbstractCallback
 */
public class ProgressBarCallback extends AbstractCallback {

    /**
     * The progress bar.
     */
    private JProgressBar pBar;

    /**
     * Constructs a new ProgressBar callback with the default desired number of
     * callbacks.
     */
    public ProgressBarCallback() {
    }

    /**
     * Constructs a new ProgressBar callback with the specified desired number of
     * callbacks.
     * 
     * @param desiredCBNumber the desired number of callbacks
     */
    public ProgressBarCallback(int desiredCBNumber) {
        super(desiredCBNumber);
    }

    /**
     * Creates a new {@code ProgressBarCallback} object.
     * 
     * @param desiredCBNumber the desired number of callbacks
     * @param pBar            the progress bar
     */
    public ProgressBarCallback(int desiredCBNumber, JProgressBar pBar) {
        super(desiredCBNumber);
        this.setpBar(pBar);
    }
    
    /**
     * Returns the progress bar.
     * 
     * @return the pBar
     */
    public JProgressBar getpBar() {
        return pBar;
    }

    /**
     * Sets the progress bar.
     * 
     * @param pBar the pBar to set
     */
    public void setpBar(JProgressBar pBar) {
        this.pBar = pBar;
    }


    /**
     * Maps from the interval {@code [0...desiredCBNumber]} to interval
     * {@code [pBar.getMinimum()...pBar.getMaximum()]}.
     * 
     * @param progress the value to map from the interval
     *                 {@code [0...desiredCBNumber]} to interval
     *                 {@code [pBar.getMinimum()...pBar.getMaximum()}.
     * @return the mapped value
     */
    private int getPBarValue(int progress) {
        
        if (progress > this.desiredCBNumber)
            progress = this.desiredCBNumber;
        else if (progress < 0)
            progress = 0;
        
        int min = pBar.getMinimum();
        int max = pBar.getMaximum();
        
        if (min > max)
            max--;
        else
            max++;
        
        return min + (int) ((progress / (double) this.desiredCBNumber) * (max - min));
    }

    @Override
    public void setCallbackCount(int cbCount) {
        super.setCallbackCount(cbCount);
        pBar.setValue(getPBarValue(this.getProgress()));
        if (cbCount < 0)
            pBar.setValue(pBar.getMaximum());
    }

    @Override
    public void callback(Object object) throws Exception {
        super.callback(object);
        pBar.setValue(this.getPBarValue(this.getProgress()));
    }

}

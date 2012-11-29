/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import javax.swing.JPanel;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public abstract class AbstractOptionsPanel extends JPanel {
	private final MarvisGraphMainWindow main_window;
	public AbstractOptionsPanel(MarvisGraphMainWindow w){
		this.main_window = w;
	}
	
	public MarvisGraphMainWindow getMainWindow(){
		return main_window;
	}
	
	abstract public void updateOptions();
	
	abstract public AbstractNetworkCreator getProcess();
}

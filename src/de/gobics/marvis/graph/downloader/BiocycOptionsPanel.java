package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.utils.swing.FilechooserTextField;
import de.gobics.marvis.utils.swing.filechooser.ChooserDirectory;
import java.io.File;
import java.util.logging.Logger;

/**
 * Panel to load flatfiles from the BioCyc Collection
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class BiocycOptionsPanel extends AbstractOptionsPanel{
	private static final Logger logger = Logger.getLogger(BiocycOptionsPanel.class.getName());
	private final FilechooserTextField chooser = new FilechooserTextField(ChooserDirectory.getInstance());
	
	public BiocycOptionsPanel(MarvisGraphMainWindow w){
		super(w);
		add(chooser);
		chooser.setSelectedFile(new File("/c1/scratch/MarVis-Scratch/BioCyc/biocyc-16.0/ara/8.0"));
	}

	@Override
	public void updateOptions() {
		// nothing to do here
	}

	@Override
	public AbstractNetworkCreator getProcess() {
		File selected = chooser.getSelectedFileWithShow();
		return new BiocycCreateNetworkProcess( selected );
	}
	
}

package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.utils.swing.FilechooserTextField;
import de.gobics.marvis.utils.swing.SpringUtilities;
import de.gobics.marvis.utils.swing.filechooser.ChooserTarGz;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.SpringLayout;

/**
 * Panel to load flatfiles from the BioCyc Collection
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class BiocycOptionsPanel extends AbstractOptionsPanel{
	private static final Logger logger = Logger.getLogger(BiocycOptionsPanel.class.getName());
	private final FilechooserTextField chooser = new FilechooserTextField(ChooserTarGz.getInstance());
	private final JCheckBox cb_variants = new JCheckBox("Create reactions variants", true);
	
	public BiocycOptionsPanel(MarvisGraphMainWindow w){
		super(w);
		setLayout(new SpringLayout());
		add(chooser);
		add(cb_variants);
		SpringUtilities.makeCompactGrid(this, 2, 1);
		chooser.setSelectedFile(new File("/home/manuel/ara.tar.gz"));
	}

	@Override
	public void updateOptions() {
		// nothing to do here
	}

	@Override
	public AbstractNetworkCreator getProcess() {
		File selected = chooser.getSelectedFileWithShow();
		if( selected == null || !selected.exists()){
			return null;
		}
		BiocycCreateNetworkProcess process = new BiocycCreateNetworkProcess( selected );
		process.setCreateReactionVariants(cb_variants.isSelected());
		return process;
	}
	
}

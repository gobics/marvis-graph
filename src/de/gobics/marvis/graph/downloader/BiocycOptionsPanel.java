package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.utils.swing.FilechooserTextField;
import de.gobics.marvis.utils.swing.filechooser.ChooserTarGz;
import java.awt.FlowLayout;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;

/**
 * Panel to load flatfiles from the BioCyc Collection
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class BiocycOptionsPanel extends AbstractOptionsPanel {

	private static final Logger logger = Logger.getLogger(BiocycOptionsPanel.class.getName());
	private final FilechooserTextField chooser = new FilechooserTextField(ChooserTarGz.getInstance());
	private final JComboBox<String> cb_strategy = new JComboBox<>(new String[]{
				"Remove metabolite classes from reactions",
				"Use metabolite classes as metabolites",
				"Substitute classes with metabolites",
				"Build reactions for each variant"
			});

	public BiocycOptionsPanel(MarvisGraphMainWindow w) {
		super(w);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		chooser.setSelectedFile(new File("/home/manuel/ara.tar.gz"));
		add(chooser);
		add(cb_strategy);

	}

	@Override
	public void updateOptions() {
		// nothing to do here
	}

	@Override
	public AbstractNetworkCreator getProcess() {
		File selected = chooser.getSelectedFileWithShow();
		if (selected == null || !selected.exists()) {
			return null;
		}
		BiocycCreateNetworkProcess process = new BiocycCreateNetworkProcess(selected);
		int idx = cb_strategy.getSelectedIndex();
		if (idx == 0) {
			process.setClassesStrategy(BiocycCreateNetworkProcess.ClassesStategy.RemoveClasses);
		}
		else if (idx == 1) {
			process.setClassesStrategy(BiocycCreateNetworkProcess.ClassesStategy.AsMetabolite);
		}
		else if (idx == 2) {
			process.setClassesStrategy(BiocycCreateNetworkProcess.ClassesStategy.SingleReaction);
		}
		else if (idx == 3) {
			process.setClassesStrategy(BiocycCreateNetworkProcess.ClassesStategy.ReactionVariants);
		}
		else {
			throw new RuntimeException("Unkown selection " + idx + ": " + cb_strategy.getItemAt(idx));
		}

		return process;
	}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.gui.ErrorDialog;
import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.utils.swing.AbstractTask;
import de.gobics.marvis.utils.swing.Statusdialog;
import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javacyc.Javacyc;
import javax.swing.SwingWorker;

/**
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class PwtoolsOptionsPanel extends AbstractOptionsPanel {

	private static final Logger logger = Logger.getLogger(PwtoolsOptionsPanel.class.
			getName());
	private final ComboBoxOrganisms organisms = new ComboBoxOrganisms();

	public PwtoolsOptionsPanel(MarvisGraphMainWindow main_window) {
		super(main_window);
		setLayout(new BorderLayout());
		add(organisms, BorderLayout.CENTER);
	}

	@Override
	public void updateOptions() {
		if (!new File("/tmp/ptools-socket").exists()) {
			ErrorDialog.show(null, "No running Pathway Tools instance found");
			return;
		}
		SwingWorker<OrganismDescription[], Void> process = new AbstractTask<OrganismDescription[], Void>() {

			@Override
			public OrganismDescription[] performTask() throws Exception {
				Javacyc cyc = new Javacyc("META");
				ArrayList<String> pgdbs = cyc.allPGDBs();
				OrganismDescription[] orgs = new OrganismDescription[pgdbs.size()];

				for (int i = 0; i < orgs.length; i++) {
					orgs[i] = new OrganismDescription(
							pgdbs.get(i),
							cyc.getOrganismNameWithStrainAndGenomeSource(pgdbs.
							get(i)));
				}
				return orgs;
			}
		};

		getMainWindow().monitorTask(process);
		process.execute();
		try {
			organisms.setOrganisms(process.get());
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Can not get result of Orgism listing: ", ex);
			getMainWindow().display_error("Can not get result of organism listing", ex);
		}

	}

	@Override
	public AbstractNetworkCreator getProcess() {
		return new PwtoolsCreateNetworkProcess(organisms.getSelectedOrganismID());
	}
}

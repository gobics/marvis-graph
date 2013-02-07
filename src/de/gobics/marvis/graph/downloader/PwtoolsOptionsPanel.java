/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.gui.ErrorDialog;
import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import de.gobics.marvis.utils.task.AbstractTask;
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
		AbstractTask<OrganismDescription[], Void> process = new AbstractTask<OrganismDescription[], Void>() {
			@Override
			public OrganismDescription[] doTask() throws Exception {
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

		getMainWindow().executeTask(process);
		if (process.isDone()) {
			organisms.setOrganisms(process.getTaskResult());
		}

	}

	@Override
	public AbstractNetworkCreator getProcess() {
		return new PwtoolsCreateNetworkProcess(organisms.getSelectedOrganismID());
	}
}

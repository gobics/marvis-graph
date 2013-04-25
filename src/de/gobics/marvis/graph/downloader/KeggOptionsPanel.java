package de.gobics.marvis.graph.downloader;

import de.gobics.keggapi.KeggAPI;
import de.gobics.keggapi.KeggMysqlCache;
import de.gobics.marvis.graph.gui.ErrorDialog;
import de.gobics.marvis.graph.gui.MarvisGraphMainWindow;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The options panel to create a new network based on the KEGG API.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class KeggOptionsPanel extends AbstractOptionsPanel {

	private static final Logger logger = Logger.getLogger(KeggOptionsPanel.class.
			getName());
	/**
	 * Access to the KEGG Database via the REST API.
	 */
	private KeggAPI keggapi = new KeggAPI();
	/**
	 * KEGG provides access to several databases. Therefore, the user has to
	 * select a specific organism, which can be done here.
	 */
	private final ComboBoxOrganisms organisms = new ComboBoxOrganisms();

	public KeggOptionsPanel(MarvisGraphMainWindow w) {
		super(w);
		setLayout(new BorderLayout());
		add(organisms, BorderLayout.CENTER);
		try {
			logger.info("Try to connect to MySQL KEGG cache");
			keggapi = new KeggMysqlCache();
			logger.info("Succesfully connected to MySQL cache");
		}
		catch (Exception ex) {
			logger.log(Level.INFO, "Can not instanciate MySQL cache. Using default API: ", ex);
		}
	}

	@Override
	public void updateOptions() {
		try {
			String[] org_lines = keggapi.fetch("list", "organism").split("\n");
			OrganismDescription[] orgs = new OrganismDescription[org_lines.length + 1];
			orgs[0] = new OrganismDescription("ko", "KEGG Ortholog pathways (ko)");

			for (int i = 0; i < org_lines.length; i++) {
				String[] token = org_lines[i].split("\t");
				orgs[i + 1] = new OrganismDescription(token[1], token[2]);
			}
			Arrays.sort(orgs, 1, orgs.length - 1, new Comparator<OrganismDescription>() {

				@Override
				public int compare(OrganismDescription t, OrganismDescription t1) {
					return t.name.compareTo(t1.name);
				}
			});
			organisms.setOrganisms(orgs);
		}
		catch (Exception ex) {
			Logger.getLogger(KeggOptionsPanel.class.getName()).
					log(Level.SEVERE, null, ex);
			ErrorDialog.show(null, "Can not load organisms", ex);
		}
	}

	@Override
	public AbstractNetworkCreator getProcess() {
		KeggCreateNetworkProcess process = new KeggCreateNetworkProcess(organisms.
				getSelectedOrganismID());
		process.setKeggAPI(keggapi);
		return process;
	}
}

package de.gobics.marvis.graph.gui.tasks;

import java.io.*;
import de.gobics.marvis.graph.*;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

/**
 * Process to import metabolic marker data from a CSV file into the graph.
 * The original graph will not be modified but an altered version will be returned.
 * If an error happens. The old graph is still valid.
 * 
 * @author manuel
 */
public class ImportMetabolicMarkerCef extends AbstractTask<MetabolicNetwork, Void> {

	private static final Logger logger = Logger.getLogger(ImportMetabolicMarkerCef.class.
			getName());
	private MetabolicNetwork network;
	private File[] filenames = new File[0];
	private String[] condition_names = new String[0];
	
	public ImportMetabolicMarkerCef(final MetabolicNetwork graph) {
		this.network = graph.clone();
	}

	public void setInputFile(File datafile) {
		setInputFiles(new File[]{datafile});
	}
	public void setInputFiles(File[] datafiles) {
		this.filenames = datafiles;
	}

	public void setIntensityMapping(String[] condition_names) {
		this.condition_names = condition_names;
	}

	public MetabolicNetwork importMarker() throws IOException, JDOMException {
		for (File current_file : filenames) {
			importMarker(current_file);
		}
		return network;
	}

	public MetabolicNetwork importMarker(File current_file) throws IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new FileInputStream(current_file));
		Element root = doc.getRootElement();

		setProgress(0);

		logger.finer("Importing objects");
		List<Element> children = root.getChildren("Compound");
		int counter = 1;
		setProgressMax(children.size());
		
		for (Element e : children) {
			Marker marker = network.createMarker("m"+counter);
			
			List<Element> list_location = e.getChildren("Location");
			if( list_location.size() != 1 ){
				throw new RuntimeException("Can not determine location child of: "+e);
			}
			Element location = list_location.get(0);
			
			double mass = new Double( location.getAttributeValue("m") );
			double rt = new Double( location.getAttributeValue("rt"));
			
			marker.setMass(mass);
			marker.setRetentionTime((float)rt);
			
			//FIXME: Ich brauch intensitaeten
			
			incrementProgress();
		}

		setProgress(100);
		logger.info("MetabolicNetwork ready");
		return network;
	}

	public int getNumberOfLines(File current_file) throws IOException {
		FileReader fr = new FileReader(current_file);
		BufferedReader br = new BufferedReader(fr);
		int count = 0;
		while (br.readLine() != null) {
			count++;
		}
		return count;
	}

	@Override
	protected MetabolicNetwork performTask() throws Exception {
		return importMarker();
	}
}

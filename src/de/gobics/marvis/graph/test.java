package de.gobics.marvis.graph;

import de.gobics.marvis.graph.downloader.AbstractNetworkCreator;
import de.gobics.marvis.graph.downloader.KeggCreateNetworkProcess;
import de.gobics.marvis.graph.gui.tasks.AbstractTaskListener;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksPathway;
import de.gobics.marvis.graph.gui.tasks.CalculateNetworksReaction;
import de.gobics.marvis.graph.gui.tasks.LoadNetwork;
import de.gobics.marvis.utils.LoggingUtils;
import de.gobics.marvis.utils.StringUtils;
import de.gobics.marvis.utils.swing.Heatmap;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author manuel
 */
public class test {

	private final static Logger logger = Logger.getLogger(test.class.getName());

	public static void main(String[] args) throws Exception {
		LoggingUtils.initLogger(Level.FINER);
		LoadNetwork loader;
		MetabolicNetwork network;
		KeggCreateNetworkProcess downloader;


		loader = new LoadNetwork("/c1/scratch/manuel/mg-paper/graph.ath.marker.transcripts.full.xml.gz");
		network = loader.load();

		Set<Transcript> transcript_set = getTranscripts(network, network.getPathway("path:ath00592"));
		System.out.println("id;"+StringUtils.join(";", transcript_set.iterator().next().getRawIntensityNames()));
		for(Transcript t: transcript_set){
			System.out.println(t.getId() + ";"+ StringUtils.join(";", t.getRawIntensitiesAsFloat()));
		}


		Transcript[] transcripts = transcript_set.toArray(new Transcript[transcript_set.
				size()]);
		double[][] data = new double[transcripts.length][transcripts[0].
				getRawIntensities().length];
		Transcript[] labels_x = transcripts.clone();
		String[] labels_y = transcripts[0].getRawIntensityNames();
		for (int midx = 0; midx < transcripts.length; midx++) {
			float[] intensities = transcripts[midx].getRawIntensities();
			for (int iidx = 0; iidx < intensities.length; iidx++) {
				data[midx][iidx] = (double) intensities[iidx];
			}
		}

		Heatmap heatmap_transcripts = new Heatmap(data);
		heatmap_transcripts.setLabelX(labels_x);
		heatmap_transcripts.setLabelY(labels_y);
		
		JFrame frame = new JFrame();
		frame.add(heatmap_transcripts);
		frame.setVisible(true);
	}

	private static Set<Transcript> getTranscripts(MetabolicNetwork network, Pathway pathway) {
		Set<Transcript> transcripts = new TreeSet<Transcript>();

		for (Reaction r : network.getReactions(pathway)) {
			for (Enzyme e : network.getEnzymes(r)) {
				for (Gene g : network.getEncodingGenes(e)) {
					transcripts.addAll(network.getTranscripts(g));
				}
			}
		}

		return transcripts;
	}
}

package de.gobics.marvis.graph.downloader;

import de.gobics.marvis.graph.*;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * This class can be used to generate a report about a metabolic network.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class MetabolicNetworkTester {

	private static final Logger logger = Logger.getLogger(MetabolicNetworkTester.class.
			getName());
	private final MetabolicNetwork network;

	public MetabolicNetworkTester(MetabolicNetwork network) {
		this.network = network;
	}

	public String generateReport() {
		StringBuilder sb = new StringBuilder("Report for metabolic network: " + network.
				getName() + "\n");

		sb.append("Network contains:\n");
		sb.append("  Compounds: ").append(network.getCompounds().size()).append("\n");
		sb.append("  Reactions: ").append(network.getReactions().size()).append("\n");
		sb.append("  Enzymes:   ").append(network.getEnzymes().size()).append("\n");
		sb.append("  Genes:     ").append(network.getGenes().size()).append("\n");
		sb.append("  Pathways:  ").append(network.getPathways().size()).append("\n");
		if (network.hasMarkers()) {
			sb.append("  Marker  :  ").append(network.getMarkers().size()).
					append("\n");
		}
		if (network.hasTranscripts()) {
			sb.append("  Transcripts:  ").append(network.getTranscripts().size()).
					append("\n");
		}

		sb.append("\nThere are...\n");
		sb.append("  ").append(countCompoundWithoutReaction()).append(" compound without reaction\n");
		sb.append("  ").append(countReactionWithoutPathway()).append(" reactions without pathway\n");
		sb.append("  ").append(countEnzymesWithoutReaction()).append(" enzymes without reaction\n");
		sb.append("  ").append(countGenesWithoutEnzymes()).append(" genes without enzymes\n");
		if (network.hasMarkers()) {
			sb.append("  ").append(countMarkerWithoutReaction()).append(" marker without link to reaction (").
					append(network.getMarkers().size() - countMarkerWithoutReaction()).
					append(" with)\n");

		}
		if (network.hasTranscripts()) {
			sb.append("  ").append(countTranscriptsWithoutReaction()).append(" transcripts without link to reaction(").
					append(network.getTranscripts().size() - countTranscriptsWithoutReaction()).
					append(" with)\n");
		}


		return sb.toString();
	}

	public int countCompoundWithoutReaction() {
		int counter = 0;
		for (Compound c : network.getCompounds()) {
			if (!assertFound(network.getReactions(c))) {
				counter++;
			}
		}
		return counter;
	}

	public int countReactionWithoutPathway() {
		int counter = 0;
		for (Reaction r : network.getReactions()) {
			if (!assertFound(network.getPathways(r))) {
				counter++;
			}
		}
		return counter;
	}

	public int countEnzymesWithoutReaction() {
		int counter = 0;
		for (Enzyme e : network.getEnzymes()) {
			if (!assertFound(network.getReactions(e))) {
				counter++;
			}
		}
		return counter;
	}

	public int countGenesWithoutEnzymes() {
		int counter = 0;
		for (Gene e : network.getGenes()) {
			if (!assertFound(network.getEncodedEnzymes(e))) {
				counter++;
			}
		}
		return counter;
	}

	public int countMarkerWithoutReaction() {
		int counter = 0;
		for (Marker m : network.getMarkers()) {
			Set<Reaction> reactions = new TreeSet<Reaction>();

			for (Compound c : network.getAnnotations(m)) {
				reactions.addAll(network.getReactions(c));
			}

			if (!assertFound(reactions)) {
				counter++;
			}
		}
		return counter;
	}

	public int countTranscriptsWithoutReaction() {
		int counter = 0;
		for (Transcript t : network.getTranscripts()) {
			Set<Reaction> reactions = new TreeSet<Reaction>();

			for (Gene g : network.getGenes(t)) {
				for (Enzyme e : network.getEncodedEnzymes(g)) {
					reactions.addAll(network.getReactions(e));
				}
			}

			if (!assertFound(reactions)) {
				counter++;
			}
		}
		return counter;
	}

	private boolean assertFound(Collection<? extends GraphObject> list) {
		return list != null && !list.isEmpty();
	}
}

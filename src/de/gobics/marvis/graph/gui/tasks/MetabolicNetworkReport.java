package de.gobics.marvis.graph.gui.tasks;

import de.gobics.marvis.graph.*;
import de.gobics.marvis.utils.task.AbstractTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * This class can be used to generate a report about a metabolic network.
 *
 * @author Manuel Landesfeind &lt;manuel@gobics.de&gt;
 */
public class MetabolicNetworkReport extends AbstractTask<String, Void> {

	private static final Logger logger = Logger.getLogger(MetabolicNetworkReport.class.
			getName());
	private final MetabolicNetwork network;

	public MetabolicNetworkReport(MetabolicNetwork network) {
		this.network = network;
		setTaskTitle("Network report");
		setTaskDescription("Generating report for network");
	}

	@Override
	protected String doTask() throws Exception {
		return generateReport();
	}

	public String generateReport() {
		StringBuilder sb = new StringBuilder("Report for metabolic network: " + network.
				getName() + "\n");

		sb.append("Network contains:\n");
		if (network.hasMarkers()) {
			sb.append("  Metabolic marker  :  ").append(network.getMarkers().size()).append("\n");
			sb.append("            ").append(countMarkerWithCompound()).append(" linked to a metabolite\n");
		}
		sb.append("  Compounds: ").append(network.getCompounds().size()).append("\n");
		if (network.hasMarkers()) {
			sb.append("            ").append(countCompoundsWithMarker()).append(" with marker candidate\n");
		}
		if (network.hasTranscripts()) {
			sb.append("  Transcripts:  ").append(network.getTranscripts().size()).
					append("\n");
			sb.append("            ").append(countTranscriptsWithGene()).append(" linked to a gene\n");
		}
		sb.append("  Genes:     ").append(network.getGenes().size()).append("\n");
		if (network.hasTranscripts()) {
			sb.append("            ").append(countGenesWithTranscript()).append(" with transcript\n");
		}
		sb.append("  Enzymes:   ").append(network.getEnzymes().size()).append("\n");
		if (network.hasTranscripts()) {
			sb.append("            ").append(countEnzymesWithTranscript()).append(" with transcript\n");
		}

		sb.append("  Reactions: ").append(network.getReactions().size()).append("\n");

		sb.append("  Pathways:  ").append(network.getPathways().size()).append("\n");


		sb.append("\nLinks to reactions\n");
		if (network.hasMarkers()) {
			sb.append("  ").append(countMissingReactionLink(network.getMarkers())).append(" metabolic marker without reaction\n");
		}
		sb.append("  ").append(countMissingReactionLink(network.getCompounds())).append(" metabolites without reaction\n");

		if (network.hasTranscripts()) {
			sb.append("  ").append(countMissingReactionLink(network.getTranscripts())).append(" transcripts without reaction\n");
		}
		sb.append("  ").append(countMissingReactionLink(network.getGenes())).append(" genes without reaction\n");
		sb.append("  ").append(countMissingReactionLink(network.getEnzymes())).append(" enzymes without reaction\n");
		sb.append("  ").append(countMissingReactionLink(network.getPathways())).append(" pathways without reaction\n");


		sb.append("\nLinks to pathways\n");
		if (network.hasMarkers()) {
			sb.append("  ").append(countMissingPathwayLink(network.getMarkers())).append(" metabolic marker without pathway\n");
		}
		sb.append("  ").append(countMissingPathwayLink(network.getCompounds())).append(" metabolites without pathway\n");
		sb.append("  ").append(countMissingPathwayLink(network.getReactions())).append(" reactions without pathway\n");

		if (network.hasTranscripts()) {
			sb.append("  ").append(countMissingPathwayLink(network.getTranscripts())).append(" transcripts without pathway\n");
		}
		sb.append("  ").append(countMissingPathwayLink(network.getGenes())).append(" genes without pathway\n");
		sb.append("  ").append(countMissingPathwayLink(network.getEnzymes())).append(" enzymes without pathway\n");




		if (network.hasMarkers()) {
			sb.append("  ").append(countMissingReactionLink(network.getMarkers())).append(" marker without link to reaction (").
					append(network.getMarkers().size() - countMissingReactionLink(network.getMarkers())).
					append(" with)\n");
			int count = countUniqueReactionsReachableFrom(network.getMarkers());
			sb.append("     reaching ").append(count).append(" distinct reactions\n");

		}
		if (network.hasTranscripts()) {
			sb.append("  ").append(countMissingReactionLink(network.getTranscripts())).append(" transcripts without link to reaction (").
					append(network.getTranscripts().size() - countMissingReactionLink(network.getTranscripts())).
					append(" with)\n");
			int count = countUniqueReactionsReachableFrom(network.getTranscripts());
			sb.append("     reaching ").append(count).append(" distinct reactions\n");
		}


		return sb.toString();
	}

	private int countMarkerWithCompound() {
		int counter = 0;
		for (Marker m : network.getMarkers()) {
			if (assertFound(network.getAnnotations(m))) {
				counter++;
			}
		}
		return counter;
	}

	private int countTranscriptsWithGene() {
		int counter = 0;
		for (Transcript m : network.getTranscripts()) {
			if (assertFound(network.getGenes(m))) {
				counter++;
			}
		}
		return counter;
	}

	private int countReactionWithoutPathway() {
		int counter = 0;
		for (Reaction r : network.getReactions()) {
			if (!linkToPathway(r)) {
				counter++;
			}
		}
		return counter;
	}

	private int countEnzymesWithoutReaction() {
		int counter = 0;
		for (Enzyme e : network.getEnzymes()) {
			if (!linkToReaction(e)) {
				counter++;
			}
		}
		return counter;
	}

	private int countGenesWithoutReaction() {
		int counter = 0;
		for (Gene g : network.getGenes()) {
			if (!linkToReaction(g)) {
				counter++;
			}
		}
		return counter;
	}

	private int countGenesWithoutEnzymes() {
		int counter = 0;
		for (Gene e : network.getGenes()) {
			if (!assertFound(network.getEncodedEnzymes(e))) {
				counter++;
			}
		}
		return counter;
	}

	private int countMarkerWithoutReaction() {
		int counter = 0;
		for (Marker m : network.getMarkers()) {
			if (!linkToReaction(m)) {
				counter++;
			}
		}
		return counter;
	}

	private int countTranscriptsWithoutReaction() {
		int counter = 0;
		for (Transcript t : network.getTranscripts()) {

			if (!linkToReaction(t)) {
				counter++;
			}
		}
		return counter;
	}

	private int countGenesWithTranscript() {
		int counter = 0;
		for (Gene e : network.getGenes()) {
			if (assertFound(network.getTranscripts(e))) {
				counter++;
			}
		}
		return counter;
	}

	private int countEnzymesWithTranscript() {
		int counter = 0;
		for (Enzyme e : network.getEnzymes()) {
			boolean found = false;
			for (Gene g : network.getEncodingGenes(e)) {
				if (assertFound(network.getTranscripts(g))) {
					found = true;
				}
			}

			if (found) {
				counter++;
			}

		}
		return counter;
	}

	private int countCompoundsWithMarker() {
		int counter = 0;
		for (Compound c : network.getCompounds()) {
			if (assertFound(network.getAnnotatingMarker(c))) {
				counter++;
			}
		}
		return counter;
	}

	public int countUniqueReactionsReachableFrom(Collection<? extends GraphObject> objs) {
		Set<Reaction> rs = new TreeSet<Reaction>();
		for (GraphObject o : objs) {
			if (o instanceof Transcript) {
				for (Gene g : network.getGenes((Transcript) o)) {
					for (Enzyme e : network.getEncodedEnzymes(g)) {
						rs.addAll(network.getReactions(e));
					}
				}
			}
			else if (o instanceof Marker) {
				for (Compound c : network.getAnnotations((Marker) o)) {
					rs.addAll(network.getReactions(c));
				}
			}
		}
		return rs.size();
	}

	private boolean assertFound(Collection<? extends GraphObject> list) {
		return list != null && !list.isEmpty();
	}

	private <T extends GraphObject> boolean linkToReaction(T o) {
		ArrayList<T> c = new ArrayList<>(1);
		c.add(o);
		return linkToReaction(c);
	}

	private <T extends GraphObject> boolean linkToReaction(Collection<? extends GraphObject> os) {
		if (os.isEmpty()) {
			return false;
		}

		Class<? extends GraphObject> c = os.iterator().next().getClass();

		if (c.equals(Marker.class)) {
			for (GraphObject mo : os) {
				if (linkToReaction(network.getAnnotations((Marker) mo))) {
					return true;
				}
			}
		}

		if (c.equals(Compound.class)) {
			for (GraphObject mo : os) {
				if (!network.getReactions((Compound) mo).isEmpty()) {
					return true;
				}
			}
		}

		if (c.equals(Reaction.class)) {
			return true;
		}

		if (c.equals(Enzyme.class)) {
			for (GraphObject mo : os) {
				if (!network.getReactions((Enzyme) mo).isEmpty()) {
					return true;
				}
			}
		}

		if (c.equals(Gene.class)) {
			for (GraphObject mo : os) {
				if (linkToReaction(network.getEncodedEnzymes((Gene) mo))) {
					return true;
				}
			}
		}

		if (c.equals(Transcript.class)) {
			for (GraphObject mo : os) {
				if (linkToReaction(network.getGenes((Transcript) mo))) {
					return true;
				}
			}
		}

		if (c.equals(Pathway.class)) {
			for (GraphObject mo : os) {
				if (!network.getReactions((Pathway) mo).isEmpty()) {
					return true;
				}
			}
		}

		return false;
	}

	private <T extends GraphObject> boolean linkToPathway(T o) {
		ArrayList<T> c = new ArrayList<>(1);
		c.add(o);
		return linkToPathway(c);
	}

	private <T extends GraphObject> boolean linkToPathway(Collection<? extends GraphObject> os) {
		if (os.isEmpty()) {
			return false;
		}

		Class<? extends GraphObject> c = os.iterator().next().getClass();

		if (c.equals(Marker.class)) {
			for (GraphObject mo : os) {
				if (linkToPathway(network.getAnnotations((Marker) mo))) {
					return true;
				}
			}
		}

		if (c.equals(Compound.class)) {
			for (GraphObject mo : os) {
				if (!linkToPathway(network.getReactions((Compound) mo))) {
					return true;
				}
			}
		}

		if (c.equals(Reaction.class)) {
			for (GraphObject mo : os) {
				if (!network.getPathways((Reaction) mo).isEmpty()) {
					return true;
				}
			}
		}

		if (c.equals(Enzyme.class)) {
			for (GraphObject mo : os) {
				if (linkToPathway(network.getReactions((Enzyme) mo))) {
					return true;
				}
			}
		}

		if (c.equals(Gene.class)) {
			for (GraphObject mo : os) {
				if (linkToPathway(network.getEncodedEnzymes((Gene) mo))) {
					return true;
				}
			}
		}

		if (c.equals(Transcript.class)) {
			for (GraphObject mo : os) {
				if (linkToPathway(network.getGenes((Transcript) mo))) {
					return true;
				}
			}
		}

		if (c.equals(Pathway.class)) {
			return true;
		}

		return false;
	}

	private int countMissingReactionLink(Collection<? extends GraphObject> os) {
		int counter = 0;

		for (GraphObject o : os) {
			if (!linkToReaction(o)) {
				counter++;
			}
		}

		return counter;
	}

	private int countMissingPathwayLink(Collection<? extends GraphObject> os) {
		int counter = 0;

		for (GraphObject o : os) {
			if (!linkToPathway(o)) {
				counter++;
			}
		}

		return counter;
	}
}

package de.gobics.marvis.graph.sort;

import de.gobics.marvis.graph.Compound;
import de.gobics.marvis.graph.Gene;
import de.gobics.marvis.graph.MetabolicNetwork;
import de.gobics.marvis.utils.stat.HypergeometricDistribution;

/**
 *
 * @author manuel
 */
public class NetworkSorterSEA extends AbstractGraphScore {

	private HypergeometricDistribution dist = null;

	public NetworkSorterSEA(MetabolicNetwork p) {
		super(p);
	}

	public NetworkSorterSEA() {
	}

	@Override
	public void setParent(MetabolicNetwork p) {
		super.setParent(p);
		int overall_size = 0;
		if (getParent().hasMarkers()) {
			overall_size += getParent().getCompounds().size();
		}
		if (getParent().hasTranscripts()) {
			overall_size += getParent().getGenes().size();
		}

		int good = 0;
		if (getParent().hasMarkers()) {
			for (Compound c : getParent().getCompounds()) {
				if (getParent().isExplainable(c)) {
					good++;
				}
			}

		}
		if (getParent().hasTranscripts()) {
			for (Gene g : getParent().getGenes()) {
				if (getParent().isExplainable(g)) {
					good++;
				}
			}
		}

		dist = new HypergeometricDistribution(overall_size, good);
	}

	@Override
	public Double calculateScore(MetabolicNetwork graph) {
		if (dist == null) {
			throw new RuntimeException("Can not calculate required parameter");
		}
		int set_size = 0;
		int i = 0;
		if (getParent().hasMarkers()) {
			set_size += graph.getCompounds().size();
			for (Compound c : graph.getCompounds()) {
				if (graph.isExplainable(c)) {
					i++;
				}
			}

		}
		if (getParent().hasTranscripts()) {
			set_size += graph.getGenes().size();
			for (Gene g : graph.getGenes()) {
				if (graph.isExplainable(g)) {
					i++;
				}
			}
		}
		return dist.hypergeom(set_size, i);
	}

	@Override
	public String getName() {
		return "Set enrichment";
	}

	@Override
	public String getDescription() {
		return "Applies variant of set enrichment analysis using hypergeometric distribution";
	}

	@Override
	public AbstractGraphScore like(MetabolicNetwork new_parent) {
		return new NetworkSorterSEA(new_parent);
	}
}
